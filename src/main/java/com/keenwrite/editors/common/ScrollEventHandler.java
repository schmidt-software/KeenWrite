/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.editors.common;

import com.keenwrite.events.ScrollLockEvent;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.skin.ScrollBarSkin;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.greenrobot.eventbus.Subscribe;

import javax.swing.*;
import java.util.function.Consumer;

import static com.keenwrite.events.Bus.register;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static javafx.geometry.Orientation.VERTICAL;
import static javax.swing.SwingUtilities.invokeLater;

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

  private boolean mLocked;

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

    initVerticalScrollBarThumb(
      mEditorScrollPane,
      thumb -> {
        final var handler = new MouseHandler( thumb.getOnMouseDragged() );
        thumb.setOnMouseDragged( handler );
      }
    );

    register( this );
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
    invokeLater( () -> {
      if( isEnabled() ) {
        // e prefix is for editor pane.
        final var eScrollPane = getEditorScrollPane();
        final var eScrollY =
          eScrollPane.estimatedScrollYProperty().getValue().intValue();
        final var eHeight = (int)
          (eScrollPane.totalHeightEstimateProperty().getValue().intValue()
            - eScrollPane.getHeight());
        final var eRatio = eHeight > 0
          ? min( max( eScrollY / (float) eHeight, 0 ), 1 ) : 0;

        // p prefix is for preview pane.
        final var pScrollBar = getPreviewScrollBar();
        final var pHeight = pScrollBar.getMaximum() - pScrollBar.getHeight();
        final var pScrollY = (int) (pHeight * eRatio);

        pScrollBar.setValue( pScrollY );
        pScrollBar.getParent().repaint();
      }
    } );
  }

  @Subscribe
  public void handle( final ScrollLockEvent event ) {
    mLocked = event.isLocked();
  }

  private void initVerticalScrollBarThumb(
    final VirtualizedScrollPane<StyleClassedTextArea> pane,
    final Consumer<StackPane> consumer ) {
    // When the skin property is set, the stack pane is available (not null).
    getVerticalScrollBar( pane ).skinProperty().addListener( ( c, o, n ) -> {
      for( final var node : ((ScrollBarSkin) n).getChildren() ) {
        // Brittle, but what can you do?
        if( node.getStyleClass().contains( "thumb" ) ) {
          consumer.accept( (StackPane) node );
        }
      }
    } );
  }

  /**
   * Returns the vertical {@link ScrollBar} instance associated with the
   * given scroll pane. This is {@code null}-safe because the scroll pane
   * initializes its vertical {@link ScrollBar} upon construction.
   *
   * @param pane The scroll pane that contains a vertical {@link ScrollBar}.
   * @return The vertical {@link ScrollBar} associated with the scroll pane.
   * @throws IllegalStateException Could not obtain the vertical scroll bar.
   */
  private ScrollBar getVerticalScrollBar(
    final VirtualizedScrollPane<StyleClassedTextArea> pane ) {

    for( final var node : pane.getChildrenUnmodifiable() ) {
      if( node instanceof final ScrollBar scrollBar &&
        scrollBar.getOrientation() == VERTICAL ) {
        return scrollBar;
      }
    }

    throw new IllegalStateException( "No vertical scroll bar found." );
  }

  private boolean isEnabled() {
    // TODO: As a minor optimization, when this is set to false, it could remove
    // the MouseHandler and ScrollHandler so that events only dispatch to one
    // object (instead of one per editor tab).
    return mEnabled.get() && !mLocked;
  }

  private VirtualizedScrollPane<StyleClassedTextArea> getEditorScrollPane() {
    return mEditorScrollPane;
  }

  private JScrollBar getPreviewScrollBar() {
    return mPreviewScrollBar;
  }
}
