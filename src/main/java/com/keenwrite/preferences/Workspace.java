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
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;

import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import static com.keenwrite.Bootstrap.APP_TITLE_LOWERCASE;
import static com.keenwrite.Constants.FILE_PREFERENCES;
import static com.keenwrite.StatusBarNotifier.clue;
import static javafx.collections.FXCollections.observableSet;

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
   * Constructs a new workspace with the given identifier. This will attempt
   * to read the configuration file stored in the
   */
  public Workspace( final WorkspacePreferences preferences ) {
    mPreferences = preferences;
    load( preferences );
  }

  /**
   * Attempts to load the {@link Constants#FILE_PREFERENCES} configuration file.
   * If not found, this will fall back to an empty configuration file, leaving
   * the application to fill in default values.
   */
  private void load( final WorkspacePreferences preferences ) {
    try {
      final var config = createConfiguration();

      preferences.consumeValueKeys( ( key ) -> {
        final var configValue = config.getProperty( key.toString() );
        final var propertyValue = preferences.valuesProperty( key );
        propertyValue.setValue( unmarshall( propertyValue, configValue ) );
      } );

      preferences.consumeSetKeys( ( key ) -> {
        final var configList =
          new HashSet<>( config.getList( key.toString() ) );
        final var propertySet = preferences.setsProperty( key );
        propertySet.setValue( observableSet( configList ) );
      } );
    } catch( final Exception ex ) {
      clue( ex );
    }
  }

  /**
   * Saves the current workspace.
   */
  public void save() {
    try {
      final var config = createConfiguration();

      // The root config key can only be set for an empty configuration file.
      config.setRootElementName( APP_TITLE_LOWERCASE );

      mPreferences.consumeValues( ( key, value ) -> config.setProperty(
        key.toString(), value.getValue() )
      );

      mPreferences.consumeSets(
        ( key, set ) -> {
          final String keyName = key.toString();
          set.forEach( ( value ) -> config.addProperty( keyName, value ) );
        }
      );
      new FileHandler( config ).save( FILE_PREFERENCES );
    } catch( final Exception ex ) {
      clue( ex );
    }
  }

  /**
   * Attempts to create a configuration that can read and write from the
   * {@link Constants#FILE_PREFERENCES} file.
   *
   * @return Configuration instance that can read and write project settings.
   */
  private XMLConfiguration createConfiguration() throws ConfigurationException {
    return new Configurations().xml( FILE_PREFERENCES );
  }

  private Object unmarshall(
    final Property<?> property, final Object configValue ) {
    return UNMARSHALL
      .getOrDefault( property.getClass(), ( value ) -> value )
      .apply( configValue.toString() );
  }
}
