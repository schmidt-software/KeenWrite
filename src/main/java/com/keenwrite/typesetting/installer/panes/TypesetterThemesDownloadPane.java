/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.typesetting.installer.panes;

import com.keenwrite.io.UserDataDir;

/**
 * Responsible for downloading themes into the application's data directory.
 * The data directory differs between platforms, which is handled
 * transparently by the {@link UserDataDir} class.
 */
public class TypesetterThemesDownloadPane extends InstallerPane {
  public TypesetterThemesDownloadPane() {
  }

  @Override
  public String getHeaderKey() {
    return "NO KEY";
  }
}
