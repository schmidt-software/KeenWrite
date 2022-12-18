/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite;

import com.keenwrite.collections.InterpolatingMap;
import com.keenwrite.preferences.Key;
import com.keenwrite.sigils.PropertyKeyOperator;
import com.keenwrite.sigils.SigilKeyOperator;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import static com.keenwrite.constants.Constants.APP_BUNDLE_NAME;
import static java.util.ResourceBundle.getBundle;

/**
 * Recursively resolves message properties. Property values can refer to other
 * properties using a <code>${var}</code> syntax.
 */
public final class Messages {

  private static final SigilKeyOperator OPERATOR = new PropertyKeyOperator();
  private static final InterpolatingMap MAP = new InterpolatingMap( OPERATOR );

  static {
    // Obtains the application resource bundle using the default locale. The
    // locale cannot be changed using the application, making interpolation of
    // values viable as a one-time operation.
    try {
      final var bundle = getBundle( APP_BUNDLE_NAME );

      bundle.keySet().forEach( key -> MAP.put( key, bundle.getString( key ) ) );
      MAP.interpolate();
    } catch( final Exception ignored ) {
      // This is bad, but it'll be extremely apparent when the UI loads. We
      // can't log this through regular channels because that'd lead to a
      // circular dependency.
    }
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

  public static int getInt( final String key, final int defaultValue ) {
    try {
      return Integer.parseInt( get( key ) );
    } catch( final NumberFormatException ignored ) {
      return defaultValue;
    }
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

  private Messages() { }
}
