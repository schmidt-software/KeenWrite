/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.constants;

import com.keenwrite.Bootstrap;
import com.keenwrite.Services;
import com.keenwrite.service.Settings;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Locale;

import static com.keenwrite.Bootstrap.APP_TITLE_LOWERCASE;
import static com.keenwrite.Bootstrap.USER_DATA_DIR;
import static com.keenwrite.io.SysFile.toFile;
import static com.keenwrite.io.UserDataDir.getUserHome;
import static com.keenwrite.preferences.LocaleScripts.withScript;
import static com.keenwrite.util.SystemUtils.*;
import static java.io.File.separator;
import static java.lang.String.format;
import static java.lang.System.getProperty;

/**
 * Defines application-wide default values.
 */
public final class Constants {

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
  public static final Settings sSettings = Services.load( Settings.class );

  public static final double WINDOW_X_DEFAULT = 0;
  public static final double WINDOW_Y_DEFAULT = 0;
  public static final double WINDOW_W_DEFAULT = 1200;
  public static final double WINDOW_H_DEFAULT = 800;

  public static final File DOCUMENT_DEFAULT = getFile( "document" );
  public static final int DOCUMENT_OFFSET = 0;
  public static final File DEFINITION_DEFAULT = getFile( "definition" );
  public static final File PDF_DEFAULT = getFile( "pdf" );

  public static final String APP_BUNDLE_NAME = get( "application.messages" );

  public static final String STYLESHEET_APPLICATION_BASE =
    get( "file.stylesheet.application.base" );
  public static final String STYLESHEET_APPLICATION_SKIN =
    get( "file.stylesheet.application.skin" );
  public static final String STYLESHEET_MARKDOWN =
    get( "file.stylesheet.markdown" );
  public static final String STYLESHEET_MARKDOWN_LOCALE =
    "file.stylesheet.markdown.locale";
  public static final String STYLESHEET_PREVIEW =
    get( "file.stylesheet.preview" );
  public static final String STYLESHEET_PREVIEW_LOCALE =
    "file.stylesheet.preview.locale";

  public static final File FILE_PREFERENCES = getPreferencesFile();

  /**
   * Refer to file name extension settings in the configuration file. Do not
   * terminate with a period.
   */
  public static final String GLOB_PREFIX_FILE = "file.ext";

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
   * Default image extension order to use when scanning.
   */
  public static final String PERSIST_IMAGES_DEFAULT =
    get( "file.ext.image.order" );

  /**
   * Default working directory.
   */
  public static final File USER_DIRECTORY =
    new File( System.getProperty( "user.dir" ) );

  /**
   * Location to write temporary files.
   */
  public static final String TEMPORARY_DIRECTORY =
    System.getProperty( "java.io.tmpdir" );

  public static final String NEWLINE = System.lineSeparator();

  /**
   * Default path to use for an untitled (pathless) file.
   */
  public static final Path DEFAULT_DIRECTORY = USER_DIRECTORY.toPath();

  /**
   * Default character set to use when reading/writing files.
   */
  public static final Charset DEFAULT_CHARSET = Charset.defaultCharset();

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
   * Default text editor font name.
   */
  public static final String FONT_NAME_EDITOR_DEFAULT = "Noto Sans Regular";

  /**
   * Default text editor font size, in points.
   */
  public static final float FONT_SIZE_EDITOR_DEFAULT = 12f;

  /**
   * Default preview font name.
   */
  public static final String FONT_NAME_PREVIEW_DEFAULT = "Source Serif 4";

  /**
   * Default preview font size, in points.
   */
  public static final float FONT_SIZE_PREVIEW_DEFAULT = 13f;

  /**
   * Scaling factor for rendering mathematics.
   */
  public static final double FONT_SIZE_MATH_DEFAULT = 2;

  /**
   * Default monospace preview font name.
   */
  public static final String FONT_NAME_PREVIEW_MONO_NAME_DEFAULT =
    "Source Code Pro";

  /**
   * Default monospace preview font size, in points.
   */
  public static final float FONT_SIZE_PREVIEW_MONO_SIZE_DEFAULT = 13f;

  /**
   * Default locale for font loading, including ISO 15924 alpha-4 script code.
   */
  public static final Locale LOCALE_DEFAULT = withScript( Locale.getDefault() );

  /**
   * Default CSS to apply (resolves to a minimal implementation).
   */
  public static final String SKIN_DEFAULT = "Modena Light";

  /**
   * Custom JavaFX CSS to apply to user interface.
   */
  public static final File SKIN_CUSTOM_DEFAULT = null;

  /**
   * Custom HTML CSS to apply to HTML preview panel.
   */
  public static final File PREVIEW_CUSTOM_DEFAULT = null;

  /**
   * Default identifier to use for synchronized scrolling.
   */
  public static final String CARET_ID = "caret";

  /**
   * Default spacing for UI items (e.g., toolbars).
   */
  public static final int UI_CONTROL_SPACING = 10;

  /**
   * Default server name for rendering diagrams.
   */
  public static final String DIAGRAM_SERVER_NAME = "kroki.io";

  /**
   * Application action messages properties prefix.
   */
  public static final String ACTION_PREFIX = "Action.";

  /**
   * Restrict theme names when displaying.
   */
  public static final byte THEME_NAME_LENGTH = 30;

  /**
   * Prevent instantiation.
   */
  private Constants() {
  }

  /**
   * Converts from points to pixels because FlyingSaucer cannot handle points
   * properly. This is used to convert font sizes.
   *
   * @param points The points to convert to pixels.
   * @return The given number of points in equivalent pixels.
   */
  public static int toPixels( final double points ) {
    return (int) (points * (1 + 1 / 3f));
  }

  public static String get( final String key ) {
    return sSettings.getSetting( key, "" );
  }

  /**
   * Returns a default {@link File} instance based on the given key suffix.
   *
   * @param suffix Appended to {@code "file.default."}.
   * @return A new {@link File} instance that references the settings file name.
   */
  private static File getFile( final String suffix ) {
    return new File( get( STR."file.default.\{suffix}" ) );
  }

  /**
   * Returns the equivalent of {@code $HOME/.filename.xml}.
   */
  private static File getPreferencesFile() {
    return new File( format(
      "%s%s.%s.xml",
      getProperty( "user.home" ),
      separator,
      APP_TITLE_LOWERCASE
    ) );
  }

  /**
   * Tries to get a system-independent path to the user's fonts directory.
   */
  public static File getFontDirectory() {
    final var FONT_PATH = Path.of( "fonts" );
    final var USER_HOME = getUserHome();

    final String fontBase;
    final Path fontUser;

    if( IS_OS_WINDOWS ) {
      fontBase = System.getenv( "WINDIR" );
      fontUser = FONT_PATH;
    }
    else if( IS_OS_MAC ) {
      fontBase = USER_HOME;
      fontUser = Path.of( "Library", "Fonts" );
    }
    else if( IS_OS_UNIX ) {
      fontBase = USER_HOME;
      fontUser = Path.of( ".fonts" );
    }
    else {
      fontBase = USER_DATA_DIR.toString();
      fontUser = FONT_PATH;
    }

    final var base = fontBase == null
      ? USER_DATA_DIR.relativize( fontUser )
      : Path.of( fontBase ).resolve( fontUser );

    return toFile( base );
  }
}
