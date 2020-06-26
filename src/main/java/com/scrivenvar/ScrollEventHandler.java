/*
 * Copyright 2020 White Magic Software, Ltd.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.scrivenvar;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.skin.ScrollBarSkin;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.StyleClassedTextArea;

import javax.swing.*;

import static javafx.geometry.Orientation.VERTICAL;

/**
 * Converts scroll events from {@link VirtualizedScrollPane} scroll bars to
 * an instance of {@link JScrollBar}.
 * <p>
 * Called to synchronize the scrolling areas for either scrolling with the
 * mouse or scrolling using the scrollbar's thumb. Both are required to avoid
 * scrolling on the estimatedScrollYProperty that occurs when text events
 * fire. Scrolling performed for text events are handled separately to ensure
 * the preview panel scrolls to the same position in the Markdown editor,
 * taking into account things like images, tables, and other potentially
 * long vertical presentation items.
 * </p>
 */
public final class ScrollEventHandler implements EventHandler<Event> {

  private final class MouseHandler implements EventHandler<MouseEvent> {
    private final EventHandler<? super MouseEvent> mOldHandler;

    /**
     * Constructs a new handler for mouse scrolling events.
     *
     * @param oldHandler Receives the event after scrolling takes place.
     */
    private MouseHandler( final EventHandler<? super MouseEvent> oldHandler ) {
      mOldHandler = oldHandler;
    }

    @Override
    public void handle( final MouseEvent event ) {
      ScrollEventHandler.this.handle( event );
      mOldHandler.handle( event );
    }
  }

  private final class ScrollHandler implements EventHandler<ScrollEvent> {
    @Override
    public void handle( final ScrollEvent event ) {
      ScrollEventHandler.this.handle( event );
    }
  }

  private final VirtualizedScrollPane<StyleClassedTextArea> mEditorScrollPane;
  private final JScrollBar mPreviewScrollBar;
  private final BooleanProperty mEnabled = new SimpleBooleanProperty();

  /**
   * @param editorScrollPane Scroll event source (human movement).
   * @param previewScrollBar Scroll event destination (corresponding movement).
   */
  public ScrollEventHandler(
      final VirtualizedScrollPane<StyleClassedTextArea> editorScrollPane,
      final JScrollBar previewScrollBar ) {
    mEditorScrollPane = editorScrollPane;
    mPreviewScrollBar = previewScrollBar;

    mEditorScrollPane.addEventFilter( ScrollEvent.ANY, new ScrollHandler() );

    final var thumb = getVerticalScrollBarThumb( mEditorScrollPane );
    thumb.setOnMouseDragged( new MouseHandler( thumb.getOnMouseDragged() ) );
  }

  /**
   * Gets a property intended to be bound to selected property of the tab being
   * scrolled. This is required because there's only one preview pane but
   * multiple editor panes. Each editor pane maintains its own scroll position.
   *
   * @return A {@link BooleanProperty} representing whether the scroll
   * events for this tab are to be executed.
   */
  public BooleanProperty enabledProperty() {
    return mEnabled;
  }

  /**
   * Scrolls the preview scrollbar relative to the edit scrollbar. Algorithm
   * is based on Karl Tauber's ratio calculation.
   *
   * @param event Unused; either {@link MouseEvent} or {@link ScrollEvent}
   */
  @Override
  public void handle( final Event event ) {
    if( isEnabled() ) {
      final var eScrollPane = getEditorScrollPane();
      final int eScrollY =
          eScrollPane.estimatedScrollYProperty().getValue().intValue();
      final int eHeight = (int)
          (eScrollPane.totalHeightEstimateProperty().getValue().intValue()
              - eScrollPane.getHeight());
      final double eRatio = eHeight > 0
          ? Math.min( Math.max( eScrollY / (float) eHeight, 0 ), 1 ) : 0;

      final var pScrollBar = getPreviewScrollBar();
      final var pHeight = pScrollBar.getMaximum() - pScrollBar.getHeight();
      final var pScrollY = (int) (pHeight * eRatio);

      pScrollBar.setValue( pScrollY );
      pScrollBar.getParent().repaint();
    }
  }

  private StackPane getVerticalScrollBarThumb(
      final VirtualizedScrollPane<StyleClassedTextArea> pane ) {
    final ScrollBar scrollBar = getVerticalScrollBar( pane );
    final ScrollBarSkin skin = (ScrollBarSkin) (scrollBar.skinProperty().get());

    for( final Node node : skin.getChildren() ) {
      // Brittle, but what can you do?
      if( node.getStyleClass().contains( "thumb" ) ) {
        return (StackPane) node;
      }
    }

    throw new IllegalArgumentException( "No scroll bar skin found." );
  }

  private ScrollBar getVerticalScrollBar(
      final VirtualizedScrollPane<StyleClassedTextArea> pane ) {

    for( final Node node : pane.getChildrenUnmodifiable() ) {
      if( node instanceof ScrollBar ) {
        final ScrollBar scrollBar = (ScrollBar) node;

        if( scrollBar.getOrientation() == VERTICAL ) {
          return scrollBar;
        }
      }
    }

    throw new IllegalArgumentException( "No vertical scroll pane found." );
  }

  private boolean isEnabled() {
    // TODO: As a minor optimization, when this is set to false, it could remove
    // the MouseHandler and ScrollHandler so that events only dispatch to one
    // object (instead of one per editor tab).
    return mEnabled.get();
  }

  private VirtualizedScrollPane<StyleClassedTextArea> getEditorScrollPane() {
    return mEditorScrollPane;
  }

  private JScrollBar getPreviewScrollBar() {
    return mPreviewScrollBar;
  }
}
