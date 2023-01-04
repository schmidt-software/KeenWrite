/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.ui.spelling;

import com.keenwrite.editors.TextEditor;
import com.keenwrite.spelling.api.SpellCheckListener;
import com.keenwrite.spelling.api.SpellChecker;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.NodeVisitor;
import com.vladsch.flexmark.util.ast.VisitHandler;
import javafx.beans.property.ObjectProperty;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.IndexRange;
import javafx.scene.control.MenuItem;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.fxmisc.richtext.model.PlainTextChange;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.keenwrite.events.StatusEvent.clue;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static org.fxmisc.richtext.model.TwoDimensional.Bias.Forward;

/**
 * Responsible for checking the spelling of a document being edited.
 */
public final class TextEditorSpellChecker {
  private final ObjectProperty<SpellChecker> mSpellChecker;
  private final Parser mParser = Parser.builder().build();

  /**
   * Create a new spellchecker that can highlight spelling mistakes within a
   * {@link StyleClassedTextArea}. The given {@link SpellChecker} is wrapped
   * in a mutable {@link ObjectProperty} because the user may swap languages
   * at runtime.
   *
   * @param checker The spellchecker to use when scanning for spelling errors.
   */
  public TextEditorSpellChecker( final ObjectProperty<SpellChecker> checker ) {
    assert checker != null;

    mSpellChecker = checker;
  }

  /**
   * Call to spellcheck the entire document.
   */
  public void checkDocument( final TextEditor editor ) {
    spellcheck( editor.getTextArea(), editor.getText(), -1 );
  }

  /**
   * Listen for changes to any particular paragraph and perform a quick
   * spell check upon it. The style classes in the editor will be changed to
   * mark any spelling mistakes in the paragraph. The user may then interact
   * with any misspelled word (i.e., any piece of text that is marked) to
   * revise the spelling.
   * <p>
   * Use {@link PlainTextChange} so that notifications of style changes
   * are suppressed. Checking against the identity ensures that only
   * new text additions or deletions trigger proofreading.
   */
  public void checkParagraph(
    final StyleClassedTextArea editor,
    final PlainTextChange change ) {
    // Check current paragraph; the document was checked when opened.
    final var offset = change.getPosition();
    final var position = editor.offsetToPosition( offset, Forward );
    var paraId = position.getMajor();
    var paragraph = editor.getParagraph( paraId );
    var text = paragraph.getText();

    // If the current paragraph is blank, it may mean the caret is at the
    // start of a new paragraph (i.e., a blank line). Spellcheck the "next"
    // paragraph, instead.
    if( text.isBlank() ) {
      final var paragraphs = editor.getParagraphs().size();

      paraId = Math.min( paraId + 1, paragraphs - 1 );
      paragraph = editor.getParagraph( paraId );
      text = paragraph.getText();
    }

    // Prevent doubling-up styles.
    editor.clearStyle( paraId );

    spellcheck( editor, text, paraId );
  }

  /**
   * Spellchecks a subset of the entire document.
   *
   * @param editor The document (or portions thereof) to spellcheck.
   * @param text   Look up words for this text in the lexicon.
   * @param paraId Set to -1 to apply resulting style spans to the entire text.
   */
  private void spellcheck(
    final StyleClassedTextArea editor, final String text, final int paraId ) {
    final var builder = new StyleSpansBuilder<Collection<String>>();
    final var runningIndex = new AtomicInteger( 0 );

    // The text nodes must be relayed through a contextual "visitor" that
    // can return text in chunks with correlative offsets into the string.
    // This allows Markdown and R Markdown documents to return sets of
    // words to check.
    final var node = mParser.parse( text );
    final var visitor = new TextVisitor( ( visited, bIndex, eIndex ) -> {
      // Treat hyphenated compound words as individual words.
      final var check = visited.replace( '-', ' ' );
      final var checker = getSpellChecker();

      checker.proofread( check, ( misspelled, prevIndex, currIndex ) -> {
        prevIndex += bIndex;
        currIndex += bIndex;

        // Clear styling between lexiconically absent words.
        builder.add( emptyList(), prevIndex - runningIndex.get() );
        builder.add( singleton( "spelling" ), currIndex - prevIndex );
        runningIndex.set( currIndex );
      } );
    } );

    visitor.visit( node );

    // If the running index was set, at least one word triggered the listener.
    if( runningIndex.get() > 0 ) {
      // Clear styling after the last lexiconically absent word.
      builder.add( emptyList(), text.length() - runningIndex.get() );

      final var spans = builder.create();

      if( paraId >= 0 ) {
        editor.setStyleSpans( paraId, 0, spans );
      }
      else {
        editor.setStyleSpans( 0, spans );
      }
    }
  }

  /**
   * Called to display a pop-up with a list of spelling corrections. When the
   * user selects an item from the list, the word at the caret position is
   * replaced (with the selected item).
   */
  public void autofix( final TextEditor editor ) {
    final var caretWord = editor.getCaretWord();
    final var textArea = editor.getTextArea();
    final var word = textArea.getText( caretWord );
    final var suggestions = checkWord( word, 10 );

    if( suggestions.isEmpty() ) {
      clue( "Editor.spelling.check.matches.none", word );
    }
    else if( !suggestions.contains( word ) ) {
      final var menu = createSuggestionsPopup( textArea );
      final var items = menu.getItems();
      textArea.setContextMenu( menu );

      for( final var correction : suggestions ) {
        items.add( createSuggestedItem( textArea, caretWord, correction ) );
      }

      textArea.getCaretBounds().ifPresent(
        bounds -> {
          menu.setOnShown( event -> menu.requestFocus() );
          menu.show( textArea, bounds.getCenterX(), bounds.getCenterY() );
        }
      );
    }
    else {
      clue( "Editor.spelling.check.matches.okay", word );
    }
  }

  private ContextMenu createSuggestionsPopup(
    final StyleClassedTextArea textArea ) {
    final var menu = new ContextMenu();

    menu.setAutoHide( true );
    menu.setHideOnEscape( true );
    menu.setOnHidden( event -> textArea.setContextMenu( null ) );

    return menu;
  }

  /**
   * Creates a menu item capable of replacing a word under the cursor.
   *
   * @param textArea The text upon which this action will replace.
   * @param i        The beginning and ending text offset to replace.
   * @param s        The text to replace at the given offset.
   * @return The menu item that, if actioned, will replace the text.
   */
  private MenuItem createSuggestedItem(
    final StyleClassedTextArea textArea,
    final IndexRange i,
    final String s ) {
    final var menuItem = new MenuItem( s );

    menuItem.setOnAction( event -> textArea.replaceText( i, s ) );

    return menuItem;
  }

  /**
   * Returns a list of suggests for the given word. This is typically used to
   * check for suitable replacements of the word at the caret position.
   *
   * @param word  The word to spellcheck.
   * @param count The maximum number of suggested alternatives to return.
   * @return A list of recommended spellings for the given word.
   */
  public List<String> checkWord( final String word, final int count ) {
    return getSpellChecker().suggestions( word, count );
  }

  private SpellChecker getSpellChecker() {
    return mSpellChecker.get();
  }

  private static final class TextVisitor {
    private final NodeVisitor mVisitor = new NodeVisitor( new VisitHandler<>(
      com.vladsch.flexmark.ast.Text.class, this::visit )
    );

    private final SpellCheckListener mConsumer;

    public TextVisitor( final SpellCheckListener consumer ) {
      mConsumer = consumer;
    }

    private void visit( final com.vladsch.flexmark.util.ast.Node node ) {
      if( node instanceof com.vladsch.flexmark.ast.Text ) {
        mConsumer.accept( node.getChars().toString(),
                          node.getStartOffset(),
                          node.getEndOffset() );
      }

      mVisitor.visitChildren( node );
    }
  }
}
