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

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.skin.ScrollBarSkin;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.StyleClassedTextArea;

import javax.swing.*;

import static javafx.geometry.Orientation.VERTICAL;

/**
 * Converts scroll events from {@link VirtualizedScrollPane} scroll bars to
 * an instance of {@link JScrollBar}.
 */
public final class ScrollBarDragHandler implements EventHandler<MouseEvent> {
  private final VirtualizedScrollPane<StyleClassedTextArea> mEditorScrollPane;
  private final JScrollBar mPreviewScrollBar;
  private final EventHandler<? super MouseEvent> mOldHandler;

  /**
   * @param editorScrollPane Scroll event source (human movement).
   * @param previewScrollBar Scroll event destination (corresponding movement).
   */
  public ScrollBarDragHandler(
      final VirtualizedScrollPane<StyleClassedTextArea> editorScrollPane,
      final JScrollBar previewScrollBar ) {
    mEditorScrollPane = editorScrollPane;
    mPreviewScrollBar = previewScrollBar;

//    mEditorScrollPane.estimatedScrollYProperty().addObserver( c -> {
//      System.out.println("SCROLL SCROLL THE BOAT");
//    });

    final var thumb = getVerticalScrollBarThumb( mEditorScrollPane );
    mOldHandler = thumb.getOnMouseDragged();
    thumb.setOnMouseDragged( this );
  }

  /**
   * Called to synchronize the scrolling areas. This will suppress any
   * scroll events that happen shortly after the user has typed a key.
   * See {@link Constants#KEYBOARD_SCROLL_DELAY} for details.
   */
  @Override
  public void handle( final MouseEvent event ) {
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
    mOldHandler.handle( event );
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

  private VirtualizedScrollPane<StyleClassedTextArea> getEditorScrollPane() {
    return mEditorScrollPane;
  }

  private JScrollBar getPreviewScrollBar() {
    return mPreviewScrollBar;
  }
}
