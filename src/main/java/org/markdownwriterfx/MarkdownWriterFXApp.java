/*
 * Copyright (c) 2015 Karl Tauber <karl at jformdesigner dot com>
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
package org.markdownwriterfx;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import static java.nio.file.Paths.get;
import java.util.List;
import java.util.prefs.Preferences;
import static java.util.prefs.Preferences.userRoot;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.markdownwriterfx.options.Options;
import org.markdownwriterfx.util.StageState;

/**
 * Markdown Writer FX application.
 *
 * @author Karl Tauber
 */
public final class MarkdownWriterFXApp extends Application implements ApplicationProperty {

  private static Application app;

  private MainWindow mainWindow;
  private StageState stageState;
  private PropertiesConfiguration configuration;

  public static void main( String[] args ) {
    launch( args );
  }

  /**
   * Application entry point.
   *
   * @param stage The primary application stage.
   *
   * @throws Exception Could not read configuration file.
   */
  @Override
  public void start( Stage stage ) throws Exception {
    initApplication();
    initConfiguration();
    initOptions();
    initWindow();
    initState( stage );
    initStage( stage );

    stage.show();
  }

  public PropertiesConfiguration getConfiguration() {
    return this.configuration;
  }

  private String getApplicationTitle() {
    return getProperty( "application.title", "Markdown Writer FX" );
  }

  @Override
  public String getProperty( String property, String defaultValue ) {
    return getConfiguration().getString( property, defaultValue );
  }
  
  @Override
  public List<Object> getPropertyList(String property, List<String> defaults ) {
    return getConfiguration().getList( property );
  }

  private void initApplication() {
    app = this;
  }

  protected void initConfiguration()
    throws ConfigurationException, URISyntaxException, IOException {
    setConfiguration( createPropertiesConfiguration() );
  }

  private PropertiesConfiguration createPropertiesConfiguration()
    throws ConfigurationException {
    final URL url = getConfigurationSource();

    return url == null
      ? new PropertiesConfiguration()
      : new PropertiesConfiguration( url );
  }

  private URL getConfigurationSource() {
    return getClass().getResource( getConfigurationName() );
  }

  private String getConfigurationName() {
    return "settings.properties";
  }

  protected void initOptions() {
    Options.load( getOptions() );
  }

  private void initWindow() {
    setWindow( new MainWindow( this ) );
  }

  private void initState( Stage stage ) {
    stageState = new StageState( stage, getState() );
  }

  private void initStage( Stage stage ) {
    stage.getIcons().addAll(
      new Image( "org/markdownwriterfx/markdownwriterfx16.png" ),
      new Image( "org/markdownwriterfx/markdownwriterfx32.png" ),
      new Image( "org/markdownwriterfx/markdownwriterfx128.png" ),
      new Image( "org/markdownwriterfx/markdownwriterfx256.png" ),
      new Image( "org/markdownwriterfx/markdownwriterfx512.png" ) );
    stage.setTitle( getApplicationTitle() );
    stage.setScene( getScene() );
  }

  private Scene getScene() {
    return getMainWindow().getScene();
  }

  protected MainWindow getMainWindow() {
    return this.mainWindow;
  }

  private void setWindow( MainWindow mainWindow ) {
    this.mainWindow = mainWindow;
  }

  private StageState getStageState() {
    return this.stageState;
  }

  private void setStageState( StageState stageState ) {
    this.stageState = stageState;
  }

  private static Application getApplication() {
    return app;
  }

  protected void setConfiguration( PropertiesConfiguration configuration ) {
    this.configuration = configuration;
  }

  public static void showDocument( String uri ) {
    getApplication().getHostServices().showDocument( uri );
  }

  static private Preferences getRootPreferences() {
    return userRoot().node( "markdownwriterfx" );
  }

  private static Preferences getOptions() {
    return getRootPreferences().node( "options" );
  }

  public static Preferences getState() {
    return getRootPreferences().node( "state" );
  }

  /**
   * Unused.
   *
   * @return
   *
   * @throws URISyntaxException
   */
  private Path getConfigurationPath() throws URISyntaxException {
    final Path appDir = getApplicationDirectory();
    return get( appDir.toString(), getConfigurationName() );
  }

  /**
   * Unused.
   *
   * @return
   *
   * @throws URISyntaxException
   */
  private Path getApplicationDirectory() throws URISyntaxException {
    final Path appPath = get( getApplicationPath() );
    return appPath.getParent();
  }

  /**
   * Unused. Returns the path to the application's start-up directory.
   *
   * @return A Path where the main class is running.
   */
  private String getApplicationPath() throws URISyntaxException {
    return getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
  }

}
