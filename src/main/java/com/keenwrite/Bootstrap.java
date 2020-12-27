/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite;

import java.util.Properties;

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
public class Bootstrap {
  private static final Properties BOOTSTRAP = new Properties();

  static {
    try( final var stream =
             Constants.class.getResourceAsStream( "/bootstrap.properties" ) ) {
      BOOTSTRAP.load( stream );
    } catch( final Exception ignored ) {
      // Bootstrap properties cannot be found, throw in the towel.
    }
  }

  public static final String APP_TITLE =
      BOOTSTRAP.getProperty( "application.title" );
  public static final String APP_TITLE_LOWERCASE = APP_TITLE.toLowerCase();
}
