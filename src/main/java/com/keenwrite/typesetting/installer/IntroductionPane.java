/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.typesetting.installer;

import static com.keenwrite.typesetting.installer.InstallPane.*;

public final class IntroductionPane {
  public static InstallPane create() {
    final var pane = wizardPane(
      "Wizard.typesetter.all.1.install.header" );
    pane.setContent( flowPane(
      hyperlink( "Wizard.typesetter.all.1.install.about.container.link" ),
      label( "Wizard.typesetter.all.1.install.about.text.1" ),
      hyperlink( "Wizard.typesetter.all.1.install.about.typesetter.link" ),
      label( "Wizard.typesetter.all.1.install.about.text.2" )
    ) );

    return pane;
  }
}
