/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.typesetting.installer;

import com.keenwrite.events.ExportFailedEvent;
import com.keenwrite.io.CommandNotFoundException;
import com.keenwrite.typesetting.container.api.Container;
import com.keenwrite.typesetting.container.impl.Podman;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import org.apache.commons.lang3.function.FailableConsumer;
import org.controlsfx.dialog.Wizard;
import org.greenrobot.eventbus.Subscribe;

import java.util.LinkedList;

import static com.keenwrite.Messages.get;
import static com.keenwrite.events.Bus.register;
import static com.keenwrite.typesetting.installer.InstallPane.*;
import static java.lang.System.lineSeparator;
import static javafx.application.Platform.runLater;
import static org.apache.commons.lang3.SystemUtils.*;

public final class TypesetterInstaller {
  /**
   * All except for Linux.
   */
  static final String ALL_INITIALIZER = "all.container.initializer";
  static final String WIN_BIN = "windows.container.binary";
  static final String WIN_INSTALLER = "windows.container.installer";
  static final String WIN_DOWNLOADER = "windows.container.downloader";

  public TypesetterInstaller() {
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

  private InstallPane[] wizardPanes() {
    final var panes = new LinkedList<InstallPane>();

    // STEP 1: Introduction panel (all)
    panes.add( IntroductionPane.create() );

    if( IS_OS_WINDOWS ) {
      // STEP 2 a: Download container (Windows)
      panes.add( WindowsContainerDownloadPane.create() );
      // STEP 2 b: Install container (Windows)
      panes.add( WindowsContainerInstallPane.create() );
    }
    else if( IS_OS_UNIX ) {
      // STEP 2: Install container (Unix)
      panes.add( UnixContainerInstallPane.create() );
    }
    else {
      // STEP 2: Install container (other)
      panes.add( UniversalContainerInstallPane.create() );
    }

    if( !IS_OS_LINUX ) {
      // STEP 3: Initialize container (all except Linux)
      panes.add( CommonContainerInitializationPane.create() );
    }

    // STEP 4: Install typesetter container image (all)
    panes.add( TypesetterImageDownloadPanel.create() );

    return panes.toArray( InstallPane[]::new );
  }

  public static InstallPane createContainerOutputPanel(
    final String headerKey,
    final String correctKey,
    final String missingKey,
    final FailableConsumer<Container, CommandNotFoundException> fc,
    final int cols
  ) {
    final var textarea = textArea( 5, cols );
    final var titledPane = titledPane( "Output", textarea );
    final var borderPane = new BorderPane();
    borderPane.setBottom( titledPane );

    final var container = createContainer( textarea );

    final var pane = wizardPane(
      headerKey,
      ( wizard, self ) -> {
        self.disableNext( true );

        try {
          final var properties = wizard.getProperties();
          final var thread = properties.get( ALL_INITIALIZER );

          if( thread instanceof Thread initializer && initializer.isAlive() ) {
            return;
          }

          final var task = createTask( () -> {
            fc.accept( container );
            properties.remove( thread );
            return null;
          } );

          task.setOnSucceeded( event -> {
            append( textarea, get( correctKey ) );
            self.disableNext( false );
          } );
          task.setOnFailed( event -> append( textarea, get( missingKey ) ) );
          task.setOnCancelled( event -> append( textarea, get( missingKey ) ) );

          final var initializer = createThread( task );
          properties.put( ALL_INITIALIZER, initializer );
          initializer.start();
        } catch( final Exception e ) {
          throw new RuntimeException( e );
        }
      } );
    pane.setContent( borderPane );

    return pane;
  }

  /**
   * Creates a container that can have its standard output read as an input
   * stream that's piped directly to a {@link TextArea}.
   *
   * @param textarea The {@link TextArea} to receive text.
   * @return An object that can perform tasks against a container.
   */
  public static Container createContainer( final TextArea textarea ) {
    return new Podman( text -> append( textarea, text ) );
  }

  public static void update( final Label node, final String text ) {
    runLater( () -> node.setText( text ) );
  }

  public static void append( final TextArea node, final String text ) {
    runLater( () -> {
      node.appendText( text );
      node.appendText( lineSeparator() );
    } );
  }
}
