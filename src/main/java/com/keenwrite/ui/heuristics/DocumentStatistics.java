/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.ui.heuristics;

import com.keenwrite.events.DocumentChangedEvent;
import com.keenwrite.preferences.Workspace;
import com.keenwrite.preview.HtmlPanel;
import com.keenwrite.util.MurmurHash;
import com.whitemagicsoftware.wordcount.Tokenizer;
import com.whitemagicsoftware.wordcount.TokenizerException;
import com.whitemagicsoftware.wordcount.TokenizerFactory;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.greenrobot.eventbus.Subscribe;
import org.jsoup.Jsoup;

import java.util.Locale;

import static com.keenwrite.events.Bus.register;
import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.events.WordCountEvent.fireWordCountEvent;
import static com.keenwrite.preferences.WorkspaceKeys.KEY_LANGUAGE_LOCALE;
import static com.keenwrite.preferences.WorkspaceKeys.KEY_UI_FONT_EDITOR_NAME;
import static java.lang.String.format;
import static java.util.Locale.ENGLISH;
import static javafx.application.Platform.runLater;
import static javafx.collections.FXCollections.observableArrayList;

/**
 * Responsible for displaying document statistics, such as word count and
 * word frequency.
 */
public final class DocumentStatistics
  extends TableView<DocumentStatistics.StatEntry> {
  /**
   * Parses documents into word counts.
   */
  private static Tokenizer sTokenizer = createTokenizer( ENGLISH );

  private final ObservableList<StatEntry> mItems = observableArrayList();

  /**
   * Creates a new observer of document change events that will gather and
   * display document statistics (e.g., word counts).
   *
   * @param workspace Settings used to configure the statistics engine.
   */
  public DocumentStatistics( final Workspace workspace ) {
    final var sortedItems = new SortedList<>( mItems );
    sortedItems.comparatorProperty().bind( comparatorProperty() );
    setItems( sortedItems );

    initView();
    initListeners( workspace );
    register( this );

    final var fontName = workspace.stringProperty( KEY_UI_FONT_EDITOR_NAME );

    fontName.addListener(
      ( c, o, n ) -> {
        if( n != null ) {
          setFontFamily( n );
        }
      }
    );

    setFontFamily( fontName.getValue() );
  }

  /**
   * Called when the hashcode for the current document changes. This happens
   * when non-collapsable-whitespace is added to the document. When the
   * document is sent to {@link HtmlPanel} for rendering, the parsed
   * {@link Jsoup} document is converted to text. If that text differs
   * (using {@link MurmurHash}), then this method is called. The implication
   * is that all variables and executable statements have been replaced.
   * An event bus subscriber is used so that text processing occurs outside
   * of the UI processing threads.
   *
   * @param event Container for the document text that has changed.
   */
  @Subscribe
  public void handle( final DocumentChangedEvent event ) {
    try {
      final var tokens = sTokenizer.tokenize( event.getDocument() );
      final var sum = new int[]{0};

      runLater( () -> {
        mItems.clear();
        tokens.forEach( ( k, v ) -> {
          final var count = v[ 0 ];
          if( count > 2 ) {
            mItems.add( new StatEntry( k, count ) );
          }
          sum[ 0 ] += count;
        } );

        fireWordCountEvent( sum[ 0 ] );
      } );

    } catch( final TokenizerException ex ) {
      clue( ex );
    }
  }

  @SuppressWarnings( "unchecked" )
  private void initView() {
    final TableColumn<StatEntry, String> colWord = createColumn( "Word" );
    final TableColumn<StatEntry, Number> colCount = createColumn( "Count" );

    colWord.setCellValueFactory( stat -> stat.getValue().wordProperty() );
    colCount.setCellValueFactory( stat -> stat.getValue().tallyProperty() );
    colCount.setComparator( colCount.getComparator().reversed() );

    final var columns = getColumns();
    columns.add( colWord );
    columns.add( colCount );

    setMaxWidth( Double.MAX_VALUE );
    setPrefWidth( 128 );
    setColumnResizePolicy( CONSTRAINED_RESIZE_POLICY );
    getSortOrder().setAll( colCount, colWord );

    getStyleClass().add( "" );
  }

  private void initListeners( final Workspace workspace ) {
    final var property = workspace.localeProperty( KEY_LANGUAGE_LOCALE );
    property.addListener(
      ( c, o, n ) -> sTokenizer = createTokenizer( property.toLocale() )
    );
  }

  /**
   * Creates a tokenizer for English text (can handle most Latin languages).
   *
   * @return An English-based tokenizer for counting words.
   */
  private static Tokenizer createTokenizer( final Locale language ) {
    return TokenizerFactory.create( language );
  }

  private <E, T> TableColumn<E, T> createColumn( final String key ) {
    return new TableColumn<>( key );
  }

  private void setFontFamily( final String value ) {
    runLater( () -> setStyle( format( "-fx-font-family:'%s';", value ) ) );
  }

  /**
   * Represents the number of times a word appears in a document.
   */
  protected static final class StatEntry {
    private final StringProperty mWord;
    private final IntegerProperty mTally;

    public StatEntry( final String word, final int tally ) {
      mWord = new SimpleStringProperty( word );
      mTally = new SimpleIntegerProperty( tally );
    }

    private StringProperty wordProperty() {
      return mWord;
    }

    private IntegerProperty tallyProperty() {
      return mTally;
    }
  }
}
