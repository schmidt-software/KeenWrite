/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.typesetting.installer;

import static com.keenwrite.typesetting.container.impl.Podman.CONTAINER_NAME;
import static com.keenwrite.typesetting.installer.TypesetterInstaller.createContainerOutputPanel;

public final class TypesetterImageDownloadPane {
  private static final String PREFIX =
    "Wizard.typesetter.all.4.download.typesetter";

  static InstallPane create() {
    return createContainerOutputPanel(
      PREFIX + ".header",
      PREFIX + ".correct",
      PREFIX + ".missing",
      container -> container.pull( CONTAINER_NAME ),
      45
    );
  }
}
