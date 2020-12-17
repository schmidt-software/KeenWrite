/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.service.impl;

import com.keenwrite.service.Settings;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.convert.ListDelimiterHandler;

import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

import static com.keenwrite.Constants.PATH_PROPERTIES_SETTINGS;

/**
 * Responsible for loading settings that help avoid hard-coded assumptions.
 */
public final class DefaultSettings implements Settings {

  private static final char VALUE_SEPARATOR = ',';

  private final PropertiesConfiguration mProperties = createProperties();

  public DefaultSettings() {
  }

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
   * @param defaults The defaults values to use should the property be unset.
   * @return The list of properties coerced from objects to strings.
   */
  @Override
  public List<String> getStringSettingList(
      final String property, final List<String> defaults ) {
    return getSettings().getList( String.class, property, defaults );
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

  private PropertiesConfiguration createProperties() {
    final var url = getPropertySource();
    final var configuration = new PropertiesConfiguration();

    if( url != null ) {
      try( final var reader = new InputStreamReader(
          url.openStream(), getDefaultEncoding() ) ) {
        configuration.setListDelimiterHandler( createListDelimiterHandler() );
        configuration.read( reader );
      } catch( final Exception ex ) {
        throw new RuntimeException( ex );
      }
    }

    return configuration;
  }

  protected Charset getDefaultEncoding() {
    return Charset.defaultCharset();
  }

  protected ListDelimiterHandler createListDelimiterHandler() {
    return new DefaultListDelimiterHandler( VALUE_SEPARATOR );
  }

  private URL getPropertySource() {
    return DefaultSettings.class.getResource( PATH_PROPERTIES_SETTINGS );
  }

  private PropertiesConfiguration getSettings() {
    return mProperties;
  }
}
