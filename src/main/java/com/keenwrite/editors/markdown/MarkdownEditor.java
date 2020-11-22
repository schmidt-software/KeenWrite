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

import com.keenwrite.TextResource;
import com.keenwrite.editors.TextEditor;
import com.keenwrite.processors.markdown.CaretPosition;
import javafx.scene.layout.BorderPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.StyleClassedTextArea;

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

  public MarkdownEditor() {
    mTextArea.setWrapText( true );
    mTextArea.getStyleClass().add( "markdown" );
    mTextArea.getStylesheets().add( STYLESHEET_MARKDOWN );
    mTextArea.requestFollowCaret();

    mScrollPane.setVbarPolicy( ALWAYS );

    setCenter( mScrollPane );
  }

  /**
   * Delegate the focus request to the text area itself.
   */
  @Override
  public void requestFocus() {
    System.out.println( "WE GOT FOCUS, SOUND THE ALARM" );
    mTextArea.requestFocus();
  }

  @Override
  public void setText( final String text ) {
    mTextArea.clear();
    mTextArea.appendText( text );
  }

  @Override
  public String getText() {
    return mTextArea.getText();
  }

  /**
   * Changes the absolute position of the caret within the editor. This will
   * update the view port to follow the caret position because of the call
   * to {@link StyleClassedTextArea#requestFollowCaret()}.
   *
   * @param position The new caret position.
   */
  @Override
  public void setInsertionPoint( final int position ) {
    mTextArea.moveTo( position );
  }

  @Override
  public CaretPosition createCaretPosition() {
    return CaretPosition
        .builder().with( CaretPosition.Mutator::setEditor, mTextArea ).build();
  }
}
