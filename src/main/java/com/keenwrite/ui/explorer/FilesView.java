package com.keenwrite.ui.explorer;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.nio.file.Path;

public class FilesView extends TableView<FilesView.PathEntry> {

  public FilesView( final Path directory ) {
    initView();
  }

  private void initView() {
    final TableColumn<PathEntry, String> colType = createColumn( "Type" );
    final TableColumn<PathEntry, String> colName = createColumn( "Name" );
    final TableColumn<PathEntry, Number> colSize = createColumn( "Size" );
    final TableColumn<PathEntry, String> colDate = createColumn( "Date" );

    colType.setCellValueFactory( stat -> stat.getValue().typeProperty() );
    colName.setCellValueFactory( stat -> stat.getValue().nameProperty() );
    colSize.setCellValueFactory( stat -> stat.getValue().sizeProperty() );
    colDate.setCellValueFactory( stat -> stat.getValue().dateProperty() );

    final var columns = getColumns();
    columns.add( colType );
    columns.add( colName );
    columns.add( colSize );
    columns.add( colDate );

    setMaxWidth( Double.MAX_VALUE );
    setPrefWidth( 256 );
    setColumnResizePolicy( CONSTRAINED_RESIZE_POLICY );
  }

  private <E, T> TableColumn<E, T> createColumn( final String key ) {
    return new TableColumn<>( key );
  }

  protected static final class PathEntry {
    private final StringProperty mType;
    private final StringProperty mName;
    private final IntegerProperty mSize;
    private final StringProperty mDate;

    public PathEntry(
      final StringProperty type,
      final StringProperty name,
      final IntegerProperty size,
      final StringProperty date ) {
      mType = type;
      mName = name;
      mSize = size;
      mDate = date;
    }

    private StringProperty typeProperty() {
      return mType;
    }

    private StringProperty nameProperty() {
      return mName;
    }

    private IntegerProperty sizeProperty() {
      return mSize;
    }

    private StringProperty dateProperty() {
      return mDate;
    }
  }
}
