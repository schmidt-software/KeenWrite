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
package org.markdownwriterfx.options;

import java.util.prefs.Preferences;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import static org.markdownwriterfx.util.Utils.putPrefs;
import static org.markdownwriterfx.util.Utils.putPrefsBoolean;
import static org.markdownwriterfx.util.Utils.putPrefsInt;
import org.pegdown.Extensions;

/**
 * Options
 *
 * @author Karl Tauber
 */
public class Options {
  private static final StringProperty LINE_SEPARATOR = new SimpleStringProperty();
  private static final StringProperty ENCODING = new SimpleStringProperty();
  private static final IntegerProperty MARKDOWN_EXTENSIONS = new SimpleIntegerProperty();
  private static final BooleanProperty SHOW_WHITESPACE = new SimpleBooleanProperty();

  private static Preferences options;

  public static void load( Preferences options ) {
    Options.options = options;

    setLineSeparator( options.get( "lineSeparator", null ) );
    setEncoding( options.get( "encoding", null ) );
    setMarkdownExtensions( options.getInt( "markdownExtensions", Extensions.ALL ) );
    setShowWhitespace( options.getBoolean( "showWhitespace", false ) );
  }

  public static void save() {
    putPrefs( options, "lineSeparator", getLineSeparator(), null );
    putPrefs( options, "encoding", getEncoding(), null );
    putPrefsInt( options, "markdownExtensions", getMarkdownExtensions(), Extensions.ALL );
    putPrefsBoolean( options, "showWhitespace", isShowWhitespace(), false );
  }

  public static String getLineSeparator() {
    return LINE_SEPARATOR.get();
  }

  public static void setLineSeparator( String lineSeparator ) {
    LINE_SEPARATOR.set( lineSeparator );
  }

  public static StringProperty lineSeparatorProperty() {
    return LINE_SEPARATOR;
  }

  public static String getEncoding() {
    return ENCODING.get();
  }

  public static void setEncoding( String encoding ) {
    ENCODING.set( encoding );
  }

  public static StringProperty encodingProperty() {
    return ENCODING;
  }

  public static int getMarkdownExtensions() {
    return MARKDOWN_EXTENSIONS.get();
  }

  public static void setMarkdownExtensions( int markdownExtensions ) {
    MARKDOWN_EXTENSIONS.set( markdownExtensions );
  }

  public static IntegerProperty markdownExtensionsProperty() {
    return MARKDOWN_EXTENSIONS;
  }

  public static boolean isShowWhitespace() {
    return SHOW_WHITESPACE.get();
  }

  public static void setShowWhitespace( boolean showWhitespace ) {
    SHOW_WHITESPACE.set( showWhitespace );
  }

  public static BooleanProperty showWhitespaceProperty() {
    return SHOW_WHITESPACE;
  }
}
