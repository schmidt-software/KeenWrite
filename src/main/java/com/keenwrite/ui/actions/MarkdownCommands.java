/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.ui.actions;

import com.keenwrite.editors.markdown.HyperlinkModel;
import com.keenwrite.editors.markdown.LinkVisitor;
import com.keenwrite.processors.markdown.MarkdownProcessor;
import com.keenwrite.ui.dialogs.ImageDialog;
import com.keenwrite.ui.dialogs.LinkDialog;
import com.vladsch.flexmark.ast.Link;
import javafx.scene.control.Dialog;
import javafx.stage.Window;
import org.fxmisc.richtext.StyleClassedTextArea;

import java.nio.file.Path;

public class MarkdownCommands {

  private final Window mParent;
  private final Path mBase;

  public MarkdownCommands( final Window parent, final Path path ) {
    mParent = parent;
    mBase = path.getParent();
  }

  public void insertLink( final StyleClassedTextArea textArea ) {
    insertObject( createLinkDialog( textArea ), textArea );
  }

  public void insertImage( final StyleClassedTextArea textArea ) {
    insertObject( createImageDialog(), textArea );
  }

  /**
   * Returns one of: selected text, word under cursor, or parsed hyperlink from
   * the markdown AST.
   *
   * @return An instance containing the link URL and display text.
   */
  private HyperlinkModel getHyperlink( final StyleClassedTextArea textArea ) {
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

  private Dialog<String> createLinkDialog(
      final StyleClassedTextArea textArea ) {
    return new LinkDialog( getWindow(), getHyperlink( textArea ) );
  }

  private Dialog<String> createImageDialog() {
    return new ImageDialog( getWindow(), getParentPath() );
  }

  private void insertObject(
      final Dialog<String> dialog, final StyleClassedTextArea textArea ) {
    dialog.showAndWait().ifPresent( textArea::replaceSelection );
  }

  private Path getParentPath() {
    return mBase;
  }

  private Window getWindow() {
    return mParent;
  }
}
