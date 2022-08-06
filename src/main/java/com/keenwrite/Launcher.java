/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite;

import com.keenwrite.cmdline.Arguments;
import com.keenwrite.cmdline.ColourScheme;
import com.keenwrite.cmdline.HeadlessApp;
import picocli.CommandLine;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.function.Consumer;

import static com.keenwrite.Bootstrap.*;
import static com.keenwrite.security.PermissiveCertificate.installTrustManager;
import static java.lang.String.format;
import static picocli.CommandLine.IParameterExceptionHandler;
import static picocli.CommandLine.ParameterException;
import static picocli.CommandLine.UnmatchedArgumentException.printSuggestions;

/**
 * Launches the application using the {@link MainApp} class.
 *
 * <p>
 * This is required until modules are implemented, which may never happen
 * because the application should be ported away from Java and JavaFX.
 * </p>
 */
public final class Launcher implements Consumer<Arguments> {

  /**
   * Needed for the GUI.
   */
  private final String[] mArgs;

  /**
   * Responsible for informing the user of an invalid command-line option,
   * along with suggestions as to the closest argument name that matches.
   */
  private static final class ArgHandler implements IParameterExceptionHandler {
    /**
     * Invoked by the command-line parser when an invalid option is provided.
     *
     * @param ex   Captures information about the parameter.
     * @param args Captures the complete command-line arguments.
     * @return The application exit code (non-zero).
     */
    public int handleParseException(
      final ParameterException ex, final String[] args ) {
      final var cmd = ex.getCommandLine();
      final var writer = cmd.getErr();
      final var spec = cmd.getCommandSpec();
      final var mapper = cmd.getExitCodeExceptionMapper();

      writer.println( ex.getMessage() );
      printSuggestions( ex, writer );
      writer.print( cmd.getHelp().fullSynopsis() );
      writer.printf( "Run '%s --help' for details.%n", spec.qualifiedName() );

      return mapper == null
        ? spec.exitCodeOnInvalidInput()
        : mapper.getExitCode( ex );
    }
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

  /**
   * Immediately exits the application.
   *
   * @param exitCode Code to provide back to the calling shell.
   */
  public static void terminate( final int exitCode ) {
    System.exit( exitCode );
  }

  private static void parse( final String[] args ) {
    assert args != null;

    final var arguments = new Arguments( new Launcher( args ) );
    final var parser = new CommandLine( arguments );

    parser.setColorScheme( ColourScheme.create() );
    parser.setParameterExceptionHandler( new ArgHandler() );
    parser.setUnmatchedArgumentsAllowed( false );

    final var exitCode = parser.execute( args );
    final var parseResult = parser.getParseResult();

    if( parseResult.isUsageHelpRequested() ) {
      terminate( exitCode );
    }
    else if( parseResult.isVersionHelpRequested() ) {
      showAppInfo();
      terminate( exitCode );
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
      message = "Run using a Java Runtime Environment that includes JavaFX.";
      out( "ERROR: %s", message );
    }
    else {
      error.printStackTrace( System.err );
    }
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
   * Delegates running the application via the command-line argument parser.
   * This is the main entry point for the application, regardless of whether
   * run from the command-line or as a GUI.
   *
   * @param args Command-line arguments.
   */
  public static void main( final String[] args ) {
    installTrustManager();
    parse( args );
  }

  /**
   * @param args Command-line arguments (passed into the GUI).
   */
  public Launcher( final String[] args ) {
    mArgs = args;
  }

  /**
   * Called after the arguments have been parsed.
   *
   * @param args The parsed command-line arguments.
   */
  @Override
  public void accept( final Arguments args ) {
    assert args != null;

    try {
      int argCount = mArgs.length;

      if( args.quiet() ) {
        argCount--;
      }
      else {
        showAppInfo();
      }

      if( args.debug() ) {
        argCount--;
      }
      else {
        MainApp.disableLogging();
      }

      if( argCount <= 0 ) {
        // When no command-line arguments are provided, launch the GUI.
        MainApp.main( mArgs );
      }
      else {
        // When command-line arguments are supplied, run in headless mode.
        HeadlessApp.main( args );
      }
    } catch( final Throwable t ) {
      log( t );
    }
  }

  private static void showAppInfo() {
    out( "%n%s version %s", APP_TITLE, APP_VERSION );
    out( "Copyright 2016-%s White Magic Software, Ltd.", APP_YEAR );
    out( "Portions copyright 2015-2020 Karl Tauber.%n" );
  }
}
