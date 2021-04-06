/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite;

import com.keenwrite.constants.Constants;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Properties;

import static org.apache.batik.util.ParsedURL.setGlobalUserAgent;

/**
 * Responsible for loading the bootstrap.properties file, which is
 * tactically located outside of the standard resource reverse domain name
 * namespace to avoid hard-coding the application name in many places.
 * Instead, the application name is located in the bootstrap file, which is
 * then used to look-up the remaining settings.
 * <p>
 * See {@link Constants#PATH_PROPERTIES_SETTINGS} for details.
 * </p>
 */
public final class Bootstrap {
  /**
   * Order matters, this must be populated before deriving the app title.
   */
  private static final Properties P = new Properties();

  static {
    try( final var in = openResource( "/bootstrap.properties" ) ) {
      P.load( in );
    } catch( final Exception ignored ) {
      // Bootstrap properties cannot be found, throw in the towel.
    }
  }

  public static final String APP_TITLE = P.getProperty( "application.title" );
  public static final String APP_TITLE_LOWERCASE = APP_TITLE.toLowerCase();
  public static final String APP_VERSION = Launcher.getVersion();
  public static final String APP_YEAR = getYear();

  static {
    System.setProperty( "http.agent", APP_TITLE + " " + APP_VERSION );
    setGlobalUserAgent( System.getProperty( "http.agent" ) );
  }

  @SuppressWarnings( "SameParameterValue" )
  private static InputStream openResource( final String path ) {
    return Constants.class.getResourceAsStream( path );
  }

  private static String getYear() {
    return Integer.toString( Calendar.getInstance().get( Calendar.YEAR ) );
  }
}
