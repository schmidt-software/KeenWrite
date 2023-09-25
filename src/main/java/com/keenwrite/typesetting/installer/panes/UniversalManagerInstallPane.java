/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.typesetting.installer.panes;

/**
 * Responsible for installing the container manager for any operating system
 * that was not explicitly detected.
 */
public final class UniversalManagerInstallPane extends InstallerPane {
  private static final String PREFIX =
    "Wizard.typesetter.all.2.install.container";

  public UniversalManagerInstallPane() { }

  @Override
  protected String getHeaderKey() {
    return PREFIX + ".header";
  }
}
