/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.typesetting.installer.panes;

/**
 * Responsible for informing the user as to what will happen next.
 */
public final class IntroductionPane extends InstallerPane {
  private static final String PREFIX = "Wizard.typesetter.all.1.install";

  public IntroductionPane() {
    setContent( flowPane(
      hyperlink( PREFIX + ".about.container.link" ),
      label( PREFIX + ".about.text.1" ),
      hyperlink( PREFIX + ".about.typesetter.link" ),
      label( PREFIX + ".about.text.2" )
    ) );
  }

  @Override
  protected String getHeaderKey() {
    return PREFIX + ".header";
  }
}
