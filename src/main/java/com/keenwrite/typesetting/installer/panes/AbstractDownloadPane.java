/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.typesetting.installer.panes;

import com.keenwrite.io.SysFile;
import javafx.collections.ObservableMap;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import org.controlsfx.dialog.Wizard;

import java.io.File;
import java.net.URI;

import static com.keenwrite.Bootstrap.USER_DATA_DIR;
import static com.keenwrite.Messages.get;
import static com.keenwrite.Messages.getUri;
import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.io.SysFile.toFile;
import static com.keenwrite.io.downloads.DownloadManager.downloadAsync;
import static com.keenwrite.io.downloads.DownloadManager.toFilename;

/**
 * Responsible for asynchronous downloads.
 */
public abstract class AbstractDownloadPane extends InstallerPane {
  private static final String STATUS = ".status";

  private final Label mStatus;
  private final File mTarget;
  private final String mFilename;
  private final URI mUri;

  public AbstractDownloadPane() {
    mUri = getUri( STR."\{getPrefix()}.download.link.url" );
    mFilename = toFilename( mUri );
    final var directory = USER_DATA_DIR;
    mTarget = toFile( directory.resolve( mFilename ) );
    final var source = labelf( STR."\{getPrefix()}.paths", mFilename, directory );
    mStatus = labelf( STR."\{getPrefix()}\{STATUS}.progress", 0, 0 );

    final var border = new BorderPane();
    border.setTop( source );
    border.setCenter( spacer() );
    border.setBottom( mStatus );

    setContent( border );
  }

  @Override
  public void onEnteringPage( final Wizard wizard ) {
    disableNext( true );

    final var threadName = getClass().getCanonicalName();
    final var properties = wizard.getProperties();
    final var thread = properties.get( threadName );

    if( thread instanceof Task<?> downloader && downloader.isRunning() ) {
      clue( "Wizard.container.install.download.running" );
      return;
    }

    updateProperties( properties );

    final var target = getTarget();
    final var sysFile = new SysFile( target );
    final var checksum = getChecksum();

    if( sysFile.exists() ) {
      final var checksumOk = sysFile.isChecksum( checksum );
      final var suffix = checksumOk ? ".ok" : ".no";

      updateStatus( STR."\{STATUS}.checksum\{suffix}", mFilename );
      disableNext( !checksumOk );
    }
    else {
      clue( "Wizard.container.install.download.started", mUri );

      final var task = downloadAsync( mUri, target, ( progress, bytes ) -> {
        final var suffix = progress < 0 ? ".bytes" : ".progress";

        updateStatus( STATUS + suffix, progress, bytes );
      } );

      properties.put( threadName, task );

      task.setOnSucceeded( _ -> onDownloadSucceeded( threadName, properties ) );
      task.setOnFailed( _ -> onDownloadFailed( threadName, properties ) );
      task.setOnCancelled( _ -> onDownloadFailed( threadName, properties ) );
    }
  }

  protected void updateProperties(
    final ObservableMap<Object, Object> properties ) {
  }

  @Override
  protected String getHeaderKey() {
    return STR."\{getPrefix()}.header";
  }

  protected File getTarget() {
    return mTarget;
  }

  protected abstract String getChecksum();

  protected abstract String getPrefix();

  protected void onDownloadSucceeded(
    final String threadName, final ObservableMap<Object, Object> properties ) {
    updateStatus( STR."\{STATUS}.success" );
    properties.remove( threadName );
    disableNext( false );
  }

  protected void onDownloadFailed(
    final String threadName, final ObservableMap<Object, Object> properties ) {
    updateStatus( STR."\{STATUS}.failure" );
    properties.remove( threadName );
  }

  protected void updateStatus( final String suffix, final Object... args ) {
    update( mStatus, get( getPrefix() + suffix, args ) );
  }

  protected void deleteTarget() {
    if( !getTarget().delete() ) {
      clue( "Main.status.error.file.delete", getTarget() );
    }
  }
}
