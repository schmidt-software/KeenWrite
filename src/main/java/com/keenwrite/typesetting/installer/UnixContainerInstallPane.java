/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.typesetting.installer;

import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;

import static com.keenwrite.Messages.get;
import static com.keenwrite.Messages.getInt;
import static com.keenwrite.typesetting.installer.InstallPane.*;
import static com.keenwrite.typesetting.installer.WizardConstants.PAD;
import static java.lang.String.format;
import static org.apache.commons.lang3.SystemUtils.IS_OS_MAC;

public final class UnixContainerInstallPane {
  private static final String PREFIX =
    "Wizard.typesetter.unix.2.install.container";

  private record UnixOsCommand( String name, String command )
    implements Comparable<UnixOsCommand> {
    @Override
    public int compareTo(
      final @NotNull UnixOsCommand other ) {
      return toString().compareToIgnoreCase( other.toString() );
    }

    @Override
    public String toString() {
      return name;
    }
  }

  static InstallPane create() {
    final var commands = textArea( 2, 40 );
    final var titledPane = titledPane( "Run", commands );
    final var comboBox = createUnixOsCommandMap();
    final var selection = comboBox.getSelectionModel();
    selection
      .selectedItemProperty()
      .addListener( ( c, o, n ) -> commands.setText( n.command() ) );

    if( IS_OS_MAC ) {
      final var items = comboBox.getItems();
      for( final var item : items ) {
        if( "MacOS".equalsIgnoreCase( item.name ) ) {
          selection.select( item );
          break;
        }
      }
    }
    else {
      selection.select( 0 );
    }

    final var distro = label(
      PREFIX + ".os" );
    distro.setText( distro.getText() + ":" );
    distro.setPadding( new Insets( PAD / 2.0, PAD, 0, 0 ) );

    final var hbox = new HBox();
    hbox.getChildren().add( distro );
    hbox.getChildren().add( comboBox );
    hbox.setPadding( new Insets( 0, 0, PAD, 0 ) );

    final var stepsPane = new VBox();
    final var steps = stepsPane.getChildren();
    steps.add( label( PREFIX + ".step.0" ) );
    steps.add( spacer() );
    steps.add( label( PREFIX + ".step.1" ) );
    steps.add( label( PREFIX + ".step.2" ) );
    steps.add( label( PREFIX + ".step.3" ) );
    steps.add( label( PREFIX + ".step.4" ) );
    steps.add( spacer() );

    steps.add( flowPane(
      label( PREFIX + ".details.prefix" ),
      hyperlink( PREFIX + ".details.link" ),
      label( PREFIX + ".details.suffix" )
    ) );
    steps.add( spacer() );

    final var border = new BorderPane();
    border.setTop( stepsPane );
    border.setCenter( hbox );
    border.setBottom( titledPane );

    final var pane = wizardPane(
      PREFIX + ".header" );
    pane.setContent( border );

    return pane;
  }

  /**
   * Creates a collection of *nix distributions mapped to instructions for users
   * to run in a terminal.
   *
   * @return A map of *nix to instructions.
   */
  private static ComboBox<UnixOsCommand> createUnixOsCommandMap() {
    new ComboBox<UnixOsCommand>();
    final var comboBox = new ComboBox<UnixOsCommand>();
    final var items = comboBox.getItems();
    final var prefix = PREFIX + ".command";
    final var distros = getInt( prefix + ".distros", 14 );

    for( int i = 1; i <= distros; i++ ) {
      final var suffix = format( ".%02d", i );
      final var name = get( prefix + ".os.name" + suffix );
      final var command = get( prefix + ".os.text" + suffix );

      items.add( new UnixOsCommand( name, command ) );
    }

    items.sort( UnixOsCommand::compareTo );

    return comboBox;
  }
}
