/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.typesetting.installer;

import static com.keenwrite.typesetting.container.impl.Podman.CONTAINER_NAME;
import static com.keenwrite.typesetting.installer.TypesetterInstaller.createContainerOutputPanel;

public  final class TypesetterImageDownloadPanel {
  public static InstallPane create() {
    return createContainerOutputPanel(
      "Wizard.typesetter.all.4.download.typesetter.header",
      "Wizard.typesetter.all.4.download.typesetter.correct",
      "Wizard.typesetter.all.4.download.typesetter.missing",
      container -> container.pull( CONTAINER_NAME ),
      45
    );
  }
}
