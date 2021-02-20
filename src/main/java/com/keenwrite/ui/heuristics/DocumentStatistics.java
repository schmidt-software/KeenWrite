/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.ui.heuristics;

import com.keenwrite.events.DocumentChangedEvent;
import com.keenwrite.preview.HtmlPanel;
import com.keenwrite.util.MurmurHash;
import com.whitemagicsoftware.wordcount.TokenizerFactory;
import javafx.scene.control.TableView;
import org.greenrobot.eventbus.Subscribe;
import org.jsoup.Jsoup;

import static com.keenwrite.events.Bus.register;
import static java.util.Locale.ENGLISH;

/**
 * Responsible for displaying document statistics, such as word count and
 * word frequency.
 */
public class DocumentStatistics extends TableView {
  public DocumentStatistics() {
    register( this );
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
    final var tokenizer = TokenizerFactory.create( ENGLISH );
    final var statistics = tokenizer.tokenize( event.getDocument() );

    for( final var entry : statistics.entrySet() ) {
      System.out.println( entry.getKey() + "/" + entry.getValue()[ 0 ] );
    }
  }
}
