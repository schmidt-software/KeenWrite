/*
 * Copyright 2020 Karl Tauber and White Magic Software, Ltd.
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
package com.keenwrite;

import com.keenwrite.editors.markdown.MarkdownEditorPane;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * Editor for a single file.
 */
public final class FileEditorController {

  private final MarkdownEditorPane mEditorPane = new MarkdownEditorPane();

  /**
   * File to load into the editor.
   */
  private Path mPath;

  /**
   * Searches from the caret position forward for the given string.
   *
   * @param needle The text string to match.
   */
  public void searchNext( final String needle ) {
    final var haystack = getEditorText();
    int index = haystack.indexOf( needle, getCaretTextOffset() );

    // Wrap around.
    if( index == -1 ) {
      index = haystack.indexOf( needle );
    }

    if( index >= 0 ) {
      setCaretTextOffset( index );
      getEditor().selectRange( index, index + needle.length() );
    }
  }

  /**
   * Returns the index into the text where the caret blinks happily away.
   *
   * @return A number from 0 to the editor's document text length.
   */
  private int getCaretTextOffset() {
    return getEditor().getCaretPosition();
  }

  /**
   * Moves the caret to a given offset.
   *
   * @param offset The new caret offset.
   */
  private void setCaretTextOffset( final int offset ) {
    getEditor().moveTo( offset );
    //getEditor().requestFollowCaret();
  }

  /**
   * Returns the text area associated with this tab.
   *
   * @return A text editor.
   */
  private StyleClassedTextArea getEditor() {
    return getEditorPane().getEditor();
  }

  /**
   * Returns the path to the file being edited in this tab.
   *
   * @return A non-null instance.
   */
  public Path getPath() {
    return mPath;
  }

  /**
   * Sets the path to a file for editing and then updates the tab with the
   * file contents.
   *
   * @param path A non-null instance.
   */
  public void setPath( final Path path ) {
    assert path != null;
    mPath = path;
  }

  /**
   * Forwards the request to the editor pane.
   *
   * @return The text to process.
   */
  public String getEditorText() {
    return getEditorPane().getText();
  }

  /**
   * Returns the editor pane, or creates one if it doesn't yet exist.
   *
   * @return The editor pane, never null.
   */
  @NotNull
  public MarkdownEditorPane getEditorPane() {
    return mEditorPane;
  }
}
