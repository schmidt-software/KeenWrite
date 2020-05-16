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
package com.scrivenvar.service.impl;

import com.scrivenvar.service.Settings;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.convert.ListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

import static com.scrivenvar.Constants.SETTINGS_NAME;

/**
 * Responsible for loading settings that help avoid hard-coded assumptions.
 *
 * @author White Magic Software, Ltd.
 */
public class DefaultSettings implements Settings {

  private static final char VALUE_SEPARATOR = ',';

  private PropertiesConfiguration properties;

  public DefaultSettings()
      throws ConfigurationException, URISyntaxException, IOException {
    setProperties( createProperties() );
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
   * Changes key's value. This will clear the old value before setting the new
   * value so that the old value is erased, not changed into a list.
   *
   * @param key   The property key name to obtain its value.
   * @param value The new value to set.
   */
  @Override
  public void putSetting( final String key, final String value ) {
    getSettings().clearProperty( key );
    getSettings().addProperty( key, value );
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

  private PropertiesConfiguration createProperties()
      throws ConfigurationException {

    final URL url = getPropertySource();
    final PropertiesConfiguration configuration = new PropertiesConfiguration();

    if( url != null ) {
      try( final Reader r = new InputStreamReader( url.openStream(),
                                                   getDefaultEncoding() ) ) {
        configuration.setListDelimiterHandler( createListDelimiterHandler() );
        configuration.read( r );

      } catch( final IOException ex ) {
        throw new RuntimeException( new ConfigurationException( ex ) );
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
    return DefaultSettings.class.getResource( getSettingsFilename() );
  }

  private String getSettingsFilename() {
    return SETTINGS_NAME;
  }

  private void setProperties( final PropertiesConfiguration configuration ) {
    this.properties = configuration;
  }

  private PropertiesConfiguration getSettings() {
    return this.properties;
  }
}
