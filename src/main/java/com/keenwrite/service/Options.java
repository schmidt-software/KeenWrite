/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.service;

import com.dlsc.preferencesfx.PreferencesFx;

import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Responsible for persisting options that are safe to load before the UI
 * is shown. This can include items like window dimensions, last file
 * opened, split pane locations, and more. This cannot be used to persist
 * options that are user-controlled (i.e., all options available through
 * {@link PreferencesFx}).
 */
public interface Options extends Service {

  /**
   * Returns the {@link Preferences} that persist settings that cannot
   * be configured via the user interface.
   *
   * @return A valid {@link Preferences} instance, never {@code null}.
   */
  Preferences getState();

  /**
   * Stores the key and value into the user preferences to be loaded the next
   * time the application is launched.
   *
   * @param key   Name of the key to persist along with its value.
   * @param value Value to associate with the key.
   * @throws BackingStoreException Could not persist the change.
   */
  void put( String key, String value ) throws BackingStoreException;

  /**
   * Retrieves the value for a key in the user preferences.
   *
   * @param key          Retrieve the value of this key.
   * @param defaultValue The value to return in the event that the given key has
   *                     no associated value.
   * @return The value associated with the key.
   */
  String get( String key, String defaultValue );

  /**
   * Retrieves the value for a key in the user preferences. This will return
   * the empty string if the value cannot be found.
   *
   * @param key The key to find in the preferences.
   * @return A non-null, possibly empty value for the key.
   */
  String get( String key );

  /**
   * Retrieves the values for a key in the user preferences. This will return
   * an empty {@link List} if the value cannot be found.
   *
   * @param key The key to find in the preferences.
   * @return A non-null, possibly empty {@link List} of values for the key.
   */
  List<String> getStrings( final String key );

  /**
   * Stores the key and values into the user preferences to be loaded the next
   * time the application is launched.
   *
   * @param key   Name of the key to persist along with its value.
   * @param value Values to associate with the key.
   */
  void putStrings( final String key, final List<String> value );
}
