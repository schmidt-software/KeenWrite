/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.preferences;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static com.keenwrite.constants.Constants.LOCALE_DEFAULT;
import static com.keenwrite.preferences.Workspace.listProperty;
import static java.util.Locale.forLanguageTag;

/**
 * Responsible for providing a list of locales from which the user may pick.
 */
public final class LocaleProperty extends SimpleObjectProperty<String> {

  /**
   * Lists the locales having fonts that are supported by the application.
   * When the Markdown and preview CSS files are loaded, a general file is
   * first loaded, then a specific file is loaded according to the locale.
   * The specific file overrides font families so that different languages
   * may be presented.
   * <p>
   * Using an instance of {@link LinkedHashMap} preserves display order.
   * </p>
   * <p>
   * See
   * <a href="https://oracle.com/java/technologies/javase/jdk12locales.html">
   * JDK 12 Locales
   * </a> for details.
   * </p>
   */
  private static final Map<String, Locale> sLocales = new LinkedHashMap<>();

  static {
    final String[] tags = {
      "en-Latn-AU",
      "en-Latn-CA",
      "en-Latn-GB",
      "en-Latn-NZ",
      "en-Latn-US",
      "en-Latn-ZA",
      "ja-Jpan-JP",
      "ko-Kore-KR",
      "zh-Hans-CN",
      "zh-Hans-SG",
      "zh-Hant-HK",
      "zh-Hant-TW",
    };

    for( final var tag : tags ) {
      final var locale = forLanguageTag( tag );
      sLocales.put( locale.getDisplayName(), locale );
    }
  }

  public LocaleProperty( final Locale locale ) {
    super( sanitize( locale ).getDisplayName() );
  }

  public static String parseLocale( final String languageTag ) {
    final var locale = forLanguageTag( languageTag );
    final var key = getKey( sLocales, locale );
    return key == null ? LOCALE_DEFAULT.getDisplayName() : key;
  }

  public static String toLanguageTag( final String displayName ) {
    return sLocales.getOrDefault( displayName, LOCALE_DEFAULT ).toLanguageTag();
  }

  public Locale toLocale() {
    return sLocales.getOrDefault( getValue(), LOCALE_DEFAULT );
  }

  private static Locale sanitize( final Locale locale ) {
    // If the language is "und"efined then use the default locale.
    return locale == null || "und".equalsIgnoreCase( locale.toLanguageTag() )
      ? LOCALE_DEFAULT
      : locale;
  }

  public static ObservableList<String> localeListProperty() {
    return listProperty( sLocales.keySet() );
  }

  /**
   * Performs an O(n) search through the given map to find the key that is
   * mapped to the given value. A bi-directional map would be faster, but
   * also introduces additional dependencies. This doesn't need to be fast
   * because it happens once, at start up, and there aren't a lot of values.
   *
   * @param map   The map containing a key to find based on a value.
   * @param value The value to find within the map.
   * @param <K>   The type of key associated with a value.
   * @param <V>   The type of value associated with a key.
   * @return The key that corresponds to the given value, or {@code null} if
   * the key is not found.
   */
  @SuppressWarnings( "SameParameterValue" )
  private static <K, V> K getKey( final Map<K, V> map, final V value ) {
    for( final var entry : map.entrySet() ) {
      if( Objects.equals( value, entry.getValue() ) ) {
        return entry.getKey();
      }
    }

    return null;
  }
}
