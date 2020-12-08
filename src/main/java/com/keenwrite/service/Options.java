/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.service;

import com.dlsc.preferencesfx.PreferencesFx;

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

}
