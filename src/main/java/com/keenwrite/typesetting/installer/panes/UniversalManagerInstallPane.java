/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.typesetting.installer.panes;

public final class UniversalManagerInstallPane extends InstallerPane {
  private static final String PREFIX =
    "Wizard.typesetter.all.2.install.container";

  public UniversalManagerInstallPane() { }

  @Override
  protected String getHeaderKey() {
    return PREFIX + ".header";
  }
}
