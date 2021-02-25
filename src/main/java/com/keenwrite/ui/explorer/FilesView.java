/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.ui.explorer;

import com.keenwrite.ui.controls.BrowseButton;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static com.keenwrite.Constants.UI_CONTROL_SPACING;
import static com.keenwrite.events.StatusEvent.clue;
import static java.nio.file.Files.readAttributes;
import static java.time.ZoneId.systemDefault;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static javafx.collections.FXCollections.observableArrayList;
import static javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY;
import static javafx.scene.layout.Priority.ALWAYS;

/**
 * Responsible for browsing files.
 */
public class FilesView extends BorderPane {
  private final ObjectProperty<File> mDirectory = new SimpleObjectProperty<>();
  private final ObservableList<PathEntry> mItems = observableArrayList();
  private final DateTimeFormatter mFormatter;

  public FilesView( final File directory, final Locale locale ) {
    assert directory != null;

    mFormatter = ISO_LOCAL_DATE_TIME
      .withLocale( locale )
      .withZone( systemDefault() );

    final var browse = createDirectoryChooser();
    final var table = createFileTable();

    final var sortedItems = new SortedList<>( mItems );
    sortedItems.comparatorProperty().bind( table.comparatorProperty() );
    table.setItems( sortedItems );

    mDirectory.addListener( ( c, o, n ) -> {
      if( n != null ) {
        mItems.clear();

        for( final var file : n.list() ) {
          try {
            mItems.add( new PathEntry( Paths.get( directory.toString(), file ) ) );
          } catch( final Exception ex ) {
            clue( ex );
          }
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

  private TableView<FilesView.PathEntry> createFileTable() {
    final var table = new TableView<FilesView.PathEntry>();
    table.setColumnResizePolicy( CONSTRAINED_RESIZE_POLICY );

    final TableColumn<PathEntry, String> colType = createColumn( "Type" );
    final TableColumn<PathEntry, String> colName = createColumn( "Name" );
    final TableColumn<PathEntry, Number> colSize = createColumn( "Size" );
    final TableColumn<PathEntry, String> colDate = createColumn( "Date" );

    colType.setCellValueFactory( stat -> stat.getValue().typeProperty() );
    colName.setCellValueFactory( stat -> stat.getValue().nameProperty() );
    colSize.setCellValueFactory( stat -> stat.getValue().sizeProperty() );
    colDate.setCellValueFactory( stat -> stat.getValue().dateProperty() );

    final var columns = table.getColumns();
    columns.add( colType );
    columns.add( colName );
    columns.add( colSize );
    columns.add( colDate );

    return table;
  }

  public ObjectProperty<File> directoryProperty() {
    return mDirectory;
  }

  protected final class PathEntry {
    private final StringProperty mType;
    private final StringProperty mName;
    private final LongProperty mSize;
    private final StringProperty mDate;

    public PathEntry( final Path path ) throws IOException {
      this(
        path.toFile().getName(),
        readAttributes( path, BasicFileAttributes.class )
      );
    }

    private PathEntry( final String name, final BasicFileAttributes attrs ) {
      this(
        attrs.isDirectory()
          ? "<DIR>" : attrs.isRegularFile()
          ? "<FILE>" : attrs.isSymbolicLink()
          ? "<LINK>" : "<OTHER>",
        name,
        attrs.size(),
        FilesView.this.mFormatter.format( attrs.lastModifiedTime().toInstant() )
      );
    }

    public PathEntry(
      final String name,
      final String type,
      final long size,
      final String dateTime ) {
      this(
        new SimpleStringProperty( name ),
        new SimpleStringProperty( type ),
        new SimpleLongProperty( size ),
        new SimpleStringProperty( dateTime )
      );
    }

    private PathEntry(
      final StringProperty type,
      final StringProperty name,
      final LongProperty size,
      final StringProperty dateTime ) {
      mType = type;
      mName = name;
      mSize = size;
      mDate = dateTime;
    }

    private StringProperty typeProperty() {
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
  }

  private <E, T> TableColumn<E, T> createColumn( final String key ) {
    return new TableColumn<>( key );
  }
}
