/* Copyright 2020 White Magic Software, Ltd.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.keenwrite.editors.markdown;

import com.keenwrite.Constants;
import com.keenwrite.editors.TextEditor;
import com.keenwrite.io.File;
import com.keenwrite.processors.markdown.CaretPosition;
import com.keenwrite.spelling.api.SpellCheckListener;
import com.keenwrite.spelling.api.SpellChecker;
import com.keenwrite.spelling.impl.SymSpellSpeller;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.NodeVisitor;
import com.vladsch.flexmark.util.ast.VisitHandler;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import static com.keenwrite.Constants.DEFAULT_DOCUMENT;
import static com.keenwrite.Constants.STYLESHEET_MARKDOWN;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static javafx.scene.control.ScrollPane.ScrollBarPolicy.ALWAYS;
import static org.fxmisc.richtext.model.TwoDimensional.Bias.Forward;

/**
 * Responsible for editing Markdown documents.
 */
public class MarkdownEditor extends BorderPane implements TextEditor {
  /**
   * The text editor.
   */
  private final StyleClassedTextArea mTextArea =
      new StyleClassedTextArea( false );

  /**
   * Wraps the text editor in scrollbars.
   */
  private final VirtualizedScrollPane<StyleClassedTextArea> mScrollPane =
      new VirtualizedScrollPane<>( mTextArea );

  /**
   * File being edited by this editor instance.
   */
  private File mFile;

  /**
   * Set to {@code true} upon text or caret position changes. Value is {@code
   * false} by default.
   */
  private final BooleanProperty mDirty = new SimpleBooleanProperty();

  /**
   * Responsible for checking the spelling of the document being edited.
   */
  private final SpellChecker mSpellChecker;

  /**
   * Opened file's character encoding, or {@link Constants#DEFAULT_CHARSET} if
   * either no encoding could be determined or this is a new (empty) file.
   */
  private final Charset mEncoding;

  public MarkdownEditor() {
    this( DEFAULT_DOCUMENT );
  }

  public MarkdownEditor( final File file ) {
    mEncoding = open( mFile = file );

    mTextArea.setWrapText( true );
    mTextArea.getStyleClass().add( "markdown" );
    mTextArea.getStylesheets().add( STYLESHEET_MARKDOWN );
    mTextArea.requestFollowCaret();
    mTextArea.moveTo( 0 );
    mTextArea.textProperty().addListener( ( c, o, n ) -> {
      // Fire, regardless of whether the caret position has changed.
      mDirty.set( false );

      // Prevent a caret position change from raising the dirty bits.
      mDirty.set( true );
    } );
    mTextArea.caretPositionProperty().addListener( ( c, o, n ) -> {
      // Fire when the caret position has changed and the text has not.
      mDirty.set( true );
      mDirty.set( false );
    } );

    mScrollPane.setVbarPolicy( ALWAYS );
    setCenter( mScrollPane );

    mSpellChecker = SymSpellSpeller.forLexicon( "en.txt" );
    spellcheckDocument( mTextArea );
    spellcheckParagraphs( mTextArea );
  }

  /**
   * Observers may listen for changes to the property returned from this method
   * to receive notifications when either the text or caret have changed.
   * This should not be used to track whether the text has been modified.
   */
  public void addDirtyListener( ChangeListener<Boolean> listener ) {
    mDirty.addListener( listener );
  }

  public CaretPosition createCaretPosition() {
    return CaretPosition
        .builder()
        .with( CaretPosition.Mutator::setEditor, mTextArea )
        .build();
  }

  /**
   * Delegate the focus request to the text area itself.
   */
  @Override
  public void requestFocus() {
    mTextArea.requestFocus();
  }

  @Override
  public void setText( final String text ) {
    mTextArea.clear();
    mTextArea.appendText( text );
    mTextArea.getUndoManager().forgetHistory();
  }

  @Override
  public String getText() {
    return mTextArea.getText();
  }

  @Override
  public Charset getEncoding() {
    return mEncoding;
  }

  @Override
  public File getFile() {
    return mFile;
  }

  @Override
  public void rename( final File file ) {
    mFile = file;
  }

  @Override
  public Node getNode() {
    return this;
  }

  @Override
  public VirtualizedScrollPane<StyleClassedTextArea> getScrollPane() {
    return mScrollPane;
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
  private void spellcheckParagraphs( final StyleClassedTextArea editor ) {

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
   * Delegates to {@link #spellcheck(StyleClassedTextArea, String, int)}.
   * call to spell check the entire document.
   */
  private void spellcheckDocument( final StyleClassedTextArea editor ) {
    spellcheck( editor, editor.getText(), -1 );
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

  // TODO: #59 -- Replace using Markdown processor instantiated for Markdown
  //  files.
  private final Parser mParser = Parser.builder().build();

  // TODO: #59 -- Replace with generic interface; provide Markdown/XML
  //  implementations.
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
