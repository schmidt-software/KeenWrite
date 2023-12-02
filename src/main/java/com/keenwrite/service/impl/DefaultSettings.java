/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.service.impl;

import com.keenwrite.config.PropertiesConfiguration;
import com.keenwrite.service.Settings;

import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

import static com.keenwrite.constants.Constants.PATH_PROPERTIES_SETTINGS;

/**
 * Responsible for loading settings that help avoid hard-coded assumptions.
 */
public final class DefaultSettings implements Settings {

  private final PropertiesConfiguration mProperties = loadProperties();

  public DefaultSettings() {}

  /**
   * Returns the value of a string property.
   *
   * @param property     The property key.
   * @param defaultValue The value to return if no property key has been set.
   * @return The property key value, or defaultValue when no key found.
   */
  @Override
  public String getSetting( final String property, final String defaultValue ) {
    return getSettings().getString( property, defaultValue );
  }

  /**
   * Returns the value of a string property.
   *
   * @param property     The property key.
   * @param defaultValue The value to return if no property key has been set.
   * @return The property key value, or defaultValue when no key found.
   */
  @Override
  public int getSetting( final String property, final int defaultValue ) {
    return getSettings().getInt( property, defaultValue );
  }

  /**
   * Convert the generic list of property objects into strings.
   *
   * @param property The property value to coerce.
   * @param defaults The values to use should the property be unset.
   * @return The list of properties coerced from objects to strings.
   */
  @Override
  public List<String> getStringSettingList(
    final String property, final List<String> defaults ) {
    return getSettings().getList( property, defaults );
  }

  /**
   * Convert a list of property objects into strings, with no default value.
   *
   * @param property The property value to coerce.
   * @return The list of properties coerced from objects to strings.
   */
  @Override
  public List<String> getStringSettingList( final String property ) {
    return getStringSettingList( property, null );
  }

  /**
   * Returns a list of property names that begin with the given prefix.
   *
   * @param prefix The prefix to compare against each property name.
   * @return The list of property names that have the given prefix.
   */
  @Override
  public Iterator<String> getKeys( final String prefix ) {
    return getSettings().getKeys( prefix );
  }

  private PropertiesConfiguration loadProperties() {
    final var url = getPropertySource();
    final var configuration = new PropertiesConfiguration();
    final var encoding = getDefaultEncoding();

    if( url != null ) {
      try( final var reader = new InputStreamReader(
        url.openStream(), encoding ) ) {
        configuration.read( reader );
      } catch( final Exception ex ) {
        throw new RuntimeException( ex );
      }
    }

    return configuration;
  }

  private Charset getDefaultEncoding() {
    return Charset.defaultCharset();
  }

  private URL getPropertySource() {
    return DefaultSettings.class.getResource( PATH_PROPERTIES_SETTINGS );
  }

  private PropertiesConfiguration getSettings() {
    return mProperties;
  }
}
