/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite;

import com.keenwrite.constants.Constants;
import com.keenwrite.io.UserDataDir;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.Properties;

import static com.keenwrite.events.StatusEvent.clue;

/**
 * Responsible for loading the bootstrap.properties file, which is
 * tactically located outside the standard resource reverse domain name
 * namespace to avoid hard-coding the application name in many places.
 * Instead, the application name is located in the bootstrap file, which is
 * then used to look up the remaining settings.
 * <p>
 * See {@link Constants#PATH_PROPERTIES_SETTINGS} for details.
 * </p>
 */
public final class Bootstrap {
  private static final String PATH_BOOTSTRAP = "/bootstrap.properties";

  /**
   * Must be populated before deriving the app title (order matters).
   */
  private static final Properties sP = new Properties();

  public static String APP_TITLE;
  public static String APP_VERSION;
  public static String CONTAINER_VERSION;

  public static final String APP_TITLE_ABBR = "kwr";
  public static final String APP_TITLE_LOWERCASE;
  public static final String APP_VERSION_CLEAN;
  public static final String APP_YEAR;

  public static final Path USER_DATA_DIR;
  public static final File USER_CACHE_DIR;

  static {
    try( final var in = openResource( PATH_BOOTSTRAP ) ) {
      sP.load( in );

      APP_TITLE = sP.getProperty( "application.title" );
      CONTAINER_VERSION = sP.getProperty( "container.version" );
    } catch( final Exception ex ) {
      APP_TITLE = "KeenWrite";

      // Bootstrap properties cannot be found, use a default value.
      final var fmt = "Unable to load %s resource, applying defaults.%n";
      clue( ex, fmt, PATH_BOOTSTRAP );

      // There's no way to know what container version is compatible. This
      // value will cause a failure when downloading the container,
      CONTAINER_VERSION = "1.0.0";
    }

    APP_TITLE_LOWERCASE = APP_TITLE.toLowerCase();

    try {
      APP_VERSION = Launcher.getVersion();
    } catch( final Exception ex ) {
      APP_VERSION = "0.0.0";

      // Application version cannot be found, use a default value.
      final var fmt = "Unable to determine application version.";
      clue( ex, fmt );
    }

    // The plug-in that requests the version from the repository tag will
    // add a "dirty" number and indicator suffix. Removing it allows the
    // "clean" version to be used to pull a corresponding typesetter container.
    APP_VERSION_CLEAN = APP_VERSION.replaceAll( "-.*", "" );
    APP_YEAR = getYear();

    // This also sets the user agent for the SVG rendering library.
    System.setProperty( "http.agent", APP_TITLE + " " + APP_VERSION_CLEAN );

    USER_DATA_DIR = UserDataDir.getAppPath( APP_TITLE_LOWERCASE );
    USER_CACHE_DIR = USER_DATA_DIR.resolve( "cache" ).toFile();

    if( !USER_CACHE_DIR.exists() ) {
      final var ignored = USER_CACHE_DIR.mkdirs();
    }
  }

  @SuppressWarnings( "SameParameterValue" )
  private static InputStream openResource( final String path ) {
    return Constants.class.getResourceAsStream( path );
  }

  private static String getYear() {
    return Integer.toString( Calendar.getInstance().get( Calendar.YEAR ) );
  }
}
