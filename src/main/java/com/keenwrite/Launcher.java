/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite;

import com.keenwrite.cmdline.Arguments;
import com.keenwrite.cmdline.ColourScheme;
import picocli.CommandLine;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static com.keenwrite.Bootstrap.*;
import static com.keenwrite.PermissiveCertificate.installTrustManager;
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
      installTrustManager();
      showAppInfo();
      parse( args );
    } catch( final Throwable t ) {
      log( t );
    }
  }

  private static void parse( final String[] args ) {
    final var arguments = new Arguments( args );
    final var parser = new CommandLine( arguments );
    parser.setColorScheme( ColourScheme.create() );

    final var exitCode = parser.execute( args );
    final var parseResult = parser.getParseResult();

    if( parseResult.isUsageHelpRequested() ) {
      System.exit( exitCode );
    }
  }

  private static void showAppInfo() {
    out( "%n%s version %s", APP_TITLE, APP_VERSION );
    out( "Copyright 2016-%s White Magic Software, Ltd.", APP_YEAR );
    out( "Portions copyright 2015-2020 Karl Tauber.%n" );
  }

  /**
   * Writes the given placeholder text to standard output with a new line
   * appended.
   *
   * @param message The format string specifier.
   * @param args    The arguments to substitute into the format string.
   */
  private static void out( final String message, final Object... args ) {
    System.out.printf( format( "%s%n", message ), args );
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
  private static void log( final Throwable error ) {
    var message = error.getMessage();

    if( message != null && message.toLowerCase().contains( "javafx" ) ) {
      message = "Re-run using a Java Runtime Environment that includes JavaFX.";
    }

    out( "ERROR: %s", message );
  }
}
