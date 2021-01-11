/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.preferences;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static java.util.Arrays.asList;

/**
 * Responsible for adding an ISO 15924 alpha-4 script code to {@link Locale}
 * instances. This allows all {@link Locale} objects to produce language tags
 * using the same format.
 */
public final class LocaleScripts {
  /**
   * ISO 15924 alpha-4 script code to represent Latin scripts.
   */
  private static final String SCRIPT_LATIN = "Latn";

  /**
   * This value is returned when a script hasn't been mapped for an instance of
   * {@link Locale}.
   */
  private static final Map<String, String> SCRIPT_DEFAULT = m( SCRIPT_LATIN );

  private static final Map<String, Map<String, String>> SCRIPTS =
    new HashMap<>();

  static {
    put( "en", m( "Latn" ) );
    put( "jp", m( "Jpan" ) );
    put( "ko", m( "Kore" ) );
    put( "zh", m( "Hant" ), m( "Hans", "CN", "MN", "MY", "SG" ) );
  }

  /**
   * Adds a script to a given {@link Locale} object. If the given {@link Locale}
   * already has a script, then it is returned unchanged.
   *
   * @param locale The {@link Locale} to update with its associated script.
   * @return The given {@link Locale} with a script included.
   */
  public static Locale withScript( Locale locale ) {
    assert locale != null;

    final var script = locale.getScript();

    if( script == null || script.isBlank() ) {
      final var builder = new Locale.Builder();
      builder.setLocale( locale );
      builder.setScript( getScript( locale ) );
      locale = builder.build();
    }

    return locale;
  }

  @SafeVarargs
  private static void put(
    final String language, final Map<String, String>... scripts ) {
    final var merged = new HashMap<String, String>();
    asList( scripts ).forEach( merged::putAll );
    SCRIPTS.put( language, merged );
  }

  /**
   * Returns the ISO 15924 alpha-4 script code for the given {@link Locale}.
   *
   * @param locale Language and country are used to find the script code.
   * @return The ISO code for the given locale, or {@link #SCRIPT_LATIN} if
   * no code has been mapped yet.
   */
  private static String getScript( final Locale locale ) {
    return SCRIPTS.getOrDefault( locale.getLanguage(), SCRIPT_DEFAULT )
                  .getOrDefault( locale.getCountry(), SCRIPT_LATIN );
  }

  /**
   * Helper method to instantiate a new {@link Map} having all keys referencing
   * the same value.
   *
   * @param v The value to associate with each key.
   * @param k The keys to associate with the given value.
   * @return A new {@link Map} with all keys referencing the same value.
   */
  private static Map<String, String> m( final String v, final String... k ) {
    final var map = new HashMap<String, String>();
    asList( k ).forEach( ( key ) -> map.put( key, v ) );
    return Collections.unmodifiableMap( map );
  }

  /**
   * Helper method to instantiate a new {@link Map} having an empty key
   * referencing the given value. This provides a default value so that
   * an unmapped country code can return a valid script code.
   *
   * @param v The value to associate with an empty key.
   * @return A new {@link Map} with the empty key referencing the given value.
   */
  private static Map<String, String> m( final String v ) {
    return m( v, "" );
  }
}
