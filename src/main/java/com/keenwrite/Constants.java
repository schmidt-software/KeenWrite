/* Copyright 2020 White Magic Software, Ltd.
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
package com.keenwrite;

import com.keenwrite.service.Settings;
import javafx.scene.image.Image;

import java.nio.file.Path;

import static com.keenwrite.Bootstrap.APP_TITLE_LOWERCASE;
import static java.lang.String.format;

/**
 * Defines application-wide default values.
 */
public class Constants {

  /**
   * Used by the default settings to load the {@link Settings} service. This
   * must come before any attempt is made to create a {@link Settings} object.
   * The reference to {@link Bootstrap#APP_TITLE_LOWERCASE} should cause the
   * JVM to load {@link Bootstrap} prior to proceeding. Loading that class
   * beforehand will read the bootstrap properties file to determine the
   * application name, which is then used to locate the settings properties.
   */
  public static final String PATH_PROPERTIES_SETTINGS =
      format( "/com/%s/settings.properties", APP_TITLE_LOWERCASE );

  /**
   * The {@link Settings} uses {@link #PATH_PROPERTIES_SETTINGS}.
   */
  public static final Settings SETTINGS = Services.load( Settings.class );

  public static final String DEFINITION_NAME = get( "file.definition.default" );
  public static final String DOCUMENT_NAME = get( "file.document.default" );

  public static final String APP_BUNDLE_NAME = get( "application.messages" );

  // Prevent double events when updating files on Linux (save and timestamp).
  public static final int APP_WATCHDOG_TIMEOUT = get(
      "application.watchdog.timeout", 200 );

  public static final String STYLESHEET_MARKDOWN = get(
      "file.stylesheet.markdown" );
  public static final String STYLESHEET_PREVIEW = get(
      "file.stylesheet.preview" );
  public static final String STYLESHEET_SCENE = get( "file.stylesheet.scene" );

  public static final String FILE_LOGO_16 = get( "file.logo.16" );
  public static final String FILE_LOGO_32 = get( "file.logo.32" );
  public static final String FILE_LOGO_128 = get( "file.logo.128" );
  public static final String FILE_LOGO_256 = get( "file.logo.256" );
  public static final String FILE_LOGO_512 = get( "file.logo.512" );

  public static final Image ICON_DIALOG = new Image( FILE_LOGO_32 );

  public static final String PREFS_ROOT = get( "preferences.root" );
  public static final String PREFS_STATE = get( "preferences.root.state" );

  /**
   * Refer to filename extension settings in the configuration file. Do not
   * terminate these prefixes with a period.
   */
  public static final String GLOB_PREFIX_FILE = "file.ext";
  public static final String GLOB_PREFIX_DEFINITION =
      "definition." + GLOB_PREFIX_FILE;

  /**
   * Three parameters: line number, column number, and offset.
   */
  public static final String STATUS_BAR_LINE = "Main.status.line";

  public static final String STATUS_BAR_OK = "Main.status.state.default";

  /**
   * Used to show an error while parsing, usually syntactical.
   */
  public static final String STATUS_PARSE_ERROR = "Main.status.error.parse";
  public static final String STATUS_DEFINITION_BLANK =
      "Main.status.error.def.blank";
  public static final String STATUS_DEFINITION_EMPTY =
      "Main.status.error.def.empty";

  /**
   * One parameter: the word under the cursor that could not be found.
   */
  public static final String STATUS_DEFINITION_MISSING =
      "Main.status.error.def.missing";

  /**
   * Used when creating flat maps relating to resolved variables.
   */
  public static final int DEFAULT_MAP_SIZE = 64;

  /**
   * Default image extension order to use when scanning.
   */
  public static final String PERSIST_IMAGES_DEFAULT =
      get( "file.ext.image.order" );

  /**
   * Default working directory to use for R startup script.
   */
  public static final String USER_DIRECTORY = System.getProperty( "user.dir" );

  /**
   * Default path to use for an untitled (pathless) file.
   */
  public static final Path DEFAULT_DIRECTORY = Path.of( USER_DIRECTORY );

  /**
   * Default starting delimiter for definition variables. This value must
   * not overlap math delimiters, so do not use $ tokens as the first
   * delimiter.
   */
  public static final String DEF_DELIM_BEGAN_DEFAULT = "{{";

  /**
   * Default ending delimiter for definition variables.
   */
  public static final String DEF_DELIM_ENDED_DEFAULT = "}}";

  /**
   * Default starting delimiter when inserting R variables.
   */
  public static final String R_DELIM_BEGAN_DEFAULT = "x( ";

  /**
   * Default ending delimiter when inserting R variables.
   */
  public static final String R_DELIM_ENDED_DEFAULT = " )";

  /**
   * Resource directory where different language lexicons are located.
   */
  public static final String LEXICONS_DIRECTORY = "lexicons";

  /**
   * Absolute location of true type font files within the Java archive file.
   */
  public static final String FONT_DIRECTORY = "/fonts";

  /**
   * Default text editor font size, in points.
   */
  public static final float FONT_SIZE_EDITOR = 12f;

  /**
   * Default identifier to use for synchronized scrolling.
   */
  public static String CARET_ID = "caret";

  /**
   * Prevent instantiation.
   */
  private Constants() {
  }

  private static String get( final String key ) {
    return SETTINGS.getSetting( key, "" );
  }

  @SuppressWarnings("SameParameterValue")
  private static int get( final String key, final int defaultValue ) {
    return SETTINGS.getSetting( key, defaultValue );
  }
}
