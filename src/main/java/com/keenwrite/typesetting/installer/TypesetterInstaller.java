/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.typesetting.installer;

import com.keenwrite.events.ExportFailedEvent;
import com.keenwrite.preferences.Workspace;
import com.keenwrite.typesetting.installer.panes.*;
import org.controlsfx.dialog.Wizard;
import org.greenrobot.eventbus.Subscribe;

import java.util.LinkedList;

import static com.keenwrite.Messages.get;
import static com.keenwrite.events.Bus.register;
import static org.apache.commons.lang3.SystemUtils.*;

/**
 * Responsible for installing the typesetting system and all its requirements.
 */
public final class TypesetterInstaller {
  private final Workspace mWorkspace;

  public TypesetterInstaller( final Workspace workspace ) {
    assert workspace != null;

    mWorkspace = workspace;

    register( this );
  }

  @Subscribe
  @SuppressWarnings( "unused" )
  public void handle( final ExportFailedEvent failedEvent ) {
    final var wizard = wizard();

    wizard.showAndWait();
  }

  private Wizard wizard() {
    final var title = get( "Wizard.typesetter.all.1.install.title" );
    final var wizard = new Wizard( this, title );
    final var wizardFlow = wizardFlow();

    wizard.setFlow( wizardFlow );

    return wizard;
  }

  private Wizard.Flow wizardFlow() {
    final var panels = wizardPanes();
    return new Wizard.LinearFlow( panels );
  }

  private InstallerPane[] wizardPanes() {
    final var panes = new LinkedList<InstallerPane>();

    // STEP 1: Introduction panel (all)
    panes.add( new IntroductionPane() );

    if( IS_OS_WINDOWS ) {
      // STEP 2 a: Download container (Windows)
      panes.add( new WindowsManagerDownloadPane() );
      // STEP 2 b: Install container (Windows)
      panes.add( new WindowsManagerInstallPane() );
    }
    else if( IS_OS_UNIX ) {
      // STEP 2: Install container (Unix)
      panes.add( new UnixManagerInstallPane() );
    }
    else {
      // STEP 2: Install container (other)
      panes.add( new UniversalManagerInstallPane() );
    }

    if( !IS_OS_LINUX ) {
      // STEP 3: Initialize container (all except Linux)
      panes.add( new ManagerInitializationPane() );
    }

    // STEP 4: Install typesetter container image (all)
    panes.add( new TypesetterImageDownloadPane() );

    // STEP 5: Download and install typesetter themes (all)
    panes.add( new TypesetterThemesDownloadPane( mWorkspace ) );

    return panes.toArray( InstallerPane[]::new );
  }
}
