/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite;

import com.keenwrite.preferences.Key;
import com.keenwrite.sigils.SigilOperator;
import com.keenwrite.util.InterpolatingMap;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import static com.keenwrite.constants.Constants.APP_BUNDLE_NAME;
import static java.util.ResourceBundle.getBundle;

/**
 * Recursively resolves message properties. Property values can refer to other
 * properties using a <code>${var}</code> syntax.
 */
public final class Messages {

  private static final SigilOperator OPERATOR = new SigilOperator( "${", "}" );
  private static final InterpolatingMap MAP = new InterpolatingMap( OPERATOR );

  static {
    // Obtains the application resource bundle using the default locale. The
    // locale cannot be changed using the application, making interpolation of
    // values viable as a one-time operation.
    final var BUNDLE = getBundle( APP_BUNDLE_NAME );

    BUNDLE.keySet().forEach( key -> MAP.put( key, BUNDLE.getString( key ) ) );
    MAP.interpolate();
  }

  /**
   * Returns the value for a key from the message bundle. If the value cannot
   * be found, this returns the key.
   *
   * @param key Retrieve the value for this key.
   * @return The value for the key, or the key itself if not found.
   */
  public static String get( final String key ) {
    final var v = MAP.get( key );

    return v == null ? key : v;
  }

  /**
   * Returns the value for a key from the message bundle.
   *
   * @param key Retrieve the value for this key.
   * @return The value for the key.
   */
  public static String get( final Key key ) {
    return get( key.toString() );
  }

  /**
   * Returns the value for a key from the message bundle with the arguments
   * replacing <code>{#}</code> placeholders.
   *
   * @param key  Retrieve the value for this key.
   * @param args The values to substitute for placeholders.
   * @return The value for the key.
   */
  public static String get( final String key, final Object... args ) {
    return MessageFormat.format( get( key ), args );
  }

  /**
   * Answers whether the given key is contained in the application's messages
   * properties file.
   *
   * @param key The key to look for in the {@link ResourceBundle}.
   * @return {@code true} when the key exists as an exact match.
   */
  public static boolean containsKey( final String key ) {
    return MAP.containsKey( key );
  }

  private Messages() {}
}
