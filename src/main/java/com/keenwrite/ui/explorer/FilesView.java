/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.ui.explorer;

import com.keenwrite.events.FileOpenEvent;
import com.keenwrite.ui.controls.BrowseButton;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import static com.keenwrite.constants.Constants.UI_CONTROL_SPACING;
import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.ui.fonts.IconFactory.createFileIcon;
import static java.nio.file.Files.size;
import static java.time.Instant.ofEpochMilli;
import static java.time.ZoneId.systemDefault;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Comparator.comparing;
import static javafx.collections.FXCollections.observableArrayList;
import static javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.layout.Priority.ALWAYS;
import static org.apache.commons.io.FilenameUtils.getExtension;

/**
 * Responsible for browsing files.
 */
public class FilesView extends BorderPane implements FilePicker {
  /**
   * When this directory changes, the input field will update accordingly.
   */
  private final ObjectProperty<File> mDirectory;

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
   * therein. This will update the recent directory so that it will be
   * restored upon restart.
   *
   * @param recent Contains the initial (recent) directory.
   * @param locale Contains the language settings.
   */
  public FilesView(
    final ObjectProperty<File> recent, final Locale locale ) {
    mDirectory = recent;
    mDateFormatter = createFormatter( "yyyy-MMM-dd", locale );
    mTimeFormatter = createFormatter( "HH:mm:ss", locale );

    final var browse = createDirectoryChooser();
    final var table = createFileTable();

    final var sortedItems = new SortedList<>( mItems );
    sortedItems.comparatorProperty().bind( table.comparatorProperty() );
    table.setItems( sortedItems );

    setTop( browse );
    setCenter( table );

    mDirectory.addListener( ( c, o, n ) -> updateListing( n ) );
    updateListing( mDirectory.get() );
  }

  @Override
  public void setInitialFilename( final File file ) {
  }

  @Override
  public Optional<List<File>> choose() {
    return Optional.empty();
  }

  private void updateListing( final File directory ) {
    if( directory != null ) {
      mItems.clear();

      try {
        if( directory.getParent() != null ) {
          // Allow traversal to parent-directory.
          mItems.add( pathEntry( Paths.get( ".." ) ) );
        }

        for( final var f : Objects.requireNonNull( directory.list() ) ) {
          if( !f.startsWith( "." ) ) {
            mItems.add( pathEntry( Paths.get( directory.toString(), f ) ) );
          }
        }
      } catch( final Exception ex ) {
        clue( ex );
      }
    }
  }

  /**
   * Allows the user to use an instance of {@link FileChooser} to change the
   * directory.
   *
   * @return The browse button and input field.
   */
  private HBox createDirectoryChooser() {
    final var dirProperty = directoryProperty();
    final var directory = dirProperty.get();
    final var hbox = new HBox();
    final var field = new TextField();

    mDirectory.addListener( ( c, o, n ) -> {
      if( n != null ) {field.setText( n.getAbsolutePath() );}
    } );

    field.setOnKeyPressed( event -> {
      if( event.getCode() == ENTER ) {
        mDirectory.set( new File( field.getText() ) );
      }
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
    final var style = "-fx-alignment: BASELINE_LEFT;";
    final var table = new TableView<FilesView.PathEntry>();
    table.setColumnResizePolicy( CONSTRAINED_RESIZE_POLICY );

    table.setRowFactory( tv -> {
      final var row = new TableRow<PathEntry>();

      row.setOnMouseClicked( event -> {
        if( event.getClickCount() == 2 && !row.isEmpty() ) {
          final var entry = row.getItem();
          final var dir = mDirectory.get();
          final var filename = entry.nameProperty().get();
          final var path = Path.of( dir.toString(), filename );
          final var file = path.toFile();

          if( file.isFile() ) {
            FileOpenEvent.fire( path.toUri() );
          }
          else if( file.isDirectory() ) {
            mDirectory.set( path.normalize().toFile() );
          }
        }
      } );

      return row;
    } );

    final TableColumn<PathEntry, Path> colType = createColumn( "Type" );
    final TableColumn<PathEntry, String> colName = createColumn( "Name" );
    final TableColumn<PathEntry, Number> colSize = createColumn( "Size" );
    final TableColumn<PathEntry, String> colDate = createColumn( "Date" );
    final TableColumn<PathEntry, String> colTime = createColumn( "Modified" );

    colType.setCellFactory( new FileCell<>() );

    colType.setCellValueFactory( stat -> stat.getValue().typeProperty() );
    colName.setCellValueFactory( stat -> stat.getValue().nameProperty() );
    colSize.setCellValueFactory( stat -> stat.getValue().sizeProperty() );
    colDate.setCellValueFactory( stat -> stat.getValue().dateProperty() );
    colTime.setCellValueFactory( stat -> stat.getValue().timeProperty() );

    colType.setStyle( style );
    colName.setStyle( style );
    colSize.setStyle( style );
    colDate.setStyle( style );
    colTime.setStyle( style );

    final var columns = table.getColumns();
    columns.add( colType );
    columns.add( colName );
    columns.add( colSize );
    columns.add( colDate );
    columns.add( colTime );

    table.getSortOrder().setAll( colName, colDate, colTime );

    colType.setComparator(
      comparing( p -> getExtension( p.getFileName().toString() ) )
    );

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

  /**
   * Responsible for rendering file system objects as image icons.
   *
   * @param <T> The data model type associated with a fully qualified path.
   * @param <P> Simplifies swapping {@link Path} for {@link File}.
   */
  private static class FileCell<T, P extends Path> extends TableCell<T, P>
    implements Callback<TableColumn<T, P>, TableCell<T, P>> {
    @Override
    public TableCell<T, P> call( final TableColumn<T, P> param ) {
      return new TableCell<>() {
        @Override
        protected void updateItem( final P path, final boolean empty ) {
          super.updateItem( path, empty );
          setText( null );

          try {
            setGraphic( empty || path == null ? null : createFileIcon( path ) );
          } catch( final Exception ex ) {
            clue( ex );
          }
        }
      };
    }
  }

  protected final class PathEntry {
    private final ObjectProperty<Path> mType;
    private final StringProperty mName;
    private final LongProperty mSize;
    private final StringProperty mDate;
    private final StringProperty mTime;

    private PathEntry( final Path path ) throws IOException {
      this(
        path,
        path.getFileName().toString(),
        size( path ),
        ofEpochMilli( path.toFile().lastModified() )
      );
    }

    public PathEntry(
      final Path type,
      final String name,
      final long size,
      final Instant modified ) {
      this(
        new SimpleObjectProperty<>( type ),
        new SimpleStringProperty( name ),
        new SimpleLongProperty( size ),
        new SimpleStringProperty( mDateFormatter.format( modified ) ),
        new SimpleStringProperty( mTimeFormatter.format( modified ) )
      );
    }

    private PathEntry(
      final ObjectProperty<Path> type,
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

    private ObjectProperty<Path> typeProperty() {
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
