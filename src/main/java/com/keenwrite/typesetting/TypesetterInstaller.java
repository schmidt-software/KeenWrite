/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.typesetting;

import com.keenwrite.events.ExportFailedEvent;
import com.keenwrite.events.HyperlinkOpenEvent;
import com.keenwrite.io.downloads.DownloadManager;
import com.keenwrite.io.downloads.DownloadManager.ProgressListener;
import com.keenwrite.typesetting.container.api.Container;
import com.keenwrite.typesetting.container.impl.Podman;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.function.FailableConsumer;
import org.controlsfx.dialog.Wizard;
import org.controlsfx.dialog.WizardPane;
import org.greenrobot.eventbus.Subscribe;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.function.Consumer;

import static com.keenwrite.Bootstrap.APP_VERSION_CLEAN;
import static com.keenwrite.Messages.get;
import static com.keenwrite.Messages.getInt;
import static com.keenwrite.constants.GraphicsConstants.ICON_DIALOG;
import static com.keenwrite.events.Bus.register;
import static java.lang.String.format;
import static java.lang.System.lineSeparator;
import static javafx.application.Platform.runLater;
import static org.apache.commons.lang3.SystemUtils.*;

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

    // STEP 1: Introduction panel (all)
    panes.add( createIntroductionPane() );

    if( true || IS_OS_WINDOWS ) {
      // STEP 2 a: Download container (Windows)
      panes.add( createContainerDownloadPanelWindows() );
      // STEP 2 b: Install container (Windows)
      panes.add( createContainerInstallPanelWindows() );
    }
    else if( IS_OS_UNIX ) {
      // STEP 2: Install container (Unix)
      panes.add( createContainerInstallPanelUnix() );
    }
    else {
      // STEP 2: Install container (other)
      panes.add( createContainerInstallPanelUniversal() );
    }

    if( true || !IS_OS_LINUX ) {
      // STEP 3: Initialize container (all except Linux)
      panes.add( createContainerInitializationPanel() );
    }

    // STEP 4: Install typesetter container image (all)
    panes.add( createContainerImageDownloadPanel() );

    return panes.toArray( WizardPane[]::new );
  }

  /**
   * STEP 1: Introduction panel (all)
   */
  private WizardPane createIntroductionPane() {
    final var pane = wizardPane(
      "Wizard.typesetter.all.1.install.header" );
    pane.setContent( flowPane(
      hyperlink( "Wizard.typesetter.all.1.install.about.container.link" ),
      label( "Wizard.typesetter.all.1.install.about.text.1" ),
      hyperlink( "Wizard.typesetter.all.1.install.about.typesetter.link" ),
      label( "Wizard.typesetter.all.1.install.about.text.2" )
    ) );

    return pane;
  }

  /**
   * STEP 2: Install container (Unix)
   */
  private WizardPane createContainerInstallPanelUnix() {
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
      "Wizard.typesetter.unix.2.install.container.os" );
    distro.setText( distro.getText() + ":" );
    distro.setPadding( new Insets( PAD / 2.0, PAD, 0, 0 ) );

    final var hbox = new HBox();
    hbox.getChildren().add( distro );
    hbox.getChildren().add( comboBox );
    hbox.setPadding( new Insets( 0, 0, PAD, 0 ) );

    final var stepsPane = new VBox();
    final var steps = stepsPane.getChildren();
    steps.add( label( "Wizard.typesetter.unix.2.install.container.step.0" ) );
    steps.add( spacer() );
    steps.add( label( "Wizard.typesetter.unix.2.install.container.step.1" ) );
    steps.add( label( "Wizard.typesetter.unix.2.install.container.step.2" ) );
    steps.add( label( "Wizard.typesetter.unix.2.install.container.step.3" ) );
    steps.add( label( "Wizard.typesetter.unix.2.install.container.step.4" ) );
    steps.add( spacer() );

    steps.add( flowPane(
      label( "Wizard.typesetter.unix.2.install.container.details.prefix" ),
      hyperlink( "Wizard.typesetter.unix.2.install.container.details.link" ),
      label( "Wizard.typesetter.unix.2.install.container.details.suffix" )
    ) );
    steps.add( spacer() );

    final var border = new BorderPane();
    border.setTop( stepsPane );
    border.setCenter( hbox );
    border.setBottom( titledPane );

    final var pane = wizardPane(
      "Wizard.typesetter.unix.2.install.container.header" );
    pane.setContent( border );

    return pane;
  }

  /**
   * STEP 2 a: Download container (Windows)
   */
  private WizardPane createContainerDownloadPanelWindows() {
    final var prefix = "Wizard.typesetter.win.2.download.container";

    final var binary = get( prefix + ".download.link.url" );
    final var uri = URI.create( binary );
    final var file = Paths.get( uri.getPath() ).toFile();
    final var filename = file.getName();
    final var directory = Path.of( System.getProperty( "user.dir" ) );
    final var target = directory.resolve( filename ).toFile();
    final var source = labelf( prefix + ".paths", filename, directory );
    final var status = labelf( prefix + ".status.progress", 0, 0 );

    target.deleteOnExit();

    final var border = new BorderPane();
    border.setTop( source );
    border.setCenter( spacer() );
    border.setBottom( status );

    final var pane = wizardPane(
      prefix + ".header",
      wizard -> {
        if( !target.exists() ) {
          download( uri, target, ( progress, total ) -> {
            final var msg = progress < 0
              ? get( prefix + ".status.bytes", total )
              : get( prefix + ".status.progress", progress, total );

            runLater( () -> status.setText( msg ) );
          } );
        }
        else {
          final var msg = get( prefix + ".status.exists", filename );

          runLater( () -> status.setText( msg ) );
        }
      }
    );
    pane.setContent( border );

    return pane;
  }

  /**
   * STEP 2 b: Install container (Windows)
   */
  private WizardPane createContainerInstallPanelWindows() {
    final var pane = wizardPane(
      "Wizard.typesetter.win.2.install.container.header" );

    return pane;
  }

  /**
   * STEP 2: Install container (other)
   */
  private WizardPane createContainerInstallPanelUniversal() {
    final var pane = wizardPane(
      "Wizard.typesetter.all.2.install.container.header" );

//    Wizard.typesetter.all.2.install.container.homepage.lbl=${Wizard
//    .typesetter.container.name}
//    Wizard.typesetter.all.2.install.container.homepage.url=https://podman.io


    return pane;
  }

  /**
   * STEP 3: Initialize container (all except Linux)
   */
  private WizardPane createContainerInitializationPanel() {
    return createContainerOutputPanel(
      "Wizard.typesetter.all.3.install.container.header",
      "Wizard.typesetter.all.3.install.container.correct",
      "Wizard.typesetter.all.3.install.container.missing",
      Container::start,
      35
    );
  }

  /**
   * STEP 4: Install typesetter container image (all)
   */
  private WizardPane createContainerImageDownloadPanel() {
    return createContainerOutputPanel(
      "Wizard.typesetter.all.4.download.typesetter.header",
      "Wizard.typesetter.all.4.download.container.correct",
      "Wizard.typesetter.all.4.download.container.missing",
      container -> container.pull( "typesetter", APP_VERSION_CLEAN ),
      45
    );
  }

  private WizardPane createContainerOutputPanel(
    final String headerKey,
    final String correctKey,
    final String missingKey,
    final FailableConsumer<Container, FileNotFoundException> c,
    final int cols
  ) {
    final var textarea = textArea( 5, cols );
    final var titledPane = titledPane( "Output", textarea );
    final var borderPane = new BorderPane();
    borderPane.setBottom( titledPane );

    final var container = createContainer( textarea );

    final var pane = wizardPane(
      headerKey,
      wizard -> {
        String key;

        textarea.clear();

        try {
          c.accept( container );

          key = correctKey;
        } catch( final FileNotFoundException e ) {
          key = missingKey;
        } catch( Exception e ) {
          throw new RuntimeException( e );
        }

        textarea.appendText( get( key ) );
      } );
    pane.setContent( borderPane );

    return pane;
  }

  private WizardPane wizardPane(
    final String headerKey,
    final Consumer<Wizard> listener ) {
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

    final var wizardPane = new WizardPane() {
      @Override
      public void onEnteringPage( final Wizard wizard ) {
        listener.accept( wizard );
      }
    };
    wizardPane.setHeader( borderPane );

    return wizardPane;
  }

  private WizardPane wizardPane( final String headerKey ) {
    return wizardPane( headerKey, wizard -> { } );
  }

  private TextArea textArea( final int rows, final int cols ) {
    final var textarea = new TextArea();
    textarea.setEditable( false );
    textarea.setWrapText( true );
    textarea.setPrefRowCount( rows );
    textarea.setPrefColumnCount( cols );

    return textarea;
  }

  private record UnixOsCommand( String name, String command )
    implements Comparable<UnixOsCommand> {
    @Override
    public int compareTo(
      @NotNull final TypesetterInstaller.UnixOsCommand other ) {
      return toString().compareToIgnoreCase( other.toString() );
    }

    @Override
    public String toString() {
      return name;
    }
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
    return new Label( get( key ) );
  }

  /**
   * Like printf for labels.
   *
   * @param key    The property key to look up.
   * @param values The values to insert at the placeholders.
   * @return The formatted text with values replaced.
   */
  private Label labelf( final String key, final Object... values ) {
    return new Label( get( key, values ) );
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
    final var pane = new TitledPane( key, child );
    pane.setCollapsible( false );

    return pane;
  }

  private Node flowPane( final Node... nodes ) {
    return new FlowPane( nodes );
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

  private ComboBox<UnixOsCommand> createUnixOsCommandMap() {
    new ComboBox<UnixOsCommand>();
    final var comboBox = new ComboBox<UnixOsCommand>();
    final var items = comboBox.getItems();
    final var prefix = "Wizard.typesetter.unix.2.install.container.command";
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

  private Container createContainer( final TextArea textarea ) {
    return new Podman(
      text -> runLater( () -> {
        textarea.appendText( lineSeparator() );
        textarea.appendText( text );
      } )
    );
  }

  /**
   * Downloads a resource to a local file in a separate {@link Thread}.
   *
   * @param uri      The resource to download.
   * @param file     The destination target for the resource.
   * @param listener Receives updates as the download proceeds.
   */
  private void download(
    final URI uri,
    final File file,
    final ProgressListener listener ) {
    final var task = new Task<Void>() {
      @Override
      protected Void call() throws Exception {
        try( final var token = DownloadManager.open( uri ) ) {
          final var output = new FileOutputStream( file );
          final var downloader = token.download( output, listener );

          downloader.run();
        }

        return null;
      }
    };

    new Thread( task ).start();
  }
}
