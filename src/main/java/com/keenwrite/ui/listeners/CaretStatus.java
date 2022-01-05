/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.ui.listeners;

import com.keenwrite.editors.common.Caret;
import com.keenwrite.events.CaretMovedEvent;
import com.keenwrite.events.WordCountEvent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.greenrobot.eventbus.Subscribe;

import static com.keenwrite.events.Bus.register;
import static javafx.application.Platform.runLater;
import static javafx.geometry.Pos.BASELINE_CENTER;

/**
 * Responsible for updating the UI whenever the caret changes position.
 * Only one instance of {@link CaretStatus} is allowed, which prevents
 * duplicate adds to the observable property.
 */
public class CaretStatus extends VBox {

  /**
   * Use an instance of {@link Label} for its built-in CSS style class.
   */
  private final Label mStatusText = new Label();

  /**
   * Contains caret position information within an editor.
   */
  private volatile Caret mCaret = Caret.builder().build();

  /**
   * Approximate number of words in the document.
   */
  private volatile int mCount;

  public CaretStatus() {
    setAlignment( BASELINE_CENTER );
    getChildren().add( mStatusText );
    register( this );
  }

  @Subscribe
  public void handle( final WordCountEvent event ) {
    mCount = event.getCount();
    updateStatus( mCaret, mCount );
  }

  @Subscribe
  public void handle( final CaretMovedEvent event ) {
    mCaret = event.getCaret();
    updateStatus( mCaret, mCount );
  }

  private void updateStatus( final Caret caret, final int count ) {
    assert caret != null;
    runLater( () -> mStatusText.setText( caret + " | " + count ) );
  }
}
