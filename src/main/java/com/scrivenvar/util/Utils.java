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
package com.scrivenvar.util;

import java.util.ArrayList;
import java.util.prefs.Preferences;

/**
 * Responsible for trimming, storing, and retrieving strings.
 */
public class Utils {

  public static String ltrim( final String s ) {
    int i = 0;

    while( i < s.length() && Character.isWhitespace( s.charAt( i ) ) ) {
      i++;
    }

    return s.substring( i );
  }

  public static String rtrim( final String s ) {
    int i = s.length() - 1;

    while( i >= 0 && Character.isWhitespace( s.charAt( i ) ) ) {
      i--;
    }

    return s.substring( 0, i + 1 );
  }

  public static String[] getPrefsStrings( final Preferences prefs,
                                          String key ) {
    final ArrayList<String> arr = new ArrayList<>( 256 );

    for( int i = 0; i < 10000; i++ ) {
      final String s = prefs.get( key + (i + 1), null );

      if( s == null ) {
        break;
      }

      arr.add( s );
    }

    return arr.toArray( new String[ 0 ] );
  }

  public static void putPrefsStrings( Preferences prefs, String key,
                                      String[] strings ) {
    for( int i = 0; i < strings.length; i++ ) {
      prefs.put( key + (i + 1), strings[ i ] );
    }

    for( int i = strings.length; prefs.get( key + (i + 1),
                                            null ) != null; i++ ) {
      prefs.remove( key + (i + 1) );
    }
  }
}
