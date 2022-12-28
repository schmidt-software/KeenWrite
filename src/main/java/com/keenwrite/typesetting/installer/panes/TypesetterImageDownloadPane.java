/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.typesetting.installer.panes;

import static com.keenwrite.typesetting.containerization.Podman.CONTAINER_NAME;

public final class TypesetterImageDownloadPane extends ManagerOutputPane {
  private static final String PREFIX =
    "Wizard.typesetter.all.4.download.image";

  public TypesetterImageDownloadPane() {
    super(
      PREFIX + ".correct",
      PREFIX + ".missing",
      container -> container.pull( CONTAINER_NAME ),
      45
    );
  }

  @Override
  public String getHeaderKey() {
    return PREFIX + ".header";
  }
}
