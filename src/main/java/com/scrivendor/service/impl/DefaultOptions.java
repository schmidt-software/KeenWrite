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
package com.scrivendor.service.impl;

import java.util.prefs.Preferences;
import static java.util.prefs.Preferences.userRoot;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import com.scrivendor.service.Options;
import static com.scrivendor.util.Utils.putPrefs;
import static com.scrivendor.util.Utils.putPrefsBoolean;
import static com.scrivendor.util.Utils.putPrefsInt;
import org.pegdown.Extensions;

/**
 * Options
 *
 * @author Karl Tauber
 */
public class DefaultOptions implements Options {
  private final StringProperty LINE_SEPARATOR = new SimpleStringProperty();
  private final StringProperty ENCODING = new SimpleStringProperty();
  private final IntegerProperty MARKDOWN_EXTENSIONS = new SimpleIntegerProperty();
  private final BooleanProperty SHOW_WHITESPACE = new SimpleBooleanProperty();

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
    setMarkdownExtensions( options.getInt( "markdownExtensions", Extensions.ALL ) );
    setShowWhitespace( options.getBoolean( "showWhitespace", false ) );
  }

  @Override
  public void save() {
    putPrefs( preferences, "lineSeparator", getLineSeparator(), null );
    putPrefs( preferences, "encoding", getEncoding(), null );
    putPrefsInt( preferences, "markdownExtensions", getMarkdownExtensions(), Extensions.ALL );
    putPrefsBoolean( preferences, "showWhitespace", isShowWhitespace(), false );
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

  @Override
  public int getMarkdownExtensions() {
    return MARKDOWN_EXTENSIONS.get();
  }

  @Override
  public void setMarkdownExtensions( int markdownExtensions ) {
    MARKDOWN_EXTENSIONS.set( markdownExtensions );
  }

  @Override
  public IntegerProperty markdownExtensionsProperty() {
    return MARKDOWN_EXTENSIONS;
  }

  @Override
  public boolean isShowWhitespace() {
    return SHOW_WHITESPACE.get();
  }

  @Override
  public void setShowWhitespace( boolean showWhitespace ) {
    SHOW_WHITESPACE.set( showWhitespace );
  }

  @Override
  public BooleanProperty showWhitespaceProperty() {
    return SHOW_WHITESPACE;
  }
}
