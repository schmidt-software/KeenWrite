/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.ui.dialogs;

import com.keenwrite.ui.models.HyperlinkModel;
import javafx.stage.Window;

/**
 * Dialog to enter a Markdown link.
 */
public class HyperlinkDialog extends CustomDialog<String> {
  private static final String PREFIX = "Dialog.link.";

  /**
   * Contains information about the hyperlink at the caret position in the
   * document, if a hyperlink is present at that location. This allows users
   * to edit existing hyperlinks using this {@link HyperlinkDialog}.
   */
  private final HyperlinkModel mModel;

  /**
   * @param owner {@link Window} responsible for the dialog resource.
   * @param model Existing hyperlink data, or blank for a new link.
   */
  public HyperlinkDialog( final Window owner, final HyperlinkModel model ) {
    super( owner, STR."\{PREFIX}title" );

    mModel = model;

    super.initialize();
  }

  @Override
  protected void initInputFields() {
    addInputField(
      "text",
      STR."\{PREFIX}label.text", STR."\{PREFIX}prompt.text",
      mModel.getText(),
      ( _, _, n ) -> mModel.setText( n )
    );
    addInputField(
      "url",
      STR."\{PREFIX}label.url", STR."\{PREFIX}prompt.url",
      mModel.getUrl(),
      ( _, _, n ) -> mModel.setUrl( n )
    );
    addInputField(
      "title",
      STR."\{PREFIX}label.title", STR."\{PREFIX}prompt.title",
      mModel.getTitle(),
      ( _, _, n ) -> mModel.setTitle( n )
    );
  }

  @Override
  protected String handleAccept() {
    return mModel.toString();
  }
}
