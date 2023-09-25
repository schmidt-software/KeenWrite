/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.typesetting.installer.panes;

import com.keenwrite.typesetting.containerization.ContainerManager;

/**
 * Responsible for installing the typesetter's image via the container manager.
 */
public final class TypesetterImageDownloadPane extends ManagerOutputPane {
  private static final String PREFIX =
    "Wizard.typesetter.all.4.download.image";

  public TypesetterImageDownloadPane() {
    super(
      PREFIX + ".correct",
      PREFIX + ".missing",
      ContainerManager::load,
      45
    );
  }

  @Override
  public String getHeaderKey() {
    return PREFIX + ".header";
  }
}
