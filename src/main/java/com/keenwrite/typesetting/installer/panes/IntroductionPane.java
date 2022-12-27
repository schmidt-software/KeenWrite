/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.typesetting.installer.panes;

public final class IntroductionPane extends InstallerPane {
  private static final String PREFIX =
    "Wizard.typesetter.all.1.install";

  public IntroductionPane() {
    super( PREFIX + ".header" );

    setContent( flowPane(
      hyperlink( PREFIX + ".about.container.link" ),
      label( PREFIX + ".about.text.1" ),
      hyperlink( PREFIX + ".about.typesetter.link" ),
      label( PREFIX + ".about.text.2" )
    ) );
  }
}
