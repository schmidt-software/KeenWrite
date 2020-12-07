/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.spelling.impl;

import com.keenwrite.spelling.api.SpellCheckListener;
import com.keenwrite.spelling.api.SpellChecker;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.NodeVisitor;
import com.vladsch.flexmark.util.ast.VisitHandler;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static org.fxmisc.richtext.model.TwoDimensional.Bias.Forward;

/**
 * Responsible for checking the spelling of a document being edited.
 */
public class TextEditorSpeller {
  /**
   * Only load the dictionary into memory once, because it's huge.
   */
  private static final SpellChecker mSpellChecker =
      SymSpellSpeller.forLexicon( "en.txt" );

  public TextEditorSpeller() {
  }

  /**
   * Delegates to {@link #spellcheck(StyleClassedTextArea, String, int)}.
   * call to spell check the entire document.
   */
  public void checkDocument( final StyleClassedTextArea editor ) {
    spellcheck( editor, editor.getText(), -1 );
  }

  /**
   * Listen for changes to the any particular paragraph and perform a quick
   * spell check upon it. The style classes in the editor will be changed to
   * mark any spelling mistakes in the paragraph. The user may then interact
   * with any misspelled word (i.e., any piece of text that is marked) to
   * revise the spelling.
   *
   * @param editor The text area containing paragraphs to spellcheck.
   */
  public void checkParagraphs( final StyleClassedTextArea editor ) {
    // Use the plain text changes so that notifications of style changes
    // are suppressed. Checking against the identity ensures that only
    // new text additions or deletions trigger proofreading.
    editor.plainTextChanges()
          .filter( p -> !p.isIdentity() ).subscribe( change -> {

      // Check current paragraph; the whole document was checked upon opening.
      final var offset = change.getPosition();
      final var position = editor.offsetToPosition( offset, Forward );
      final var paraId = position.getMajor();
      final var paragraph = editor.getParagraph( paraId );
      final var text = paragraph.getText();

      // Prevent doubling-up styles.
      editor.clearStyle( paraId );

      spellcheck( editor, text, paraId );
    } );
  }

  /**
   * Spellchecks a subset of the entire document.
   *
   * @param text   Look up words for this text in the lexicon.
   * @param paraId Set to -1 to apply resulting style spans to the entire
   *               text.
   */
  private void spellcheck(
      final StyleClassedTextArea editor, final String text, final int paraId ) {
    final var builder = new StyleSpansBuilder<Collection<String>>();
    final var runningIndex = new AtomicInteger( 0 );

    // The text nodes must be relayed through a contextual "visitor" that
    // can return text in chunks with correlative offsets into the string.
    // This allows Markdown, R Markdown, XML, and R XML documents to return
    // sets of words to check.

    final var node = mParser.parse( text );
    final var visitor = new TextVisitor( ( visited, bIndex, eIndex ) -> {
      // Treat hyphenated compound words as individual words.
      final var check = visited.replace( '-', ' ' );

      mSpellChecker.proofread( check, ( misspelled, prevIndex, currIndex ) -> {
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
   * TODO: #59 -- Replace using Markdown processor instantiated for Markdown
   * files.
   */
  private final Parser mParser = Parser.builder().build();

  /**
   * TODO: #59 -- Replace with generic interface; provide Markdown/XML
   * implementations.
   */
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
