package com.keenwrite.ui.dialogs;

import javafx.stage.Window;

import java.io.File;

public class OpenUrlDialog extends CustomDialog<File> {
  private static final String PREFIX = "Dialog.open_url.";

  private String mUrl;

  /**
   * Ensures that all dialogs can be closed.
   *
   * @param owner The parent window of this dialog.
   */
  public OpenUrlDialog( final Window owner ) {
    super( owner, STR."\{PREFIX}title" );
    super.initialize();
  }

  @Override
  protected void initInputFields() {
    addInputField(
      "url",
      STR."\{PREFIX}label.url", STR."\{PREFIX}prompt.url",
      "",
      ( _, _, n ) -> mUrl = n
    );
  }

  @Override
  protected File handleAccept() {
    System.out.println( STR."OPEN URL: \{mUrl}" );

    return null;
  }
}
