/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite;

import com.keenwrite.preferences.Key;

import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.Stack;

import static com.keenwrite.constants.Constants.APP_BUNDLE_NAME;
import static java.util.ResourceBundle.getBundle;

/**
 * Recursively resolves message properties. Property values can refer to other
 * properties using a <code>${var}</code> syntax.
 */
public final class Messages {

  private static final ResourceBundle RESOURCE_BUNDLE =
    getBundle( APP_BUNDLE_NAME );

  private Messages() {
  }

  /**
   * Return the value of a resource bundle value after having resolved any
   * references to other bundle variables.
   *
   * @param props The bundle containing resolvable properties.
   * @param s     The value for a key to resolve.
   * @return The value of the key with all references recursively dereferenced.
   */
  @SuppressWarnings( "SameParameterValue" )
  private static String resolve( final ResourceBundle props, final String s ) {
    final var len = s.length();
    final var stack = new Stack<StringBuilder>();
    var sb = new StringBuilder( 256 );
    var open = false;

    for( var i = 0; i < len; i++ ) {
      final var c = s.charAt( i );

      switch( c ) {
        case '$': {
          if( i + 1 < len && s.charAt( i + 1 ) == '{' ) {
            stack.push( sb );

            if( stack.size() > 20 ) {
              final var m = get( "Main.status.error.messages.recursion", s );
              throw new IllegalArgumentException( m );
            }

            sb = new StringBuilder( 256 );
            i++;
            open = true;
          }

          break;
        }

        case '}': {
          if( open ) {
            open = false;
            final var name = sb.toString();

            sb = stack.pop();
            sb.append( props.getString( name ) );
            break;
          }
        }

        default: {
          sb.append( c );
          break;
        }
      }
    }

    if( open ) {
      final var m = get( "Main.status.error.messages.syntax", s );
      throw new IllegalArgumentException( m );
    }

    return sb.toString();
  }

  /**
   * Returns the value for a key from the message bundle.
   *
   * @param key Retrieve the value for this key.
   * @return The value for the key.
   */
  public static String get( final String key ) {
    try {
      return resolve( RESOURCE_BUNDLE, RESOURCE_BUNDLE.getString( key ) );
    } catch( final Exception ignored ) {
      return key;
    }
  }

  /**
   * Returns the value for a key from the message bundle.
   *
   * @param key Retrieve the value for this key.
   * @return The value for the key.
   */
  public static String get( final Key key ) {
    return get( key.toString() );
  }

  public static String getLiteral( final String key ) {
    return RESOURCE_BUNDLE.getString( key );
  }

  public static String get( final String key, final boolean interpolate ) {
    return interpolate ? get( key ) : getLiteral( key );
  }

  /**
   * Returns the value for a key from the message bundle with the arguments
   * replacing <code>{#}</code> place holders.
   *
   * @param key  Retrieve the value for this key.
   * @param args The values to substitute for place holders.
   * @return The value for the key.
   */
  public static String get( final String key, final Object... args ) {
    return MessageFormat.format( get( key ), args );
  }

  /**
   * Answers whether the given key is contained in the application's messages
   * properties file.
   *
   * @param key The key to look for in the {@link ResourceBundle}.
   * @return {@code true} when the key exists as an exact match.
   */
  public static boolean containsKey( final String key ) {
    return RESOURCE_BUNDLE.containsKey( key );
  }

  /**
   * Returns all key names in the application's messages properties file.
   *
   * @return All key names in the {@link ResourceBundle} encapsulated by
   * this class.
   */
  public static Enumeration<String> getKeys() {
    return RESOURCE_BUNDLE.getKeys();
  }
}
