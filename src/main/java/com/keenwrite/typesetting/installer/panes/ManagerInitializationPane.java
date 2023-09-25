/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.typesetting.installer.panes;

import com.keenwrite.typesetting.containerization.ContainerManager;

/**
 * Responsible for initializing the container manager on all platforms except
 * for Linux.
 */
public final class ManagerInitializationPane extends ManagerOutputPane {

  private static final String PREFIX =
    "Wizard.typesetter.all.3.install.container";

  public ManagerInitializationPane() {
    super(
      PREFIX + ".correct",
      PREFIX + ".missing",
      ContainerManager::start,
      35
    );
  }

  @Override
  public String getHeaderKey() {
    return PREFIX + ".header";
  }
}
