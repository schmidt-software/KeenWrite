/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.typesetting.installer.panes;

import com.keenwrite.ui.clipboard.SystemClipboard;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import static com.keenwrite.Messages.get;
import static com.keenwrite.Messages.getInt;
import static com.keenwrite.util.SystemUtils.IS_OS_MAC;
import static java.lang.String.format;

public final class UnixManagerInstallPane extends InstallerPane {
  private static final String PREFIX =
    "Wizard.typesetter.unix.2.install.container";

  private final TextArea mCommands = textArea( 2, 40 );

  public UnixManagerInstallPane() {
    final var titledPane = titledPane( "Run", mCommands );
    final var comboBox = createUnixOsCommandMap();
    final var selection = comboBox.getSelectionModel();
    selection
      .selectedItemProperty()
      .addListener( ( c, o, n ) -> mCommands.setText( n.command() ) );

    // Auto-select if running on macOS.
    if( IS_OS_MAC ) {
      final var items = comboBox.getItems();

      for( final var item : items ) {
        if( "macOS".equalsIgnoreCase( item.name ) ) {
          selection.select( item );
          break;
        }
      }
    }
    else {
      selection.select( 0 );
    }

    final var distro = label( PREFIX + ".os" );
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

    setContent( border );
  }

  @Override
  public Node createButtonBar() {
    final var node = super.createButtonBar();
    final var layout = new BorderPane();
    final var copyButton = button( STR."\{PREFIX}.copy.began" );

    // Change the label to indicate clipboard is updated.
    copyButton.setOnAction( _ -> {
      SystemClipboard.write( mCommands.getText() );
      copyButton.setText( get( STR."\{PREFIX}.copy.ended" ) );
    } );

    if( node instanceof ButtonBar buttonBar ) {
      copyButton.setMinWidth( buttonBar.getButtonMinWidth() );
    }

    layout.setPadding( new Insets( PAD, PAD, PAD, PAD ) );
    layout.setLeft( copyButton );
    layout.setRight( node );

    return layout;
  }

  @Override
  protected String getHeaderKey() {
    return STR."\{PREFIX}.header";
  }

  private record UnixOsCommand( String name, String command )
    implements Comparable<UnixOsCommand> {
    @Override
    public int compareTo( final UnixOsCommand other ) {
      return toString().compareToIgnoreCase( other.toString() );
    }

    @Override
    public String toString() {
      return name;
    }
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
    final var prefix = STR."\{PREFIX}.command";
    final var distros = getInt( STR."\{prefix}.distros", 14 );

    for( int i = 1; i <= distros; i++ ) {
      final var suffix = format( ".%02d", i );
      final var name = get( STR."\{prefix}.os.name\{suffix}" );
      final var command = get( STR."\{prefix}.os.text\{suffix}" );

      items.add( new UnixOsCommand( name, command ) );
    }

    items.sort( UnixOsCommand::compareTo );

    return comboBox;
  }
}
