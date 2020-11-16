/* Copyright 2020 White Magic Software, Ltd.
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

import com.keenwrite.definition.DefinitionView;
import com.keenwrite.io.File;
import com.keenwrite.io.MediaType;
import com.keenwrite.preview.HtmlPreview;
import com.keenwrite.service.Options;
import com.keenwrite.service.Snitch;
import com.keenwrite.ui.ApplicationMenuBar;
import com.keenwrite.util.StageState;
import com.panemu.tiwulfx.control.dock.DetachableTabPane;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.controlsfx.control.StatusBar;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.LogManager;

import static com.keenwrite.Bootstrap.APP_TITLE;
import static com.keenwrite.Constants.*;
import static com.keenwrite.io.MediaType.*;
import static com.keenwrite.preferences.UserPreferences.initPreferences;
import static com.keenwrite.util.FontLoader.initFonts;
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

  //private final MainView mMainView = new MainView();

  @SuppressWarnings({"FieldCanBeLocal", "unused", "RedundantSuppression"})
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
    initSnitch();
    initState( stage );
    initStage( stage );
    initIcons( stage );
    initScene( stage );

    stage.show();

    // After the stage is visible, the panel dimensions are
    // known, which allows scaling images to fit the preview panel.
    //getMainView().init();
  }

  private void initState( final Stage stage ) {
    mStageState = new StageState( stage, getOptions().getState() );
  }

  private void initStage( final Stage stage ) {
    stage.setTitle( APP_TITLE );

    stage.addEventHandler( KEY_PRESSED, event -> {
      if( F11.equals( event.getCode() ) ) {
        stage.setFullScreen( !stage.isFullScreen() );
      }
    } );
  }

  private void initIcons( final Stage stage ) {
    stage.getIcons().addAll(
        createImage( FILE_LOGO_16 ),
        createImage( FILE_LOGO_32 ),
        createImage( FILE_LOGO_128 ),
        createImage( FILE_LOGO_256 ),
        createImage( FILE_LOGO_512 )
    );
  }

  private void initScene( final Stage stage ) {
    final var applicationView = new BorderPane();

    applicationView.setTop( createMenuBar() );
    applicationView.setBottom( getStatusBar() );
    applicationView.setCenter( createApplicationContent() );

    final var scene = new Scene( applicationView );
    stage.setScene( scene );
  }

  private Node createMenuBar() {
    final var menuBar = new ApplicationMenuBar();
    return menuBar.createMenuBar();
  }

  private StatusBar getStatusBar() {
    return StatusBarNotifier.getStatusBar();
  }

  private SplitPane createApplicationContent() {
    final var splitPane = new SplitPane();

    final var workspace = new Workspace( "default" );
    final var files = bin( workspace.restoreFiles() );
    MediaType cMediaType = UNDEFINED;
    DetachableTabPane cTabPane = new DetachableTabPane();

    for( final var file : files ) {
      if( !file.isMediaType( cMediaType ) ) {
        cTabPane = new DetachableTabPane();
        splitPane.getItems().add( cTabPane );
        cMediaType = file.getMediaType();
      }

      final var controller = createController( file );
      cTabPane.addTab( controller.getFilename(), controller.getView() );
    }

    cTabPane = new DetachableTabPane();
    cTabPane.addTab( "HTML", new HtmlPreview() );
    splitPane.getItems().add( cTabPane );

    return splitPane;
  }

  /**
   * Creates bins for the different {@link MediaType}s, which eventually are
   * added to the UI as separate tab panes. If ever a general-purpose scene
   * exporter is developed to serialize a scene to an FXML file, this could
   * be replaced by such a class.
   * <p>
   * When binning the files, this makes sure that at least one file exists
   * for every type. If the user has opted to close a particular type (such
   * as the definition pane), the view will suppressed elsewhere.
   * </p>
   * <p>
   * The order that the binned files are returned will be reflected in the
   * order that the corresponding panes are rendered in the UI. Each different
   * {@link MediaType} will be created in its own pane.
   * </p>
   *
   * @param files The files to bin by {@link MediaType}.
   * @return An in-order list of files, first by structured definition files,
   * then by plain text documents.
   */
  private List<File> bin( final List<File> files ) {
    final var linkedMap = new LinkedHashMap<MediaType, List<File>>();
    linkedMap.put( TEXT_YAML, new ArrayList<>() );
    linkedMap.put( TEXT_MARKDOWN, new ArrayList<>() );
    linkedMap.put( UNDEFINED, new ArrayList<>() );

    for( final var file : files ) {
      final var list = linkedMap.computeIfAbsent(
          file.getMediaType(), k -> new ArrayList<>()
      );

      list.add( file );
    }

    final var definitions = linkedMap.get( TEXT_YAML );
    final var documents = linkedMap.get( TEXT_MARKDOWN );
    final var undefined = linkedMap.get( UNDEFINED );

    if( definitions.isEmpty() ) {
      definitions.add( new File( DEFINITION_NAME ) );
    }

    if( documents.isEmpty() ) {
      documents.add( new File( DOCUMENT_NAME ) );
    }

    final var result = new ArrayList<File>( files.size() );
    result.addAll( definitions );
    result.addAll( documents );
    result.addAll( undefined );

    return result;
  }

  private FileController<? extends Node> createController( final File file ) {
    return switch( file.getMediaType() ) {
      case TEXT_YAML -> new FileController<>(
          file.toPath(), new DefinitionView() );
      case TEXT_MARKDOWN -> new FileController<>(
          file.toPath(), new FileEditorView( file.toPath() ) );
      default -> new FileController<>(
          file.toPath(), new FileEditorView( file.toPath() ) );
    };
  }

  /**
   * Watch for file system changes.
   */
  private void initSnitch() {
    getSnitch().start();
  }

  /**
   * Stops the snitch service, if its running.
   */
  @Override
  public void stop() {
    getSnitch().stop();
  }

  private Snitch getSnitch() {
    return mSnitch;
  }

  private Options getOptions() {
    return mOptions;
  }

  private Image createImage( final String filename ) {
    return new Image( filename );
  }
}
