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
   * The {@link Locale}s are used for multiple purposes, including:
   *
   * <ul>
   *   <li>supported text editor font listing in preferences dialog;</li>
   *   <li>text editor CSS;</li>
   *   <li>preview window CSS; and</li>
   *   <li>lexicon to load for spellcheck.</li>
   * </ul>
   *
   * When the Markdown and preview CSS files are loaded, a general file is
   * first loaded, then a specific file is loaded according to the locale.
   * The specific file overrides font families so that different languages
   * may be presented.
   * <p>
   * Using an instance of {@link LinkedHashMap} preserves display order.
   * </p>
   * <p>
   * See
   * <a href="https://www.oracle.com/java/technologies/javase/jdk19-suported-locales.html">
   * JDK 19 Supported Locales
   * </a> for details.
   * </p>
   */
  private static final Map<String, Locale> sLocales = new LinkedHashMap<>();

  static {
    @SuppressWarnings( "SpellCheckingInspection" )
    final String[] tags = {
      // English
      "en-Latn-AU",
      "en-Latn-CA",
      "en-Latn-GB",
      "en-Latn-NZ",
      "en-Latn-US",
      "en-Latn-ZA",
      // German
      "de-Latn-AT",
      "de-Latn-DE",
      "de-Latn-LU",
      "de-Latn-CH",
      // Spanish
      "es-Latn-AR",
      "es-Latn-BO",
      "es-Latn-CL",
      "es-Latn-CO",
      "es-Latn-CR",
      "es-Latn-DO",
      "es-Latn-EC",
      "es-Latn-SV",
      "es-Latn-GT",
      "es-Latn-HN",
      "es-Latn-MX",
      "es-Latn-NI",
      "es-Latn-PA",
      "es-Latn-PY",
      "es-Latn-PE",
      "es-Latn-PR",
      "es-Latn-ES",
      "es-Latn-US",
      "es-Latn-UY",
      "es-Latn-VE",
      // French
      "fr-Latn-BE",
      "fr-Latn-CA",
      "fr-Latn-FR",
      "fr-Latn-LU",
      "fr-Latn-CH",
      // Hebrew
      //"iw-Hebr-IL",
      // Italian
      "it-Latn-IT",
      "it-Latn-CH",
      // Japanese
      "ja-Jpan-JP",
      // Korean
      "ko-Kore-KR",
      // Chinese
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
    // If the language is undefined then use the default locale.
    return locale == null || "und".equalsIgnoreCase( locale.toLanguageTag() )
      ? LOCALE_DEFAULT
      : locale;
  }

  public static ObservableList<String> localeListProperty() {
    return listProperty( sLocales.keySet() );
  }

  /**
   * Performs an O(n) search through the given map to find the key that is
   * mapped to the given value. A bidirectional map would be faster, but
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
