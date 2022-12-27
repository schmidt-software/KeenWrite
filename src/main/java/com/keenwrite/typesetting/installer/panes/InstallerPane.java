/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.typesetting.installer.panes;

import com.keenwrite.events.HyperlinkOpenEvent;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import org.controlsfx.dialog.Wizard;
import org.controlsfx.dialog.WizardPane;

import java.util.concurrent.Callable;

import static com.keenwrite.Messages.get;
import static com.keenwrite.constants.GraphicsConstants.ICON_DIALOG;
import static javafx.scene.control.ButtonBar.ButtonData.NEXT_FORWARD;

/**
 * Responsible for creating a {@link WizardPane} with a common header for all
 * subclasses.
 */
public abstract class InstallerPane extends WizardPane {
  /**
   * Defines amount of spacing between the installer's UI widgets, in pixels.
   */
  static final int PAD = 10;

  private static final double HEADER_FONT_SCALE = 1.25;

  public InstallerPane() {
    setHeader( createHeader() );
  }

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

  static TitledPane titledPane( final String key, final Node child ) {
    final var pane = new TitledPane( key, child );
    pane.setCollapsible( false );

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

  static Node flowPane( final Node... nodes ) {
    return new FlowPane( nodes );
  }

  /**
   * Provides vertical spacing between {@link Node}s.
   *
   * @return A new empty vertical gap widget.
   */
  static Node spacer() {
    final var spacer = new FlowPane();
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

  void disableNext( final boolean disable ) {
    for( final var buttonType : getButtonTypes() ) {
      final var buttonData = buttonType.getButtonData();

      if( buttonData.equals( NEXT_FORWARD ) ) {
        final var button = lookupButton( buttonType );
        Platform.runLater( () -> button.setDisable( disable ) );
        break;
      }
    }
  }
}
