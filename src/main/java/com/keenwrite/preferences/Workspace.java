/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.preferences;

import com.keenwrite.Constants;
import com.keenwrite.io.File;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleFloatProperty;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.io.FileHandler;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

import static com.keenwrite.Bootstrap.APP_TITLE_LOWERCASE;
import static com.keenwrite.Constants.FILE_PREFERENCES;
import static com.keenwrite.StatusBarNotifier.clue;
import static java.lang.String.format;
import static javafx.collections.FXCollections.observableArrayList;

/**
 * Responsible for defining behaviours for separate projects. A workspace has
 * the ability to save and restore a session, including the window dimensions,
 * tab setup, files, and user preferences.
 * <p>
 * The {@link Workspace} configuration must support hierarchical (nested)
 * configuration nodes to persist the user interface state. Although possible
 * with a flat configuration file, it's not nearly as simple or elegant.
 * </p>
 * <p>
 * Neither JSON nor HOCON support schema validation and versioning, which makes
 * XML the more suitable configuration file format. Schema validation and
 * versioning provide future-proofing and ease of reading and upgrading previous
 * versions of the configuration file.
 * </p>
 */
public final class Workspace {

  /**
   * Helps instantiate {@link Property} instances for XML configuration items.
   */
  private static final Map<Class<?>, Function<String, Object>> UNMARSHALL =
    Map.of(
      SimpleLocaleProperty.class, Locale::forLanguageTag,
      SimpleBooleanProperty.class, Boolean::parseBoolean,
      SimpleDoubleProperty.class, Double::parseDouble,
      SimpleFloatProperty.class, Float::parseFloat,
      SimpleFileProperty.class, File::new
    );

  /**
   * Defines observable user preferences properties and lists.
   */
  private final WorkspacePreferences mPreferences;

  /**
   * Application configuration file used to persist both user preferences and
   * project settings. The user preferences include items such as locale
   * and font sizes while the project settings include items such as last
   * opened directory and window sizes. That is, user preferences can be
   * changed directly by the user through the preferences dialog; whereas,
   * project settings reflect application interactions.
   */
  private final XMLConfiguration mConfig;

  /**
   * Constructs a new workspace with the given identifier. This will attempt
   * to read the configuration file stored in the
   */
  public Workspace( final WorkspacePreferences preferences ) {
    mPreferences = preferences;
    mConfig = load();
  }

  /**
   * Attempts to load the {@link Constants#FILE_PREFERENCES} configuration file.
   * If not found, this will fall back to an empty configuration file, leaving
   * the application to fill in default values.
   *
   * @return Configuration instance representing last known state of the
   * application's user preferences and project settings.
   */
  private XMLConfiguration load() {
    try {
      final var config = new Configurations().xml( FILE_PREFERENCES );

      mPreferences.consumeValueKeys( ( key ) -> {
        final var configValue = config.getProperty( key.toString() );
        final var propertyValue = mPreferences.valuesProperty( key );
        propertyValue.setValue( unmarshall( propertyValue, configValue ) );
      } );

      mPreferences.consumeListKeys( ( key ) -> {
        final var configList = config.getList( key.toString() );
        final var propertyList = mPreferences.listsProperty( key );
        propertyList.setValue( observableArrayList( configList ) );
      } );

      return config;
    } catch( final Exception ex ) {
      clue( ex );

      final var config = new XMLConfiguration();

      // The root config key can only be set for an empty configuration file.
      config.setRootElementName( APP_TITLE_LOWERCASE );
      return config;
    }
  }

  /**
   * Saves the current workspace.
   */
  public void save() {
    try {
      mPreferences.consumeValues( ( key, value ) -> mConfig.setProperty(
        key.toString(), value.getValue() )
      );

      mPreferences.consumeLists(
        ( key, set ) -> {
          final String keyName = key.toString();
          set.forEach( ( value ) -> mConfig.addProperty( keyName, value ) );
        }
      );
      new FileHandler( mConfig ).save( FILE_PREFERENCES );
    } catch( final Exception ex ) {
      clue( ex );
    }
  }

  private Object unmarshall(
    final Property<?> property, final Object configValue ) {
    return UNMARSHALL
      .getOrDefault( property.getClass(), ( value ) -> value )
      .apply( configValue.toString() );
  }

  /**
   * Delegates to {@link #set(Key, String)} after converting the given
   * {@link Path} to a string (using the absolute path).
   *
   * @param key  The document key to change.
   * @param path Path to a file or directory to store in the settings.
   */
  public void set( final Key key, final Path path ) {
    set( key, toString( path ) );
  }

  /**
   * Delegates to {@link XMLConfiguration#setProperty(String, Object)} to
   * change the value for the given key. If the key doesn't exist, it will be
   * created in the hierarchy; otherwise, the existing value is overwritten.
   *
   * @param key   The document key to change.
   * @param value The new value for the key.
   */
  public void set( final Key key, final String value ) {
    mConfig.setProperty( key.toString(), value );
  }

  /**
   * Returns the value for the key from the application settings.
   *
   * @param key          The key to look up in the settings.
   * @param defaultValue The default value to return if the key is not set.
   * @return The value for the given key, or the given default if none found.
   */
  public String get( final Key key, final String defaultValue ) {
    final var prop = mConfig.getProperty( key.toString() );
    return prop == null ? defaultValue : prop.toString();
  }

  /**
   * Delegates to {@link XMLConfiguration#addProperty(String, Object)} to
   * add the given {@link File} to the list of files specified by the
   * given key.
   *
   * @param key  The document hierarchy key name.
   * @param file Absolute path of filename stored at the given key.
   */
  public void addListItem( final Key key, final File file ) {
    mConfig.addProperty( key.toString(), toString( file ) );
  }

  /**
   * Returns the list of files opened for this {@link Workspace}.
   *
   * @param key The document hierarchy key name.
   * @return A non-null, possibly empty list of {@link File} instances.
   */
  public List<File> getListFiles( final Key key ) {
    final var items = getListItems( key );
    final var files = new HashSet<File>( items.size() );
    items.forEach( ( item ) -> {
      final var file = new File( item );

      if( file.exists() ) {
        files.add( file );
      }
    } );

    // Removes duplicate and missing files. The configuration is re-populated
    // on saving because the UI will re-open the files in the list that's
    // returned by this method. Re-opening adds the files back to the config.
    // This ensures that the list never grows beyond a reasonable number.
    mConfig.clearProperty( key.toString() );

    return new ArrayList<>( files );
  }

  /**
   * Returns a list of items for a given key name.
   *
   * @param key The document hierarchy key name.
   * @return The list of strings in the document hierarchy corresponding to the
   * given key.
   */
  private List<String> getListItems( final Key key ) {
    return mConfig.getList( String.class, key.toString(), new ArrayList<>() );
  }

  /**
   * Removes the given file from the workspace so that when the application
   * is restarted, the file will not be automatically loaded. This calls
   * {@link XMLConfiguration#clearTree(String)} to remove the given file
   * from the list that corresponds to the key.
   *
   * @param key  The document hierarchy key name.
   * @param file The file to remove from the list files opened for editing.
   */
  public void purgeListItem( final Key key, final File file ) {
    final var items = getListItems( key );
    final var index = items.indexOf( toString( file ) );

    // The list index is 0-based.
    if( index >= 0 ) {
      mConfig.clearTree( format( "%s(%d)", key, index ) );
    }
  }

  public WorkspacePreferences getPreferences() {
    return mPreferences;
  }

  private String toString( final Path path ) {
    return toString( new File( path.toFile() ) );
  }

  private String toString( final File file ) {
    return file.getAbsolutePath();
  }
}
