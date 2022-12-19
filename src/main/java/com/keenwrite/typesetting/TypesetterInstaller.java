/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.typesetting;

import com.keenwrite.events.ExportFailedEvent;
import com.keenwrite.events.HyperlinkOpenEvent;
import com.keenwrite.typesetting.container.api.Container;
import com.keenwrite.typesetting.container.impl.Podman;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.SystemUtils;
import org.controlsfx.dialog.Wizard;
import org.controlsfx.dialog.WizardPane;
import org.greenrobot.eventbus.Subscribe;
import org.jetbrains.annotations.NotNull;

import java.io.PipedOutputStream;
import java.util.LinkedList;

import static com.keenwrite.Messages.get;
import static com.keenwrite.Messages.getInt;
import static com.keenwrite.constants.GraphicsConstants.ICON_DIALOG;
import static com.keenwrite.events.Bus.register;
import static java.lang.String.format;
import static javafx.event.ActionEvent.ACTION;

public class TypesetterInstaller {
  private static final int PAD = 10;

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

  private WizardPane[] wizardPanes() {
    final var panes = new LinkedList<WizardPane>();

    panes.add( createIntroductionPane() );

    if( SystemUtils.IS_OS_LINUX ) {
      panes.add( createContainerInstallPanelLinux() );
    }
    else if( SystemUtils.IS_OS_MAC ) {
      panes.add( createContainerInstallPanelMac() );
    }
    else if( SystemUtils.IS_OS_WINDOWS ) {
      panes.add( createContainerInstallPanelWindows() );
    }
    else {
      panes.add( createContainerInstallPanelUniversal() );
    }

    panes.add( createContainerInitializationPanel() );

    return panes.toArray( WizardPane[]::new );
  }

  private WizardPane createIntroductionPane() {
    final var pane = wizardPane(
      "Wizard.typesetter.all.1.install.header" );
    pane.setContent( new FlowPane(
      hyperlink(
        "Wizard.typesetter.all.1.install.about.container.link" ),
      label(
        "Wizard.typesetter.all.1.install.about.text.1" ),
      hyperlink(
        "Wizard.typesetter.all.1.install.about.typesetter.link" ),
      label(
        "Wizard.typesetter.all.1.install.about.text.2" )
    ) );

    return pane;
  }

  private WizardPane createContainerInstallPanelLinux() {
    final var commands = textArea( 2, 40 );
    final var titledPane = titledPane( "Run", commands );
    final var comboBox = createLinuxDistroCommands();
    final var selection = comboBox.getSelectionModel();
    selection
      .selectedItemProperty()
      .addListener( ( c, o, n ) -> commands.setText( n.command() ) );
    selection.select( 0 );

    final var distro = label(
      "Wizard.typesetter.linux.2.install.container.distro" );
    distro.setPadding( new Insets( PAD / 2.0, PAD, 0, 0 ) );

    final var hbox = new HBox();
    hbox.getChildren().add( distro );
    hbox.getChildren().add( comboBox );
    hbox.setPadding( new Insets( 0, 0, PAD, 0 ) );

    final var stepsPane = new VBox();
    final var steps = stepsPane.getChildren();
    steps.add( label( "Wizard.typesetter.linux.2.install.container.step.0" ) );
    steps.add( spacer() );
    steps.add( label( "Wizard.typesetter.linux.2.install.container.step.1" ) );
    steps.add( label( "Wizard.typesetter.linux.2.install.container.step.2" ) );
    steps.add( label( "Wizard.typesetter.linux.2.install.container.step.3" ) );
    steps.add( label( "Wizard.typesetter.linux.2.install.container.step.4" ) );
    steps.add( spacer() );

    final var border = new BorderPane();
    border.setTop( stepsPane );
    border.setCenter( hbox );
    border.setBottom( titledPane );

    final var pane = wizardPane(
      "Wizard.typesetter.linux.2.install.container.header" );
    pane.setContent( border );

    return pane;
  }

  private TextArea textArea( final int rows, final int cols ) {
    final var commands = new TextArea();
    commands.setEditable( false );
    commands.setPrefRowCount( rows );
    commands.setPrefColumnCount( cols );

    return commands;
  }

  private record LinuxDistro( String name, String command )
    implements Comparable<LinuxDistro> {
    @Override
    public int compareTo( @NotNull final LinuxDistro other ) {
      return toString().compareToIgnoreCase( other.toString() );
    }

    @Override
    public String toString() {
      return name;
    }
  }

  private ComboBox<LinuxDistro> createLinuxDistroCommands() {
    new ComboBox<LinuxDistro>();
    final var comboBox = new ComboBox<LinuxDistro>();
    final var items = comboBox.getItems();
    final var prefix = "Wizard.typesetter.linux.2.install.container.command";
    final var distros = getInt( prefix + ".distros", 13 );

    for( int i = 1; i <= distros; i++ ) {
      final var suffix = format( ".%02d", i );
      final var name = get( prefix + ".os.name" + suffix );
      final var command = get( prefix + ".os.text" + suffix );

      items.add( new LinuxDistro( name, command ) );
    }

    items.sort( LinuxDistro::compareTo );

    return comboBox;
  }

  private WizardPane createContainerInstallPanelMac() {
    final var pane = wizardPane(
      "Wizard.typesetter.mac.2.install.container.header" );

    return pane;
  }

  private WizardPane createContainerInstallPanelWindows() {
    final var pane = wizardPane(
      "Wizard.typesetter.win.2.install.container.header" );

    return pane;
  }

  private WizardPane createContainerInstallPanelUniversal() {
    final var pane = wizardPane(
      "Wizard.typesetter.win.2.install.container.header" );

    return pane;
  }

  private WizardPane createContainerInitializationPanel() {
    final var pane = wizardPane(
      "Wizard.typesetter.all.3.install.container.header" );

    final var textarea = textArea( 5, 50);
    final var titledPane = titledPane( "Output", textarea );

    final var initializeButton = new Button( "Initialize" );
    initializeButton.addEventFilter( ACTION, event -> {
      try {
        textarea.appendText( "INITIALIZING\n" );
        final Container container = new Podman();
        PipedOutputStream pos;
        container.initialize();
      } catch( final Exception e ) {
        e.printStackTrace();
      }
    } );

    final var borderPane = new BorderPane();
    borderPane.setTop( initializeButton );
    borderPane.setBottom( titledPane );

    pane.setContent( borderPane );

    return pane;
  }

  private WizardPane wizardPane( final String headerKey ) {
    final var imageView = new ImageView( ICON_DIALOG );
    final var headerText = get( headerKey );
    final var headerLabel = new Label( headerText );
    headerLabel.setScaleX( 1.25 );
    headerLabel.setScaleY( 1.25 );

    final var separator = new Separator();
    separator.setPadding( new Insets( PAD, 0, 0, 0 ) );

    final var borderPane = new BorderPane();
    borderPane.setCenter( headerLabel );
    borderPane.setRight( imageView );
    borderPane.setBottom( separator );
    borderPane.setPadding( new Insets( PAD, PAD, 0, PAD ) );

    final var wizardPane = new WizardPane();
    wizardPane.setHeader( borderPane );

    return wizardPane;
  }

  /**
   * Provides vertical spacing between {@link Node}s.
   *
   * @return A new empty vertical gap widget.
   */
  private Node spacer() {
    final var spacer = new FlowPane();
    spacer.setPadding( new Insets( PAD, 0, 0, 0 ) );

    return spacer;
  }

  private Label label( final String key ) {
    final var text = get( key );
    return new Label( text );
  }

  private Hyperlink hyperlink( final String prefix ) {
    final var label = get( prefix + ".lbl" );
    final var url = get( prefix + ".url" );
    final var link = new Hyperlink( label );

    link.setOnAction( e -> browse( url ) );
    link.setTooltip( new Tooltip( url ) );

    return link;
  }

  private TitledPane titledPane( final String key, final Node child ) {
    final var pane = new TitledPane( "Initialization", child );
    pane.setCollapsible( false );

    return pane;
  }

  /**
   * Opens a browser window off of the JavaFX main execution thread. This
   * is necessary so that the links open immediately, instead of being blocked
   * by any modal dialog (i.e., the {@link Wizard} instance).
   *
   * @param property The property key name associated with a hyperlink URL.
   */
  private void browse( final String property ) {
    final var url = get( property );
    new Thread( () -> HyperlinkOpenEvent.fire( url ) ).start();
  }
}
