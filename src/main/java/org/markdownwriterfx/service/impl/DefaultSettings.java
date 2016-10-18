/*
 * Copyright (c) 2016 White Magic Software, Inc.
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
package org.markdownwriterfx.service.impl;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.markdownwriterfx.service.Settings;

/**
 *
 * @author White Magic Software, Ltd.
 */
public class DefaultSettings implements Settings {

  private PropertiesConfiguration configuration;

  public DefaultSettings() throws ConfigurationException, URISyntaxException, IOException {
    setConfiguration( createPropertiesConfiguration() );
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
    return getPropertiesConfiguration().getString( property, defaultValue );
  }

  @Override
  public List<Object> getSettingList( String property, List<String> defaults ) {
    return getPropertiesConfiguration().getList( property, defaults );
  }
  
  // TODO: Coerce list
//  public List<String> 

  private PropertiesConfiguration createPropertiesConfiguration()
    throws ConfigurationException {
    final URL url = getConfigurationSource();

    return url == null
      ? new PropertiesConfiguration()
      : new PropertiesConfiguration( url );
  }

  private URL getConfigurationSource() {
    return getClass().getResource( getConfigurationName() );
  }

  private String getConfigurationName() {
    return "settings.properties";
  }

  private void setConfiguration( PropertiesConfiguration configuration ) {
    this.configuration = configuration;
  }

  private PropertiesConfiguration getPropertiesConfiguration() {
    return this.configuration;
  }
}
