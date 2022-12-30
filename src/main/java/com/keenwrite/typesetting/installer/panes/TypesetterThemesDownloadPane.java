/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.typesetting.installer.panes;

import com.keenwrite.io.UserDataDir;
import com.keenwrite.io.Zip;
import com.keenwrite.preferences.Workspace;
import javafx.collections.ObservableMap;
import org.controlsfx.dialog.Wizard;

import static com.keenwrite.Messages.get;
import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.preferences.AppKeys.KEY_TYPESET_CONTEXT_THEMES_PATH;

/**
 * Responsible for downloading themes into the application's data directory.
 * The data directory differs between platforms, which is handled
 * transparently by the {@link UserDataDir} class.
 */
public class TypesetterThemesDownloadPane extends AbstractDownloadPane {
  private static final String PREFIX =
    "Wizard.typesetter.all.5.download.themes";

  private final Workspace mWorkspace;

  public TypesetterThemesDownloadPane( final Workspace workspace ) {
    assert workspace != null;
    mWorkspace = workspace;
  }

  @Override
  public void onEnteringPage( final Wizard wizard ) {
    // Delete the target themes file to force re-download so that unzipping
    // the file takes place. This side-steps checksum validation, which would
    // be best added after downloading.
    deleteTarget();
    super.onEnteringPage( wizard );
  }

  @Override
  protected void onDownloadSucceeded(
    final String threadName, final ObservableMap<Object, Object> properties ) {
    super.onDownloadSucceeded( threadName, properties );
    final var target = getTarget();

    try {
      Zip.extract( target.toPath() );

      // Replace the default themes directory with the downloaded version.
      final var root = Zip.root( target.toPath() ).toFile();
      mWorkspace.fileProperty( KEY_TYPESET_CONTEXT_THEMES_PATH ).set( root );
      mWorkspace.save();
      deleteTarget();
    } catch( final Exception ex ) {
      clue( ex );
    }
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
