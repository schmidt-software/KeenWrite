/*
 * Copyright 2016 White Magic Software, Ltd.
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
package com.scrivenvar;

import com.scrivenvar.service.Settings;

/**
 * @author White Magic Software, Ltd.
 */
public class Constants {

  private static final Settings SETTINGS = Services.load( Settings.class );

  /**
   * Prevent instantiation.
   */
  private Constants() {
  }

  private static String get( final String key ) {
    return SETTINGS.getSetting( key, "" );
  }

  // Bootstrapping...
  public static final String SETTINGS_NAME = "/com/scrivenvar/settings.properties";

  public static final String APP_BUNDLE_NAME = get( "application.messages" );

  public static final String STYLESHEET_SCENE = get( "file.stylesheet.scene" );
  public static final String STYLESHEET_MARKDOWN = get( "file.stylesheet.markdown" );
  public static final String STYLESHEET_PREVIEW = get( "file.stylesheet.preview" );

  public static final String FILE_LOGO_16 = get( "file.logo.16" );
  public static final String FILE_LOGO_32 = get( "file.logo.32" );
  public static final String FILE_LOGO_128 = get( "file.logo.128" );
  public static final String FILE_LOGO_256 = get( "file.logo.256" );
  public static final String FILE_LOGO_512 = get( "file.logo.512" );

  public static final String CARET_POSITION_BASE = get( "caret.token.base" );
  public static final String CARET_POSITION_MD = get( "caret.token.markdown" );
  public static final String CARET_POSITION_XML = get( "caret.token.xml" );
  public static final String CARET_POSITION_HTML = get( "caret.token.html" );

  public static final String PREFS_ROOT = get( "preferences.root" );
  public static final String PREFS_ROOT_STATE = get( "preferences.root.state" );
  public static final String PREFS_ROOT_OPTIONS = get( "preferences.root.options" );
}
