/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.typesetting.installer.panes;

import com.keenwrite.typesetting.containerization.ContainerManager;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.controlsfx.dialog.Wizard;

import java.io.File;

import static com.keenwrite.Messages.get;

/**
 * Responsible for installing the container manager on Windows.
 */
public final class WindowsManagerInstallPane extends InstallerPane {
  /**
   * Property for the installation thread to help with reentrancy.
   */
  private static final String WIN_INSTALLER = "windows.container.installer";

  /**
   * Shared property to track name of container manager binary file.
   */
  static final String WIN_BIN = "windows.container.binary";

  private static final String PREFIX =
    "Wizard.typesetter.win.2.install.container";

  private final ContainerManager mContainer;
  private final TextArea mCommands;

  public WindowsManagerInstallPane() {
    mCommands = textArea( 2, 55 );

    final var titledPane = titledPane( "Output", mCommands );
    append( mCommands, get( PREFIX + ".status.running" ) );

    final var stepsPane = new VBox();
    final var steps = stepsPane.getChildren();
    steps.add( label( PREFIX + ".step.0" ) );
    steps.add( spacer() );
    steps.add( label( PREFIX + ".step.1" ) );
    steps.add( label( PREFIX + ".step.2" ) );
    steps.add( label( PREFIX + ".step.3" ) );
    steps.add( spacer() );
    steps.add( titledPane );

    final var border = new BorderPane();
    border.setTop( stepsPane );

    mContainer = createContainer();
  }

  @Override
  public void onEnteringPage( final Wizard wizard ) {
    disableNext( true );

    // Pull the fully qualified installer path from the properties.
    final var properties = wizard.getProperties();
    final var thread = properties.get( WIN_INSTALLER );

    if( thread instanceof Thread installer && installer.isAlive() ) {
      return;
    }

    final var binary = properties.get( WIN_BIN );
    final var key = PREFIX + ".status";

    if( binary instanceof File exe ) {
      final var task = createTask( () -> {
        final var exit = mContainer.install( exe );

        // Remove the installer after installation is finished.
        properties.remove( thread );

        final var msg = exit == 0
          ? get( key + ".success" )
          : get( key + ".failure", exit );

        append( mCommands, msg );
        disableNext( exit != 0 );

        return null;
      } );

      final var installer = createThread( task );
      properties.put( WIN_INSTALLER, installer );
      installer.start();
    }
    else {
      append( mCommands, get( PREFIX + ".unknown", binary ) );
    }
  }

  @Override
  public String getHeaderKey() {
    return PREFIX + ".header";
  }
}
