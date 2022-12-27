/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.typesetting.installer;

import static com.keenwrite.typesetting.installer.InstallPane.wizardPane;

public final class UniversalContainerInstallPane {
  private static final String PREFIX =
    "Wizard.typesetter.all.2.install.container";

  static InstallPane create() {
//    Wizard.typesetter.all.2.install.container.homepage.lbl=${Wizard
//    .typesetter.container.name}
//    Wizard.typesetter.all.2.install.container.homepage.url=https://podman.io

    return wizardPane( PREFIX + ".header" );
  }
}
