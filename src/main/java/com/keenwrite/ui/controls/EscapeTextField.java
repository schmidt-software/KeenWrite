/*
 * Copyright 2020 Karl Tauber and White Magic Software, Ltd.
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

package com.keenwrite.ui.controls;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

/**
 * Responsible for escaping/unescaping characters for markdown.
 */
public class EscapeTextField extends TextField {

  public EscapeTextField() {
    escapedText.bindBidirectional(
        textProperty(),
        new StringConverter<>() {
          @Override
          public String toString( String object ) {
            return escape( object );
          }

          @Override
          public String fromString( String string ) {
            return unescape( string );
          }
        }
    );
    escapeCharacters.addListener(
        e -> escapedText.set( escape( textProperty().get() ) )
    );
  }

  // 'escapedText' property
  private final StringProperty escapedText = new SimpleStringProperty();

  public StringProperty escapedTextProperty() {
    return escapedText;
  }

  // 'escapeCharacters' property
  private final StringProperty escapeCharacters = new SimpleStringProperty();

  public String getEscapeCharacters() {
    return escapeCharacters.get();
  }

  public void setEscapeCharacters( String escapeCharacters ) {
    this.escapeCharacters.set( escapeCharacters );
  }

  private String escape( final String s ) {
    final String escapeChars = getEscapeCharacters();

    return isEmpty( escapeChars ) ? s :
        s.replaceAll( "([" + escapeChars.replaceAll(
            "(.)",
            "\\\\$1" ) + "])", "\\\\$1" );
  }

  private String unescape( final String s ) {
    final String escapeChars = getEscapeCharacters();

    return isEmpty( escapeChars ) ? s :
        s.replaceAll( "\\\\([" + escapeChars
            .replaceAll( "(.)", "\\\\$1" ) + "])", "$1" );
  }

  private static boolean isEmpty( final String s ) {
    return s == null || s.isEmpty();
  }
}
