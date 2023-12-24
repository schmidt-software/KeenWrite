/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.ui.dialogs;

import com.keenwrite.ui.models.ImageModel;
import javafx.stage.Window;

/**
 * Dialog to enter a Markdown image.
 */
public class ImageDialog extends CustomDialog<String> {
  private static final String PREFIX = "Dialog.image.";

  private final ImageModel mModel;

  public ImageDialog( final Window owner, final ImageModel model ) {
    super( owner, STR."\{PREFIX}title" );

    mModel = model;

    super.initialize();
  }

  @Override
  protected void initInputFields() {
    addInputField(
      "url",
      STR."\{PREFIX}label.url", STR."\{PREFIX}prompt.url",
      mModel.getUrl(),
      ( _, _, n ) -> mModel.setUrl( n )
    );
    addInputField(
      "text",
      STR."\{PREFIX}label.text", STR."\{PREFIX}prompt.text",
      mModel.getText(),
      ( _, _, n ) -> mModel.setText( n )
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
