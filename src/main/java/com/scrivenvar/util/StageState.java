/*
 * Copyright 2020 Karl Tauber and White Magic Software, Ltd.
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
package com.scrivenvar.util;

import java.util.prefs.Preferences;

import javafx.application.Platform;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Saves and restores Stage state (window bounds, maximized, fullScreen).
 */
public class StageState {

  public static final String K_PANE_SPLIT_DEFINITION = "pane.split.definition";
  public static final String K_PANE_SPLIT_EDITOR = "pane.split.editor";
  public static final String K_PANE_SPLIT_PREVIEW = "pane.split.preview";

  private final Stage mStage;
  private final Preferences mState;

  private Rectangle normalBounds;
  private boolean runLaterPending;

  public StageState( final Stage stage, final Preferences state ) {
    mStage = stage;
    mState = state;

    restore();

    stage.addEventHandler( WindowEvent.WINDOW_HIDING, e -> save() );

    stage.xProperty().addListener( ( ob, o, n ) -> boundsChanged() );
    stage.yProperty().addListener( ( ob, o, n ) -> boundsChanged() );
    stage.widthProperty().addListener( ( ob, o, n ) -> boundsChanged() );
    stage.heightProperty().addListener( ( ob, o, n ) -> boundsChanged() );
  }

  private void save() {
    final Rectangle bounds = isNormalState() ? getStageBounds() : normalBounds;

    if( bounds != null ) {
      mState.putDouble( "windowX", bounds.getX() );
      mState.putDouble( "windowY", bounds.getY() );
      mState.putDouble( "windowWidth", bounds.getWidth() );
      mState.putDouble( "windowHeight", bounds.getHeight() );
    }

    mState.putBoolean( "windowMaximized", mStage.isMaximized() );
    mState.putBoolean( "windowFullScreen", mStage.isFullScreen() );
  }

  private void restore() {
    final double x = mState.getDouble( "windowX", Double.NaN );
    final double y = mState.getDouble( "windowY", Double.NaN );
    final double w = mState.getDouble( "windowWidth", Double.NaN );
    final double h = mState.getDouble( "windowHeight", Double.NaN );
    final boolean maximized = mState.getBoolean( "windowMaximized", false );
    final boolean fullScreen = mState.getBoolean( "windowFullScreen", false );

    if( !Double.isNaN( x ) && !Double.isNaN( y ) ) {
      mStage.setX( x );
      mStage.setY( y );
    } // else: default behavior is center on screen

    if( !Double.isNaN( w ) && !Double.isNaN( h ) ) {
      mStage.setWidth( w );
      mStage.setHeight( h );
    } // else: default behavior is use scene size

    if( fullScreen != mStage.isFullScreen() ) {
      mStage.setFullScreen( fullScreen );
    }

    if( maximized != mStage.isMaximized() ) {
      mStage.setMaximized( maximized );
    }
  }

  /**
   * Remembers the window bounds when the window is not iconified, maximized or
   * in fullScreen.
   */
  private void boundsChanged() {
    // avoid too many (and useless) runLater() invocations
    if( runLaterPending ) {
      return;
    }

    runLaterPending = true;

    // must use runLater() to ensure that change of all properties
    // (x, y, width, height, iconified, maximized and fullScreen)
    // has finished
    Platform.runLater( () -> {
      runLaterPending = false;

      if( isNormalState() ) {
        normalBounds = getStageBounds();
      }
    } );
  }

  private boolean isNormalState() {
    return !mStage.isIconified() &&
        !mStage.isMaximized() &&
        !mStage.isFullScreen();
  }

  private Rectangle getStageBounds() {
    return new Rectangle(
        mStage.getX(),
        mStage.getY(),
        mStage.getWidth(),
        mStage.getHeight()
    );
  }
}
