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

import com.keenwrite.editors.TextEditor;
import com.keenwrite.io.File;
import com.keenwrite.processors.markdown.CaretPosition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.StyleClassedTextArea;

import static com.keenwrite.Constants.DEFAULT_DOCUMENT;
import static com.keenwrite.Constants.STYLESHEET_MARKDOWN;
import static javafx.scene.control.ScrollPane.ScrollBarPolicy.ALWAYS;

/**
 * Responsible for editing Markdown documents.
 */
public class MarkdownEditor extends BorderPane implements TextEditor {
  private final StyleClassedTextArea mTextArea =
      new StyleClassedTextArea( false );
  private final VirtualizedScrollPane<StyleClassedTextArea> mScrollPane =
      new VirtualizedScrollPane<>( mTextArea );

  /**
   * File being edited by this editor instance.
   */
  private final File mFile;

  /**
   * Set to {@code true} upon text or caret position changes. Value is {@code
   * false} by default.
   */
  private final BooleanProperty mDirty = new SimpleBooleanProperty();

  public MarkdownEditor() {
    this( DEFAULT_DOCUMENT );
  }

  public MarkdownEditor( final File file ) {
    readFile( mFile = file );

    mTextArea.setWrapText( true );
    mTextArea.getStyleClass().add( "markdown" );
    mTextArea.getStylesheets().add( STYLESHEET_MARKDOWN );
    mTextArea.requestFollowCaret();
    mTextArea.moveTo( 0 );
    mTextArea.textProperty().addListener( ( c, o, n ) -> {
      // Fire, regardless of whether the caret position has changed.
      mDirty.set( false );
      mDirty.set( true );
    } );
    mTextArea.caretPositionProperty().addListener( ( c, o, n ) -> {
      // Fire when the caret position has changed and the text has not.
      mDirty.set( true );
      mDirty.set( false );
    } );

    mScrollPane.setVbarPolicy( ALWAYS );

    setCenter( mScrollPane );
  }

  /**
   * Observers may listen for changes to the property returned from this method
   * to receive notifications when either the text or caret have changed.
   * This should not be used to track whether the text has been modified.
   *
   * @return An instance of {@link ObservableBooleanValue} that can be
   * listened upon for changes to either the text content or caret position.
   */
  public ObservableBooleanValue dirtyProperty() {
    return mDirty;
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
  public File getFile() {
    return mFile;
  }

  public CaretPosition createCaretPosition() {
    return CaretPosition
        .builder()
        .with( CaretPosition.Mutator::setEditor, mTextArea )
        .build();
  }

  @Override
  public Node getNode() {
    return this;
  }

  @Override
  public VirtualizedScrollPane<StyleClassedTextArea> getScrollPane() {
    return mScrollPane;
  }
}
