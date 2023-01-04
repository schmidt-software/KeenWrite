/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.typesetting.installer.panes;

import com.keenwrite.events.HyperlinkOpenEvent;
import com.keenwrite.io.downloads.DownloadManager;
import com.keenwrite.io.downloads.DownloadManager.ProgressListener;
import com.keenwrite.typesetting.containerization.ContainerManager;
import com.keenwrite.typesetting.containerization.Podman;
import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import org.controlsfx.dialog.Wizard;
import org.controlsfx.dialog.WizardPane;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

import static com.keenwrite.Messages.get;
import static com.keenwrite.constants.GraphicsConstants.ICON_DIALOG;
import static java.lang.System.lineSeparator;
import static javafx.animation.Interpolator.LINEAR;
import static javafx.application.Platform.runLater;
import static javafx.scene.control.ButtonBar.ButtonData.NEXT_FORWARD;
import static javafx.scene.control.ContentDisplay.RIGHT;
import static javafx.util.Duration.seconds;

/**
 * Responsible for creating a {@link WizardPane} with a common header for all
 * subclasses.
 */
public abstract class InstallerPane extends WizardPane {
  /**
   * Unique key name to store the animation object so that it can be stopped.
   */
  private static final String PROP_ROTATION = "Wizard.typesetter.next.animate";

  /**
   * Defines amount of spacing between the installer's UI widgets, in pixels.
   */
  static final int PAD = 10;

  private static final double HEADER_FONT_SCALE = 1.25;

  public InstallerPane() {
    setHeader( createHeader() );
  }

  /**
   * When leaving the page, stop the animation. This is idempotent.
   *
   * @param wizard The wizard controlling the installer steps.
   */
  @Override
  public void onExitingPage( final Wizard wizard ) {
    super.onExitingPage( wizard );
    runLater( () -> stopAnimation( getNextButton() ) );
  }

  /**
   * Returns the property bundle key representing the dialog box title.
   */
  protected abstract String getHeaderKey();

  private BorderPane createHeader() {
    final var headerLabel = label( getHeaderKey() );
    headerLabel.setScaleX( HEADER_FONT_SCALE );
    headerLabel.setScaleY( HEADER_FONT_SCALE );

    final var separator = new Separator();
    separator.setPadding( new Insets( PAD, 0, 0, 0 ) );

    final var header = new BorderPane();
    header.setCenter( headerLabel );
    header.setRight( new ImageView( ICON_DIALOG ) );
    header.setBottom( separator );
    header.setPadding( new Insets( PAD, PAD, 0, PAD ) );

    return header;
  }

  /**
   * Disables the "Next" button during the installer. Normally disabling UI
   * elements is an anti-pattern (along with modal dialogs); however, in this
   * case, installation cannot proceed until each step is successfully
   * completed. Further, there may be "misleading" success messages shown
   * in the output panel, which the user may take as the step being complete.
   *
   * @param disable Set to {@code true} to disable the button.
   */
  void disableNext( final boolean disable ) {
    runLater( () -> {
      final var button = getNextButton();

      button.setDisable( disable );

      if( disable ) {
        startAnimation( button );
      }
      else {
        stopAnimation( button );
      }
    } );
  }

  /**
   * Returns the {@link Button} for advancing the wizard to the next pane.
   *
   * @return The Next button, if present, otherwise a new {@link Button}
   * instance so that API calls will succeed, despite not affecting the UI.
   */
  private Button getNextButton() {
    for( final var buttonType : getButtonTypes() ) {
      final var buttonData = buttonType.getButtonData();

      if( buttonData.equals( NEXT_FORWARD ) &&
        lookupButton( buttonType ) instanceof Button button ) {
        return button;
      }
    }

    // If there's no Next button, return a fake button.
    return new Button();
  }

  private void startAnimation( final Button button ) {
    // Create an image that is slightly taller than the button's font.
    final var graphic = new ImageView( ICON_DIALOG );
    graphic.setFitHeight( button.getFont().getSize() + 2 );
    graphic.setPreserveRatio( true );
    graphic.setSmooth( true );

    button.setGraphic( graphic );
    button.setGraphicTextGap( PAD );
    button.setContentDisplay( RIGHT );

    final var rotation = new RotateTransition( seconds( 1 ), graphic );
    getProperties().put( PROP_ROTATION, rotation );

    rotation.setCycleCount( Animation.INDEFINITE );
    rotation.setByAngle( 360 );
    rotation.setInterpolator( LINEAR );
    rotation.play();
  }

  private void stopAnimation( final Button button ) {
    final var animation = getProperties().get( PROP_ROTATION );

    if( animation instanceof RotateTransition rotation ) {
      rotation.stop();
      button.setGraphic( null );
      getProperties().remove( PROP_ROTATION );
    }
  }

  static TitledPane titledPane( final String title, final Node child ) {
    final var pane = new TitledPane( title, child );
    pane.setAnimated( false );
    pane.setCollapsible( false );
    pane.setExpanded( true );

    return pane;
  }

  static TextArea textArea( final int rows, final int cols ) {
    final var textarea = new TextArea();
    textarea.setEditable( false );
    textarea.setWrapText( true );
    textarea.setPrefRowCount( rows );
    textarea.setPrefColumnCount( cols );

    return textarea;
  }

  static Label label( final String key ) {
    return new Label( get( key ) );
  }

  /**
   * Like printf for labels.
   *
   * @param key    The property key to look up.
   * @param values The values to insert at the placeholders.
   * @return The formatted text with values replaced.
   */
  @SuppressWarnings( "SpellCheckingInspection" )
  static Label labelf( final String key, final Object... values ) {
    return new Label( get( key, values ) );
  }

  @SuppressWarnings( "SameParameterValue" )
  static Button button( final String key ) {
    return new Button( get( key ) );
  }

  static Node flowPane( final Node... nodes ) {
    return new FlowPane( nodes );
  }

  /**
   * Provides vertical spacing between {@link Node}s.
   *
   * @return A new empty vertical gap widget.
   */
  static Node spacer() {
    final var spacer = new Pane();
    spacer.setPadding( new Insets( PAD, 0, 0, 0 ) );

    return spacer;
  }

  static Hyperlink hyperlink( final String prefix ) {
    final var label = get( prefix + ".lbl" );
    final var url = get( prefix + ".url" );
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
  static void browse( final String property ) {
    final var url = get( property );
    final var task = createTask( () -> {
      HyperlinkOpenEvent.fire( url );
      return null;
    } );
    final var thread = createThread( task );

    thread.start();
  }

  static <T> Task<T> createTask( final Callable<T> callable ) {
    return new Task<>() {
      @Override
      protected T call() throws Exception {
        return callable.call();
      }
    };
  }

  static <T> Thread createThread( final Task<T> task ) {
    final var thread = new Thread( task );
    thread.setDaemon( true );
    return thread;
  }

  /**
   * Creates a container that can have its standard output read as an input
   * stream that's piped directly to a {@link TextArea}.
   *
   * @return An object that can perform tasks against a container.
   */
  static ContainerManager createContainer() {
    return new Podman();
  }

  static void update( final Label node, final String text ) {
    runLater( () -> node.setText( text ) );
  }

  static void append( final TextArea node, final String text ) {
    runLater( () -> {
      node.appendText( text );
      node.appendText( lineSeparator() );
    } );
  }

  /**
   * Downloads a resource to a local file in a separate {@link Thread}.
   *
   * @param uri      The resource to download.
   * @param file     The destination mTarget for the resource.
   * @param listener Receives updates as the download proceeds.
   */
  static Task<Void> downloadAsync(
    final URI uri,
    final File file,
    final ProgressListener listener ) {
    final Task<Void> task = createTask( () -> {
      try( final var token = DownloadManager.open( uri ) ) {
        final var output = new FileOutputStream( file );
        final var downloader = token.download( output, listener );

        downloader.run();
      }

      return null;
    } );

    createThread( task ).start();
    return task;
  }

  static String toFilename( final URI uri ) {
    return Paths.get( uri.getPath() ).toFile().getName();
  }
}
