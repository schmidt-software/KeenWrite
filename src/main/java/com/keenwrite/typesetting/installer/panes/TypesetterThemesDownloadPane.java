/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.typesetting.installer.panes;

import com.keenwrite.io.UserDataDir;
import javafx.collections.ObservableMap;

import static com.keenwrite.Messages.get;

/**
 * Responsible for downloading themes into the application's data directory.
 * The data directory differs between platforms, which is handled
 * transparently by the {@link UserDataDir} class.
 */
public class TypesetterThemesDownloadPane extends AbstractDownloadPane {
  private static final String PREFIX =
    "Wizard.typesetter.all.5.download.themes";

  @Override
  protected void onDownloadSucceeded(
    final String threadName, final ObservableMap<Object, Object> properties ) {
    super.onDownloadSucceeded( threadName, properties );

    final var target = getTarget();
  }

  @Override
  protected String getPrefix() {
    return PREFIX;
  }

  @Override
  protected String getChecksum() {
    return get( "Wizard.typesetter.themes.checksum" );
  }
}
