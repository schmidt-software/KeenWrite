/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Properties;

import static com.keenwrite.Bootstrap.APP_TITLE;
import static java.lang.String.format;

/**
 * Launches the application using the {@link Main} class.
 *
 * <p>
 * This is required until modules are implemented, which may never happen
 * because the application should be ported away from Java and JavaFX.
 * </p>
 */
public class Launcher {
  /**
   * Delegates to the application entry point.
   *
   * @param args Command-line arguments.
   */
  public static void main( final String[] args ) throws IOException {
    showAppInfo();
    Main.main( args );
  }

  @SuppressWarnings("RedundantStringFormatCall")
  private static void showAppInfo() throws IOException {
    out( format( "%s version %s", APP_TITLE, getVersion() ) );
    out( format( "Copyright %s White Magic Software, Ltd.", getYear() ) );
    out( format( "Portions copyright 2020 Karl Tauber." ) );
  }

  private static void out( final String s ) {
    System.out.println( s );
  }

  private static String getVersion() throws IOException {
    final Properties properties = loadProperties( "app.properties" );
    return properties.getProperty( "application.version" );
  }

  private static String getYear() {
    return Integer.toString( Calendar.getInstance().get( Calendar.YEAR ) );
  }

  @SuppressWarnings("SameParameterValue")
  private static Properties loadProperties( final String resource )
      throws IOException {
    final Properties properties = new Properties();
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
}
