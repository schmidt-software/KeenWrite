/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static com.keenwrite.Bootstrap.*;
import static java.lang.String.format;

/**
 * Launches the application using the {@link MainApp} class.
 *
 * <p>
 * This is required until modules are implemented, which may never happen
 * because the application should be ported away from Java and JavaFX.
 * </p>
 */
public final class Launcher {
  /**
   * Delegates to the application entry point.
   *
   * @param args Command-line arguments.
   */
  public static void main( final String[] args ) {
    try {
      showAppInfo();
      MainApp.main( args );
    } catch( final Error e ) {
      log( e );
    }
  }

  @SuppressWarnings( "RedundantStringFormatCall" )
  private static void showAppInfo() {
    out( format( "%s version %s", APP_TITLE, APP_VERSION ) );
    out( format( "Copyright 2016-%s White Magic Software, Ltd.", APP_YEAR ) );
    out( format( "Portions copyright 2015-2020 Karl Tauber." ) );
  }

  private static void out( final String s ) {
    System.out.println( s );
  }

  /**
   * Returns the application version number retrieved from the application
   * properties file. The properties file is generated at build time, which
   * keys off the repository.
   *
   * @return The application version number.
   * @throws RuntimeException An {@link IOException} occurred.
   */
  public static String getVersion() {
    try {
      final var properties = loadProperties( "app.properties" );
      return properties.getProperty( "application.version" );
    } catch( final Exception ex ) {
      throw new RuntimeException( ex );
    }
  }

  @SuppressWarnings( "SameParameterValue" )
  private static Properties loadProperties( final String resource )
    throws IOException {
    final var properties = new Properties();
    properties.load( getResourceAsStream( getResourceName( resource ) ) );
    return properties;
  }

  private static String getResourceName( final String resource ) {
    return format( "%s/%s", getPackagePath(), resource );
  }

  private static String getPackagePath() {
    return Launcher.class.getPackageName().replace( '.', '/' );
  }

  private static InputStream getResourceAsStream( final String resource ) {
    return Launcher.class.getClassLoader().getResourceAsStream( resource );
  }

  /**
   * Logs the message of an error to the console.
   *
   * @param error The fatal error that could not be handled.
   */
  private static void log( final Error error ) {
    var message = error.getMessage();

    if( message != null && message.toLowerCase().contains( "javafx" ) ) {
      message = "Re-run using a Java Runtime Environment that includes JavaFX.";
    }

    out( format( "ERROR: %s", message ) );
  }
}
