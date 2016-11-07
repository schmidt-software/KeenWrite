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
package com.scrivendor.service.impl;

import static com.scrivendor.Constants.SETTINGS_NAME;
import com.scrivendor.service.Settings;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * Responsible for loading settings that help avoid hard-coded assumptions.
 *
 * @author White Magic Software, Ltd.
 */
public class DefaultSettings implements Settings {

  private PropertiesConfiguration properties;

  public DefaultSettings()
    throws ConfigurationException, URISyntaxException, IOException {
    setProperties( createProperties() );
  }

  /**
   * Returns the value of a string property.
   *
   * @param property The property key.
   * @param defaultValue The value to return if no property key has been set.
   *
   * @return The property key value, or defaultValue when no key found.
   */
  @Override
  public String getSetting( String property, String defaultValue ) {
    return getSettings().getString( property, defaultValue );
  }

  /**
   * Returns the value of a string property.
   *
   * @param property The property key.
   * @param defaultValue The value to return if no property key has been set.
   *
   * @return The property key value, or defaultValue when no key found.
   */
  @Override
  public int getSetting( String property, int defaultValue ) {
    return getSettings().getInt( property, defaultValue );
  }

  @Override
  public List<Object> getSettingList( String property, List<String> defaults ) {
    return getSettings().getList( property, defaults );
  }

  /**
   * Convert the generic list of property objects into strings.
   *
   * @param property The property value to coerce.
   * @param defaults The defaults values to use should the property be unset.
   *
   * @return The list of properties coerced from objects to strings.
   */
  @Override
  public List<String> getStringSettingList( 
    final String property, final List<String> defaults ) {
    final List<Object> settings = getSettingList( property, defaults );

    return settings.stream()
      .map( object -> Objects.toString( object, null ) )
      .collect( Collectors.toList() );
  }

  private PropertiesConfiguration createProperties()
    throws ConfigurationException {
    final URL url = getPropertySource();

    return url == null
      ? new PropertiesConfiguration()
      : new PropertiesConfiguration( url );
  }

  private URL getPropertySource() {
    return getClass().getResource( getSettingsFilename() );
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
