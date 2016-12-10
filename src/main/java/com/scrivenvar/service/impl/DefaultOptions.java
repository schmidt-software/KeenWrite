/*
 * Copyright (c) 2015 Karl Tauber <karl at jformdesigner dot com>
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

import com.scrivenvar.service.Options;
import static com.scrivenvar.util.Utils.putPrefs;
import java.util.prefs.Preferences;
import static java.util.prefs.Preferences.userRoot;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Persistent options user can change at runtime.
 *
 * @author Karl Tauber and White Magic Software, Ltd.
 */
public class DefaultOptions implements Options {
  private final StringProperty LINE_SEPARATOR = new SimpleStringProperty();
  private final StringProperty ENCODING = new SimpleStringProperty();

  private Preferences preferences;
  
  public DefaultOptions() {
    setPreferences( getRootPreferences().node( "options" ) );
  }
  
  private void setPreferences( Preferences preferences ) {
    this.preferences = preferences;
  }

  private Preferences getRootPreferences() {
    return userRoot().node( "application" );
  }

  @Override
  public Preferences getState() {
    return getRootPreferences().node( "state" );
  }

  public Preferences getPreferences() {
    return this.preferences;
  }

  @Override
  public void load( Preferences options ) {
    setLineSeparator( options.get( "lineSeparator", null ) );
    setEncoding( options.get( "encoding", null ) );
  }

  @Override
  public void save() {
    final Preferences prefs = getPreferences();
    
    putPrefs( prefs, "lineSeparator", getLineSeparator(), null );
    putPrefs( prefs, "encoding", getEncoding(), null );
  }

  @Override
  public String getLineSeparator() {
    return LINE_SEPARATOR.get();
  }

  @Override
  public void setLineSeparator( String lineSeparator ) {
    LINE_SEPARATOR.set( lineSeparator );
  }

  @Override
  public StringProperty lineSeparatorProperty() {
    return LINE_SEPARATOR;
  }

  @Override
  public String getEncoding() {
    return ENCODING.get();
  }

  @Override
  public void setEncoding( String encoding ) {
    ENCODING.set( encoding );
  }

  @Override
  public StringProperty encodingProperty() {
    return ENCODING;
  }
}
