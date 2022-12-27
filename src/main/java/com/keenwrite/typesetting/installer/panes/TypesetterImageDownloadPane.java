/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.typesetting.installer.panes;

import static com.keenwrite.typesetting.container.impl.Podman.CONTAINER_NAME;

public final class TypesetterImageDownloadPane extends ContainerOutputPane {
  private static final String PREFIX =
    "Wizard.typesetter.all.4.download.image";

  public TypesetterImageDownloadPane() {
    super(
      PREFIX + ".header",
      PREFIX + ".correct",
      PREFIX + ".missing",
      container -> container.pull( CONTAINER_NAME ),
      45
    );
  }
}
