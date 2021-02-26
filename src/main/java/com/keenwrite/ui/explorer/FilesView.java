/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.ui.explorer;

import com.keenwrite.ui.controls.BrowseButton;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static com.keenwrite.Constants.UI_CONTROL_SPACING;
import static com.keenwrite.events.FileOpenEvent.fireFileOpenEvent;
import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.ui.fonts.IconFactory.createFileIcon;
import static java.nio.file.Files.readAttributes;
import static java.time.ZoneId.systemDefault;
import static java.time.format.DateTimeFormatter.ofPattern;
import static javafx.collections.FXCollections.observableArrayList;
import static javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY;
import static javafx.scene.layout.Priority.ALWAYS;

/**
 * Responsible for browsing files.
 */
public class FilesView extends BorderPane {
  /**
   * When this directory changes, the input field will update accordingly.
   */
  private final ObjectProperty<File> mDirectory = new SimpleObjectProperty<>();

  /**
   * Data model for the file list shown in tabular format.
   */
  private final ObservableList<PathEntry> mItems = observableArrayList();

  /**
   * Used to format a file's date string from a {@code long} value.
   */
  private final DateTimeFormatter mDateFormatter;

  /**
   * Used to format a file's time string from a {@code long} value.
   */
  private final DateTimeFormatter mTimeFormatter;

  /**
   * Constructs a new view of a directory, listing all the files contained
   * therein.
   *
   * @param directory Initial directory to open.
   * @param locale    Language settings for date/time formatting.
   */
  public FilesView( final File directory, final Locale locale ) {
    assert directory != null;

    mDateFormatter = createFormatter( "yyyy-MMM-dd", locale );
    mTimeFormatter = createFormatter( "HH:mm:ss", locale );

    final var browse = createDirectoryChooser();
    final var table = createFileTable();

    final var sortedItems = new SortedList<>( mItems );
    sortedItems.comparatorProperty().bind( table.comparatorProperty() );
    table.setItems( sortedItems );

    mDirectory.addListener( ( c, o, n ) -> {
      if( n != null ) {
        mItems.clear();

        try {
          if( n.getParent() != null ) {
            // Allow traversal to parent-directory.
            mItems.add( pathEntry( Paths.get( ".." ) ) );
          }

          for( final var f : n.list() ) {
            mItems.add( pathEntry( Paths.get( directory.toString(), f ) ) );
          }
        } catch( final Exception ex ) {
          clue( ex );
        }
      }
    } );

    mDirectory.set( directory );

    setTop( browse );
    setCenter( table );
  }

  private HBox createDirectoryChooser() {
    final var dirProperty = directoryProperty();
    final var directory = dirProperty.get();
    final var hbox = new HBox();
    final var field = new TextField();

    mDirectory.addListener( ( c, o, n ) -> {
      if( n != null ) { field.setText( n.getAbsolutePath() ); }
    } );

    final var button = new BrowseButton( directory, mDirectory::set );

    hbox.getChildren().add( button );
    hbox.getChildren().add( field );
    hbox.setSpacing( UI_CONTROL_SPACING );
    HBox.setHgrow( field, ALWAYS );

    return hbox;
  }

  @SuppressWarnings( "unchecked" )
  private TableView<FilesView.PathEntry> createFileTable() {
    final var table = new TableView<FilesView.PathEntry>();
    table.setColumnResizePolicy( CONSTRAINED_RESIZE_POLICY );

    table.setRowFactory( tv -> {
      final var row = new TableRow<PathEntry>();
      row.setOnMouseClicked( event -> {
        if( event.getClickCount() == 2 && (!row.isEmpty()) ) {
          final var entry = row.getItem();
          final var dir = mDirectory.get();
          final var filename = entry.nameProperty().get();
          final var path = Path.of( dir.toString(), filename );

          fireFileOpenEvent( path.toUri() );
        }
      } );
      return row;
    } );

    final TableColumn<PathEntry, ImageView> colType = createColumn( "Type" );
    final TableColumn<PathEntry, String> colName = createColumn( "Name" );
    final TableColumn<PathEntry, Number> colSize = createColumn( "Size" );
    final TableColumn<PathEntry, String> colDate = createColumn( "Date" );
    final TableColumn<PathEntry, String> colTime = createColumn( "Time" );

    colType.setCellValueFactory( stat -> stat.getValue().typeProperty() );
    colName.setCellValueFactory( stat -> stat.getValue().nameProperty() );
    colSize.setCellValueFactory( stat -> stat.getValue().sizeProperty() );
    colDate.setCellValueFactory( stat -> stat.getValue().dateProperty() );
    colTime.setCellValueFactory( stat -> stat.getValue().timeProperty() );

    final var columns = table.getColumns();
    columns.add( colType );
    columns.add( colName );
    columns.add( colSize );
    columns.add( colDate );
    columns.add( colTime );

    table.getSortOrder().setAll( colName, colDate, colTime );

    return table;
  }

  public ObjectProperty<File> directoryProperty() {
    return mDirectory;
  }

  private static DateTimeFormatter createFormatter(
    final String format, final Locale locale ) {
    return ofPattern( format, locale ).withZone( systemDefault() );
  }

  public PathEntry pathEntry( final Path path ) throws IOException {
    return new PathEntry( path );
  }

  protected final class PathEntry {
    private final ObjectProperty<ImageView> mType;
    private final StringProperty mName;
    private final LongProperty mSize;
    private final StringProperty mDate;
    private final StringProperty mTime;

    protected PathEntry( final Path path ) throws IOException {
      this(
        path.toFile(),
        readAttributes( path, BasicFileAttributes.class )
      );
    }

    private PathEntry( final File path, final BasicFileAttributes attrs ) {
      this(
        createFileIcon( path, attrs ),
        path.getName(),
        attrs.size(),
        attrs.lastModifiedTime().toInstant()
      );
    }

    public PathEntry(
      final ImageView type,
      final String name,
      final long size,
      final Instant dateTime ) {
      this(
        new SimpleObjectProperty<>( type ),
        new SimpleStringProperty( name ),
        new SimpleLongProperty( size ),
        new SimpleStringProperty(
          FilesView.this.mDateFormatter.format( dateTime ) ),
        new SimpleStringProperty(
          FilesView.this.mTimeFormatter.format( dateTime ) )
      );
    }

    private PathEntry(
      final ObjectProperty<ImageView> type,
      final StringProperty name,
      final LongProperty size,
      final StringProperty date,
      final StringProperty time ) {
      mType = type;
      mName = name;
      mSize = size;
      mDate = date;
      mTime = time;
    }

    private ObjectProperty<ImageView> typeProperty() {
      return mType;
    }

    private StringProperty nameProperty() {
      return mName;
    }

    private LongProperty sizeProperty() {
      return mSize;
    }

    private StringProperty dateProperty() {
      return mDate;
    }

    private StringProperty timeProperty() {
      return mTime;
    }
  }

  private <E, T> TableColumn<E, T> createColumn( final String key ) {
    return new TableColumn<>( key );
  }
}
