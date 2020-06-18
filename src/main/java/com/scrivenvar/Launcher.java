/*
 * Copyright 2020 White Magic Software, Ltd.
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
package com.scrivenvar;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Properties;

import static java.lang.String.format;

/**
 * Launches the application using the {@link Main} class.
 *
 * <p>
 * This is required until modules are implemented, which may never happen
 * because the application should be ported away from Java and JavaFX.
 * </p>
 */
public class Launcher {
  /**
   * Delegates to the application entry point.
   *
   * @param args Command-line arguments.
   */
  public static void main( final String[] args ) throws IOException {
    // Shhh.
    System.err.close();

    showAppInfo();
    Main.main( args );
  }

  @SuppressWarnings("RedundantStringFormatCall")
  private static void showAppInfo() throws IOException {
    out( format( "%s version %s", getTitle(), getVersion() ) );
    out( format( "Copyright %s by White Magic Software, Ltd.", getYear() ) );
    out( format( "Portions copyright 2020 Karl Tauber." ) );
  }

  private static void out( final String s ) {
    System.out.println( s );
  }

  private static String getTitle() throws IOException {
    final Properties properties = loadProperties( "messages.properties" );
    return properties.getProperty( "Main.title" );
  }

  private static String getVersion() throws IOException {
    final Properties properties = loadProperties( "app.properties" );
    return properties.getProperty( "application.version" );
  }

  private static String getYear() {
    return Integer.toString( Calendar.getInstance().get( Calendar.YEAR ) );
  }

  @SuppressWarnings("SameParameterValue")
  private static Properties loadProperties( final String resource )
      throws IOException {
    final Properties properties = new Properties();
    properties.load( getResourceAsStream( getResourceName( resource ) ) );
    return properties;
  }

  private static String getResourceName( final String resource ) {
    return format( "%s/%s", getPackagePath(), resource );
  }

  private static String getPackagePath() {
    return Launcher.class.getPackageName().replace( '.', '/' );
  }

  private static InputStream getResourceAsStream( final String resource ) {
    return Launcher.class.getClassLoader().getResourceAsStream( resource );
  }
}
