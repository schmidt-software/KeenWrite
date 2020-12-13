/*
 * Copyright 2020 Karl Tauber and White Magic Software, Ltd.
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

import com.keenwrite.editors.base.PlainTextEditor;
import com.keenwrite.io.File;
import com.keenwrite.processors.markdown.MarkdownProcessor;
import com.keenwrite.ui.dialogs.ImageDialog;
import com.keenwrite.ui.dialogs.LinkDialog;
import com.vladsch.flexmark.ast.Link;
import javafx.scene.control.Dialog;
import javafx.stage.Window;

import java.nio.file.Path;

import static com.keenwrite.Constants.DEFAULT_DOCUMENT;

/**
 * Provides the ability to edit a text document.
 */
public class MarkdownEditorPane extends PlainTextEditor {
  public MarkdownEditorPane() {
    this( DEFAULT_DOCUMENT );
  }

  public MarkdownEditorPane( final File file ) {
    super( file );
  }

  public void insertLink() {
    insertObject( createLinkDialog() );
  }

  public void insertImage() {
    insertObject( createImageDialog() );
  }

  /**
   * Returns one of: selected text, word under cursor, or parsed hyperlink from
   * the markdown AST.
   *
   * @return An instance containing the link URL and display text.
   */
  private HyperlinkModel getHyperlink() {
    final var textArea = getEditor();
    final var selectedText = textArea.getSelectedText();

    // Get the current paragraph, convert to Markdown nodes.
    final var mp = MarkdownProcessor.create();
    final var p = textArea.getCurrentParagraph();
    final var paragraph = textArea.getText( p );
    final var node = mp.toNode( paragraph );
    final var visitor = new LinkVisitor( textArea.getCaretColumn() );
    final var link = visitor.process( node );

    if( link != null ) {
      textArea.selectRange( p, link.getStartOffset(), p, link.getEndOffset() );
    }

    return createHyperlinkModel(
        link, selectedText, "https://localhost"
    );
  }

  @SuppressWarnings("SameParameterValue")
  private HyperlinkModel createHyperlinkModel(
      final Link link, final String selection, final String url ) {

    return link == null
        ? new HyperlinkModel( selection, url )
        : new HyperlinkModel( link );
  }

  private Path getParentPath() {
    final Path path = getPath();
    return path != null ? path.getParent() : null;
  }

  private Dialog<String> createLinkDialog() {
    return new LinkDialog( getWindow(), getHyperlink() );
  }

  private Dialog<String> createImageDialog() {
    return new ImageDialog( getWindow(), getParentPath() );
  }

  private void insertObject( final Dialog<String> dialog ) {
    dialog.showAndWait().ifPresent(
        result -> getEditor().replaceSelection( result )
    );
  }

  private Window getWindow() {
    return getScene().getWindow();
  }
}
