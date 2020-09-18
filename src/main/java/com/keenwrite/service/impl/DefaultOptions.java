/*
 * Copyright 2020 Karl Tauber and White Magic Software, Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.keenwrite.service.impl;

import com.keenwrite.service.Options;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static com.keenwrite.Constants.PREFS_ROOT;
import static com.keenwrite.Constants.PREFS_STATE;
import static java.util.prefs.Preferences.userRoot;

/**
 * Persistent options user can change at runtime.
 */
public class DefaultOptions implements Options {
  public DefaultOptions() {
  }

  /**
   * This will throw IllegalArgumentException if the value exceeds the maximum
   * preferences value length.
   *
   * @param key   The name of the key to associate with the value.
   * @param value The value to persist.
   * @throws BackingStoreException New value not persisted.
   */
  @Override
  public void put( final String key, final String value )
      throws BackingStoreException {
    getState().put( key, value );
    getState().flush();
  }

  @Override
  public String get( final String key, final String value ) {
    return getState().get( key, value );
  }

  @Override
  public String get( final String key ) {
    return get( key, "" );
  }

  private Preferences getRootPreferences() {
    return userRoot().node( PREFS_ROOT );
  }

  @Override
  public Preferences getState() {
    return getRootPreferences().node( PREFS_STATE );
  }
}
