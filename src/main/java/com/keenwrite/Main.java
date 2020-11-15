/*
 * Copyright 2020 Karl Tauber and White Magic Software, Ltd.
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
package com.keenwrite;

import com.keenwrite.preferences.FilePreferencesFactory;
import com.keenwrite.service.Options;
import com.keenwrite.service.Snitch;
import com.keenwrite.util.ResourceWalker;
import com.keenwrite.util.StageState;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.logging.LogManager;

import static com.keenwrite.Bootstrap.APP_TITLE;
import static com.keenwrite.Constants.*;
import static com.keenwrite.StatusBarNotifier.clue;
import static java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment;
import static java.awt.font.TextAttribute.*;
import static javafx.scene.input.KeyCode.F11;
import static javafx.scene.input.KeyEvent.KEY_PRESSED;

/**
 * Application entry point. The application allows users to edit plain text
 * files in a markup notation and see a real-time preview of the formatted
 * output.
 */
public final class Main extends Application {

  static {
    // Suppress logging to standard output.
    LogManager.getLogManager().reset();

    // Suppress logging to standard error.
    System.err.close();
  }

  private final Options mOptions = Services.load( Options.class );
  private final Snitch mSnitch = Services.load( Snitch.class );

  private final Thread mSnitchThread = new Thread( getSnitch() );
  private final MainWindow mMainWindow = new MainWindow();

  @SuppressWarnings({"FieldCanBeLocal"})
  private StageState mStageState;

  /**
   * Application entry point.
   *
   * @param args Command-line arguments.
   */
  public static void main( final String[] args ) {
    initPreferences();
    initFonts();
    launch( args );
  }

  /**
   * JavaFX entry point.
   *
   * @param stage The primary application stage.
   */
  @Override
  public void start( final Stage stage ) {
    initState( stage );
    initStage( stage );
    initSnitch();

    stage.show();

    // After the stage is visible, the panel dimensions are
    // known, which allows scaling images to fit the preview panel.
    getMainWindow().init();
  }

  /**
   * This needs to run before the windowing system kicks in, otherwise the
   * fonts will not be found.
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  public static void initFonts() {
    final var ge = getLocalGraphicsEnvironment();

    try {
      ResourceWalker.walk(
          FONT_DIRECTORY, path -> {
            final var uri = path.toUri();
            final var filename = path.toString();

            try( final var is = openFont( uri, filename ) ) {
              final var font = Font.createFont( Font.TRUETYPE_FONT, is );
              final Map attributes = font.getAttributes();

              attributes.put( LIGATURES, LIGATURES_ON );
              attributes.put( KERNING, KERNING_ON );
              ge.registerFont( font.deriveFont( attributes ) );
            } catch( final Exception e ) {
              clue( e );
            }
          }
      );
    } catch( final Exception e ) {
      clue( e );
    }
  }

  private static InputStream openFont( final URI uri, final String filename )
      throws IOException {
    return uri.getScheme().equals( "jar" )
        ? Main.class.getResourceAsStream( filename )
        : new FileInputStream( filename );
  }

  /**
   * Sets the factory used for reading user preferences.
   */
  private static void initPreferences() {
    System.setProperty(
        "java.util.prefs.PreferencesFactory",
        FilePreferencesFactory.class.getName()
    );
  }

  private void initState( final Stage stage ) {
    mStageState = new StageState( stage, getOptions().getState() );
  }

  private void initStage( final Stage stage ) {
    stage.getIcons().addAll(
        createImage( FILE_LOGO_16 ),
        createImage( FILE_LOGO_32 ),
        createImage( FILE_LOGO_128 ),
        createImage( FILE_LOGO_256 ),
        createImage( FILE_LOGO_512 ) );
    stage.setTitle( APP_TITLE );
    stage.setScene( getScene() );

    stage.addEventHandler( KEY_PRESSED, event -> {
      if( F11.equals( event.getCode() ) ) {
        stage.setFullScreen( !stage.isFullScreen() );
      }
    } );
  }

  /**
   * Watch for file system changes.
   */
  private void initSnitch() {
    getSnitchThread().start();
  }

  /**
   * Stops the snitch service, if its running.
   *
   * @throws InterruptedException Couldn't stop the snitch thread.
   */
  @Override
  public void stop() throws InterruptedException {
    getSnitch().stop();

    final Thread thread = getSnitchThread();
    thread.interrupt();
    thread.join();
  }

  private Snitch getSnitch() {
    return mSnitch;
  }

  private Thread getSnitchThread() {
    return mSnitchThread;
  }

  private Options getOptions() {
    return mOptions;
  }

  private MainWindow getMainWindow() {
    return mMainWindow;
  }

  private Scene getScene() {
    return getMainWindow().getScene();
  }

  private Image createImage( final String filename ) {
    return new Image( filename );
  }

  /**
   * This is here to suppress an IDE warning, the method is not used.
   */
  public StageState getStageState() {
    return mStageState;
  }
}
