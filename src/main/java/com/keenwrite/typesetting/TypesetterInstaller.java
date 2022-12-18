/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.typesetting;

import com.keenwrite.events.ExportFailedEvent;
import com.keenwrite.events.HyperlinkOpenEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.apache.commons.lang3.SystemUtils;
import org.controlsfx.dialog.Wizard;
import org.controlsfx.dialog.WizardPane;
import org.greenrobot.eventbus.Subscribe;
import org.jetbrains.annotations.NotNull;

import static com.keenwrite.Messages.get;
import static com.keenwrite.Messages.getInt;
import static com.keenwrite.constants.GraphicsConstants.ICON_DIALOG;
import static com.keenwrite.events.Bus.register;
import static java.lang.String.format;
import static javafx.application.Platform.runLater;

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
    final var panes = new WizardPane[ 2 ];

    panes[ 0 ] = createIntroductionPane();

    if( SystemUtils.IS_OS_LINUX ) {
      panes[ 1 ] = createContainerInstallPanelLinux();
    }
    else if( SystemUtils.IS_OS_MAC ) {
      panes[ 1 ] = createContainerInstallPanelMac();
    }
    else if( SystemUtils.IS_OS_WINDOWS ) {
      panes[ 1 ] = createContainerInstallPanelWindows();
    }

    return panes;
  }

  private WizardPane createIntroductionPane() {
    final var pane = wizardPane(
      "Wizard.typesetter.all.1.install.header" );
    final var containerName = get(
      "Wizard.typesetter.all.1.install.about.container.name" );
    final var introText1 = get(
      "Wizard.typesetter.all.1.install.about.text.1" );
    final var typesetterName = get(
      "Wizard.typesetter.all.1.install.about.typesetter.name" );
    final var introText2 = get(
      "Wizard.typesetter.all.1.install.about.text.2" );

    final var flowPane = new FlowPane();
    final var containerLink = hyperlink(
      "Wizard.typesetter.all.1.install.about.container.link" );
    final var introText1Label = new Label( introText1 );
    final var typesetterLink = hyperlink(
      "Wizard.typesetter.all.1.install.about.typesetter.link" );
    final var introText2Label = new Label( introText2 );

    flowPane.getChildren().addAll(
      containerLink, introText1Label, typesetterLink, introText2Label );

    pane.setContent( flowPane );

    return pane;
  }

  private WizardPane createContainerInstallPanelLinux() {
    final var commands = new TextArea();
    commands.setEditable( false );
    commands.setPrefRowCount( 2 );
    commands.setPrefColumnCount( 40 );

    final var titledPane = new TitledPane( "Run", commands );
    titledPane.setCollapsible( false );

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

    final var vbox = new VBox();
    final var steps = vbox.getChildren();
    steps.add( label( "Wizard.typesetter.linux.2.install.container.step.0" ) );
    steps.add( spacer() );
    steps.add( label( "Wizard.typesetter.linux.2.install.container.step.1" ) );
    steps.add( label( "Wizard.typesetter.linux.2.install.container.step.2" ) );
    steps.add( label( "Wizard.typesetter.linux.2.install.container.step.3" ) );
    steps.add( label( "Wizard.typesetter.linux.2.install.container.step.4" ) );
    steps.add( spacer() );

    final var border = new BorderPane();
    border.setTop( vbox );
    border.setCenter( hbox );
    border.setBottom( titledPane );

    final var pane = wizardPane(
      "Wizard.typesetter.linux.2.install.container.header" );
    pane.setContent( border );

    return pane;
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
    final var distros = getInt(
      "Wizard.typesetter.linux.2.install.container.command.distros", 13 );
    final var prefix = "Wizard.typesetter.linux.2.install.container.command.os";

    for( int i = 1; i <= distros; i++ ) {
      final var suffix = format( ".%02d", i );
      final var name = get( prefix + ".name" + suffix );
      final var command = get( prefix + ".text" + suffix );

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

  private Node spacer() {
    final var spacer = new Region();
    spacer.setPrefHeight( PAD );

    return spacer;
  }

  private Label label( final String key ) {
    final var text = get( key );
    return new Label( text );
  }

  private Hyperlink hyperlink( final String prefx ) {
    final var label = get( prefx + ".lbl" );
    final var url = get( prefx + ".url" );
    final var link = new Hyperlink( label );

    link.setOnAction( e -> browse( url ) );
    link.setTooltip( new Tooltip( url ) );

    return link;
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
