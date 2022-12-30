/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.typesetting.installer.panes;

import com.keenwrite.io.SysFile;
import com.keenwrite.io.UserDataDir;
import javafx.collections.ObservableMap;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import org.controlsfx.dialog.Wizard;

import java.io.File;
import java.net.URI;

import static com.keenwrite.Bootstrap.APP_TITLE;
import static com.keenwrite.Messages.get;
import static com.keenwrite.Messages.getUri;

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
    mUri = getUri( getPrefix() + ".download.link.url" );
    mFilename = toFilename( mUri );
    final var directory = UserDataDir.getAppPath( APP_TITLE.toLowerCase() );
    mTarget = directory.resolve( mFilename ).toFile();
    final var source = labelf( getPrefix() + ".paths", mFilename, directory );
    mStatus = labelf( getPrefix() + STATUS + ".progress", 0, 0 );

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
      return;
    }

    updateProperties( properties );

    final var target = getTarget();
    final var sysFile = new SysFile( target );
    final var checksum = getChecksum();

    if( sysFile.exists() ) {
      final var checksumOk = sysFile.isChecksum( checksum );
      final var suffix = checksumOk ? ".ok" : ".no";

      updateStatus( STATUS + ".checksum" + suffix, mFilename );
      disableNext( !checksumOk );
    }
    else {
      final var task = downloadAsync( mUri, target, ( progress, bytes ) -> {
        final var suffix = progress < 0 ? ".bytes" : ".progress";

        updateStatus( STATUS + suffix, progress, bytes );
      } );

      properties.put( threadName, task );

      task.setOnSucceeded( e -> onDownloadSucceeded( threadName, properties ) );
      task.setOnFailed( e -> onDownloadFailed( threadName, properties ) );
      task.setOnCancelled( e -> onDownloadFailed( threadName, properties ) );
    }
  }

  protected void updateProperties(
    final ObservableMap<Object, Object> properties ) {
  }

  @Override
  protected String getHeaderKey() {
    return getPrefix() + ".header";
  }

  protected File getTarget() {
    return mTarget;
  }

  protected abstract String getChecksum();

  protected abstract String getPrefix();

  protected void onDownloadSucceeded(
    final String threadName, final ObservableMap<Object, Object> properties ) {
    updateStatus( STATUS + ".success" );
    properties.remove( threadName );
    disableNext( false );
  }

  protected void onDownloadFailed(
    final String threadName, final ObservableMap<Object, Object> properties ) {
    updateStatus( STATUS + ".failure" );
    properties.remove( threadName );
  }

  protected void updateStatus( final String suffix, final Object... args ) {
    update( mStatus, get( getPrefix() + suffix, args ) );
  }

  protected void deleteTarget() {
    final var ignored = getTarget().delete();
  }
}
