/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.ui.logging;

import com.keenwrite.events.StatusEvent;
import com.keenwrite.ui.actions.Keyboard;
import com.keenwrite.ui.clipboard.SystemClipboard;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.greenrobot.eventbus.Subscribe;

import java.time.LocalDateTime;
import java.util.Objects;

import static com.keenwrite.Messages.get;
import static com.keenwrite.constants.Constants.ACTION_PREFIX;
import static com.keenwrite.constants.GraphicsConstants.ICON_DIALOG;
import static com.keenwrite.events.Bus.register;
import static com.keenwrite.events.StatusEvent.clue;
import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ofPattern;
import static javafx.application.Platform.runLater;
import static javafx.collections.FXCollections.observableArrayList;
import static javafx.event.ActionEvent.ACTION;
import static javafx.scene.control.Alert.AlertType.INFORMATION;
import static javafx.scene.control.ButtonType.OK;
import static javafx.scene.control.SelectionMode.MULTIPLE;
import static javafx.stage.Modality.NONE;

/**
 * Responsible for logging application issues to {@link TableView} entries.
 */
public final class LogView extends Alert {
  /**
   * Number of error messages to retain in the {@link TableView}; must be
   * greater than zero. Typesetting the document can cause many page number
   * messages to be logged.
   */
  private static final int CACHE_SIZE = 500;

  private final ObservableList<LogEntry> mItems = observableArrayList();
  private final TableView<LogEntry> mTable = new TableView<>( mItems );

  public LogView() {
    super( INFORMATION );
    setTitle( get( ACTION_PREFIX + "view.log.text" ) );
    initModality( NONE );
    initTableView();
    setResizable( true );
    initButtons();
    initIcon();
    initActions();
    register( this );
  }

  @Subscribe
  public void log( final StatusEvent event ) {
    runLater( () -> {
      final var logEntry = new LogEntry( event );

      if( !mItems.contains( logEntry ) ) {
        mItems.add( logEntry );

        while( mItems.size() > CACHE_SIZE ) {
          mItems.remove( 0 );
        }

        mTable.scrollTo( logEntry );
      }
    } );
  }

  /**
   * Brings the dialog to the foreground, showing it if needed.
   */
  public void view() {
    super.show();
    getStage().toFront();
  }

  /**
   * Removes all the entries from the list.
   */
  public void clear() {
    mItems.clear();
    clue();
  }

  private void initTableView() {
    final var colDate = new TableColumn<LogEntry, String>( "Timestamp" );
    final var colMessage = new TableColumn<LogEntry, String>( "Message" );
    final var colTrace = new TableColumn<LogEntry, String>( "Trace" );

    colDate.setCellValueFactory( log -> log.getValue().dateProperty() );
    colMessage.setCellValueFactory( log -> log.getValue().messageProperty() );
    colTrace.setCellValueFactory( log -> log.getValue().traceProperty() );

    final var columns = mTable.getColumns();
    columns.add( colDate );
    columns.add( colMessage );
    columns.add( colTrace );

    // Display the entire date by default.
    colDate.setPrefWidth( 135 );

    // Display most of the message by default.
    colMessage.setPrefWidth( 425 );

    // Display a large portion of the stack trace.
    colTrace.setPrefWidth( 600 );

    mTable.setMaxWidth( Double.MAX_VALUE );
    mTable.setPrefWidth( 1200 );
    mTable.getSelectionModel().setSelectionMode( MULTIPLE );
    mTable.setOnKeyPressed( event -> {
      if( Keyboard.isCopy( event ) ) {
        SystemClipboard.write( mTable );
      }
    } );

    final var pane = getDialogPane();
    pane.setContent( mTable );
  }

  private void initButtons() {
    final var pane = getDialogPane();
    final var CLEAR = new ButtonType( "CLEAR" );
    pane.getButtonTypes().add( CLEAR );

    final var buttonOk = (Button) pane.lookupButton( OK );
    final var buttonClear = (Button) pane.lookupButton( CLEAR );

    buttonOk.setDefaultButton( true );
    buttonClear.addEventFilter( ACTION, event -> {
      clear();
      event.consume();
    } );

    pane.setOnKeyReleased( t -> {
      switch( t.getCode() ) {
        case ENTER, ESCAPE -> buttonOk.fire();
        default -> { }
      }
    } );
  }

  private void initIcon() {
    getStage().getIcons().add( ICON_DIALOG );
  }

  private void initActions() {
    final var stage = getStage();
    stage.setOnCloseRequest( event -> stage.hide() );
  }

  private Stage getStage() {
    return (Stage) getDialogPane().getScene().getWindow();
  }

  private static final class LogEntry {
    private final StringProperty mDate;
    private final StringProperty mMessage;
    private final StringProperty mTrace;

    /**
     * Constructs a new {@link LogEntry} for the current time.
     */
    public LogEntry( final StatusEvent event ) {
      mDate = new SimpleStringProperty( toString( now() ) );
      mMessage = new SimpleStringProperty( event.getMessage() );
      mTrace = new SimpleStringProperty( event.getProblem() );
    }

    private StringProperty messageProperty() {
      return mMessage;
    }

    private StringProperty dateProperty() {
      return mDate;
    }

    private StringProperty traceProperty() {
      return mTrace;
    }

    @Override
    public boolean equals( final Object o ) {
      if( this == o ) { return true; }
      if( o == null || getClass() != o.getClass() ) { return false; }

      return Objects.equals( mMessage.get(), ((LogEntry) o).mMessage.get() );
    }

    @Override
    public int hashCode() {
      return mMessage != null ? mMessage.hashCode() : 0;
    }

    @Override
    public String toString() {
      final var date = mDate == null ? "" : mDate.get();
      final var message = mMessage == null ? "" : mMessage.get();
      final var trace = mTrace == null ? "" : mTrace.get();

      return getClass().getSimpleName() + "{" +
        "mDate=" + (date == null ? "''" : date) +
        ", mMessage=" + (message == null ? "''" : message) +
        ", mTrace=" + (trace == null ? "''" : trace) +
        '}';
    }

    private String toString( final LocalDateTime date ) {
      return date.format( ofPattern( "d MMM u HH:mm:ss" ) );
    }
  }
}
