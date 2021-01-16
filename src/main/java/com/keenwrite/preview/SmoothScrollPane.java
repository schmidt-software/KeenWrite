/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.preview;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

import static java.awt.Scrollbar.VERTICAL;
import static java.lang.Math.min;
import static javafx.animation.Interpolator.EASE_BOTH;

/**
 * Responsible for smoothing out the scrolling using an easing algorithm.
 *
 * @deprecated Does not refresh properly, has tearing of large images, and
 * jerks around when dragging the thumb (track).
 */
@Deprecated
public class SmoothScrollPane extends JScrollPane {

  public SmoothScrollPane( final Component component ) {
    super( component );
    setVerticalScrollBarPolicy( VERTICAL_SCROLLBAR_ALWAYS );
  }

  @Override
  public ScrollBar createVerticalScrollBar() {
    return new SmoothScrollBar( VERTICAL );
  }

  private class SmoothScrollBar extends ScrollBar implements Consumer<Integer> {
    private final Animator mAnimator = new Animator( this, () -> {
      // Fails to fix refresh problems when scrolling finishes. This is the
      // reason the class is deprecated. Calling invokeLater helps a little.
      SmoothScrollPane.this.getViewport().revalidate();
      revalidate();
      repaint();
    } );

    public SmoothScrollBar( final int orientation ) {
      super( orientation );
    }

    @Override
    public void setValue( final int nPos ) {
      final var oPos = getModel().getValue();

      mAnimator.stop();
      mAnimator.restart( oPos, nPos, 250 );
      new Thread( mAnimator ).start();
    }

    @Override
    public void accept( final Integer nPos ) {
      super.setValue( nPos );
    }
  }

  private static class Animator implements Runnable {
    private final Consumer<Integer> mAction;
    private final Runnable mComplete;

    private int mOldPos;
    private int mNewPos;
    private long mBegan;
    private long mEnded;
    private volatile boolean mRunning;

    public Animator( final Consumer<Integer> action, final Runnable complete ) {
      mAction = action;
      mComplete = complete;
    }

    /**
     * @param oPos Old scroll bar position.
     * @param nPos New scroll bar position.
     * @param time Total time to complete the scroll event (in milliseconds).
     */
    public void restart( final int oPos, final int nPos, final int time ) {
      mOldPos = oPos;
      mNewPos = nPos;
      mBegan = System.nanoTime();
      mEnded = time * 1_000_000L;
      mRunning = true;
    }

    public void stop() {
      mRunning = false;
    }

    @Override
    public void run() {
      double ratio;

      do {
        ratio = min( (double) (System.nanoTime() - mBegan) / mEnded, 1.0 );
        final int nPos = EASE_BOTH.interpolate( mOldPos, mNewPos, ratio );

        mAction.accept( nPos );
      } while( ratio <= 1 && mRunning );

      mComplete.run();
    }
  }
}
