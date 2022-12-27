/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.typesetting.installer;

import static com.keenwrite.typesetting.installer.InstallPane.*;

public final class IntroductionPane {
  private static final String PREFIX =
    "Wizard.typesetter.all.1.install";

  public static InstallPane create() {
    final var pane = wizardPane( PREFIX + ".header" );
    pane.setContent( flowPane(
      hyperlink( PREFIX + ".about.container.link" ),
      label( PREFIX + ".about.text.1" ),
      hyperlink( PREFIX + ".about.typesetter.link" ),
      label( PREFIX + ".about.text.2" )
    ) );

    return pane;
  }
}
