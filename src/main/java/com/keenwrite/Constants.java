/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite;

import com.keenwrite.io.File;
import com.keenwrite.io.MediaType;
import com.keenwrite.service.Settings;
import com.keenwrite.sigils.RSigilOperator;
import com.keenwrite.sigils.SigilOperator;
import com.keenwrite.sigils.YamlSigilOperator;
import javafx.scene.image.Image;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.function.UnaryOperator;

import static com.keenwrite.Bootstrap.APP_TITLE_LOWERCASE;
import static com.keenwrite.io.MediaType.APP_R_MARKDOWN;
import static com.keenwrite.io.MediaType.APP_R_XML;
import static java.io.File.separator;
import static java.lang.String.format;
import static java.lang.System.getProperty;

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
  public static final Settings sSettings = Services.load( Settings.class );

  public static final File DEFAULT_DEFINITION = getFile( "definition" );
  public static final File DEFAULT_DOCUMENT = getFile( "document" );

  public static final String APP_BUNDLE_NAME = get( "application.messages" );

  // Prevent double events when updating files on Linux (save and timestamp).
  public static final int APP_WATCHDOG_TIMEOUT = get(
      "application.watchdog.timeout", 200 );

  public static final String STYLESHEET_MARKDOWN = get(
      "file.stylesheet.markdown" );
  public static final String STYLESHEET_MARKDOWN_LOCALE =
      "file.stylesheet.markdown.locale";
  public static final String STYLESHEET_PREVIEW = get(
      "file.stylesheet.preview" );
  public static final String STYLESHEET_PREVIEW_LOCALE =
      "file.stylesheet.preview.locale";
  public static final String STYLESHEET_SCENE = get( "file.stylesheet.scene" );

  public static final String FILE_LOGO_16 = get( "file.logo.16" );
  public static final String FILE_LOGO_32 = get( "file.logo.32" );
  public static final String FILE_LOGO_128 = get( "file.logo.128" );
  public static final String FILE_LOGO_256 = get( "file.logo.256" );
  public static final String FILE_LOGO_512 = get( "file.logo.512" );

  public static final Image ICON_DIALOG = new Image( FILE_LOGO_32 );

  public static final String FILE_PREFERENCES = getPreferencesFilename();

  public static final String PREFS_ROOT = get( "preferences.root" );
  public static final String PREFS_STATE = get( "preferences.root.state" );

  /**
   * Refer to filename extension settings in the configuration file. Do not
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
  public static final File USER_DIRECTORY =
      new File( System.getProperty( "user.dir" ) );

  /**
   * Default path to use for an untitled (pathless) file.
   */
  public static final Path DEFAULT_DIRECTORY = USER_DIRECTORY.toPath();

  /**
   * Associates file types with {@link SigilOperator} instances.
   */
  private static final Map<MediaType, SigilOperator> SIGIL_MAP = Map.of(
      APP_R_MARKDOWN, new RSigilOperator(),
      APP_R_XML, new RSigilOperator()
  );

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
   * Default text editor font size, in points.
   */
  public static final float FONT_SIZE_EDITOR = 12f;

  /**
   * Default locale for font loading.
   */
  public static final Locale DEFAULT_LOCALE = Locale.getDefault();

  /**
   * Default identifier to use for synchronized scrolling.
   */
  public static String CARET_ID = "caret";

  /**
   * Prevent instantiation.
   */
  private Constants() {
  }

  public static UnaryOperator<String> getSigilOperator(
      final MediaType mediaType ) {
    return SIGIL_MAP.getOrDefault( mediaType, new YamlSigilOperator() );
  }

  private static String get( final String key ) {
    return sSettings.getSetting( key, "" );
  }

  @SuppressWarnings("SameParameterValue")
  private static int get( final String key, final int defaultValue ) {
    return sSettings.getSetting( key, defaultValue );
  }

  /**
   * Returns a default {@link File} instance based on the given key suffix.
   *
   * @param suffix Appended to {@code "file.default."}.
   * @return A new {@link File} instance that references the settings file name.
   */
  private static File getFile( final String suffix ) {
    return new File( get( "file.default." + suffix ) );
  }

  /**
   * Returns the equivalent of {@code $HOME/.filename.xml}.
   */
  private static String getPreferencesFilename() {
    return format(
        "%s%s.%s.xml",
        getProperty( "user.home" ),
        separator,
        APP_TITLE_LOWERCASE
    );
  }
}
