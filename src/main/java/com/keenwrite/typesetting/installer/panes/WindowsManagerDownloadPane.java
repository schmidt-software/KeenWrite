/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.typesetting.installer.panes;

import javafx.collections.ObservableMap;

import static com.keenwrite.Messages.get;
import static com.keenwrite.typesetting.installer.panes.WindowsManagerInstallPane.WIN_BIN;

/**
 * Responsible for downloading the container manager software on Windows.
 */
public final class WindowsManagerDownloadPane extends AbstractDownloadPane {
  private static final String PREFIX =
    "Wizard.typesetter.win.2.download.container";

  @Override
  protected void updateProperties(
    final ObservableMap<Object, Object> properties ) {
    properties.put( WIN_BIN, getTarget() );
  }

  @Override
  protected String getPrefix() {
    return PREFIX;
  }

  @Override
  protected String getChecksum() {
    return get( "Wizard.typesetter.container.checksum" );
  }
}
