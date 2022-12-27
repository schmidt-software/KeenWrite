/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.typesetting.installer;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.io.File;

import static com.keenwrite.Messages.get;
import static com.keenwrite.typesetting.installer.InstallPane.*;
import static com.keenwrite.typesetting.installer.TypesetterInstaller.*;

public final  class WindowsContainerInstallPane {
  public static InstallPane create() {
    final var prefix = "Wizard.typesetter.win.2.install.container";

    final var commands = textArea( 2, 55 );
    final var titledPane = titledPane( "Output", commands );
    append( commands, get( prefix + ".status.running" ) );

    final var container = createContainer( commands );
    final var stepsPane = new VBox();
    final var steps = stepsPane.getChildren();
    steps.add( label( prefix + ".step.0" ) );
    steps.add( spacer() );
    steps.add( label( prefix + ".step.1" ) );
    steps.add( label( prefix + ".step.2" ) );
    steps.add( label( prefix + ".step.3" ) );
    steps.add( spacer() );
    steps.add( titledPane );

    final var border = new BorderPane();
    border.setTop( stepsPane );

    final var pane = wizardPane(
      prefix + ".header",
      ( wizard, self ) -> {
        self.disableNext( true );

        // Pull the fully qualified installer path from the properties.
        final var properties = wizard.getProperties();
        final var thread = properties.get( WIN_INSTALLER );

        if( thread instanceof Thread installer && installer.isAlive() ) {
          return;
        }

        final var binary = properties.get( WIN_BIN );
        final var key = prefix + ".status";

        if( binary instanceof File exe ) {
          final var task = createTask( () -> {
            final var exit = container.install( exe );

            // Remove the installer after installation is finished.
            properties.remove( thread );

            final var msg = exit == 0
              ? get( key + ".success" )
              : get( key + ".failure", exit );

            append( commands, msg );
            self.disableNext( exit != 0 );

            return null;
          } );

          final var installer = createThread( task );
          properties.put( WIN_INSTALLER, installer );
          installer.start();
        }
        else {
          append( commands, get( prefix + ".unknown", binary ) );
        }
      } );
    pane.setContent( border );

    return pane;
  }
}
