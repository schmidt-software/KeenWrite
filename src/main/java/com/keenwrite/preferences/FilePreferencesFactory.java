/* Copyright 2016 David Croft
 * Copyright 2020 White Magic Software, Ltd.
 *
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
package com.keenwrite.preferences;

import java.io.File;
import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

import static com.keenwrite.Bootstrap.APP_TITLE_LOWERCASE;
import static java.io.File.separator;
import static java.lang.System.getProperty;

/**
 * PreferencesFactory implementation that stores the preferences in a
 * user-defined file. Usage:
 * <pre>
 * System.setProperty( "java.util.prefs.PreferencesFactory",
 * FilePreferencesFactory.class.getName() );
 * </pre>
 */
public class FilePreferencesFactory implements PreferencesFactory {

  private static File sPreferencesFile;
  private Preferences rootPreferences;

  @Override
  public Preferences systemRoot() {
    return userRoot();
  }

  @Override
  public Preferences userRoot() {
    final var prefs = rootPreferences;

    if( prefs == null ) {
      rootPreferences = new FilePreferences( null, "" );
    }

    return rootPreferences;
  }

  public static File getPreferencesFile() {
    final var prefs = sPreferencesFile;

    if( prefs == null ) {
      sPreferencesFile = new File( getPreferencesFilename() ).getAbsoluteFile();
    }

    return sPreferencesFile;
  }

  public static String getPreferencesFilename() {
    return getProperty( "user.home" ) + separator + '.' + APP_TITLE_LOWERCASE;
  }
}
