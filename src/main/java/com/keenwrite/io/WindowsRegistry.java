/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.io;

import java.io.IOException;
import java.util.function.Function;
import java.util.regex.Pattern;

import static java.io.File.pathSeparator;
import static java.lang.System.getenv;
import static java.util.regex.Pattern.compile;
import static java.util.regex.Pattern.quote;

/**
 * Responsible for obtaining Windows registry key values.
 */
public class WindowsRegistry {
  //@formatter:off
  private static final String SYS_KEY =
    "HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Control\\Session Manager\\Environment";
  private static final String USR_KEY =
    "HKEY_CURRENT_USER\\Environment";
  //@formatter:on

  /**
   * Regular expression pattern for matching %VARIABLE% names.
   */
  private static final String VAR_REGEX = "%.*?%";
  private static final Pattern VAR_PATTERN = compile( VAR_REGEX );

  private static final String REG_REGEX = "\\s*path\\s+REG_.*SZ\\s+(.*)";
  private static final Pattern REG_PATTERN = compile( REG_REGEX );

  /**
   * Returns the value of the Windows PATH registry key.
   *
   * @return The PATH environment variable if the registry query fails.
   */
  @SuppressWarnings( "SpellCheckingInspection" )
  public static String pathsWindows( final Function<String, String> map ) {
    try {
      final var hklm = query( SYS_KEY );
      final var hkcu = query( USR_KEY );

      return expand( hklm, map ) + pathSeparator + expand( hkcu, map );
    } catch( final IOException ex ) {
      return getenv( "PATH" );
    }
  }

  /**
   * Queries a registry key PATH value.
   *
   * @param key The registry key name to look up.
   * @return The value for the registry key.
   */
  private static String query( final String key ) throws IOException {
    final var registryVarName = "path";
    final var args = new String[]{"reg", "query", key, "/v", registryVarName};

    return SysFile.run( text -> text.contains( registryVarName ), args );
  }

  static String parseRegEntry( final String text ) {
    assert text != null;

    final var matcher = REG_PATTERN.matcher( text );
    return matcher.find() ? matcher.group( 1 ) : text.trim();
  }

  /**
   * PATH environment variables returned from the registry have unexpanded
   * variables of the form %VARIABLE%. This method will expand those values,
   * if possible, from the environment. This will only perform a single
   * expansion, which should be adequate for most needs.
   *
   * @param s The %VARIABLE%-encoded value to expand.
   * @return The given value with all encoded values expanded.
   */
  static String expand( final String s, final Function<String, String> map ) {
    // Assigned to the unexpanded string, initially.
    String expanded = s;

    final var matcher = VAR_PATTERN.matcher( expanded );

    while( matcher.find() ) {
      final var match = matcher.group( 0 );
      String value = map.apply( match );

      if( value == null ) {
        value = "";
      }
      else {
        value = value.replace( "\\", "\\\\" );
      }

      final var subexpression = compile( quote( match ) );
      expanded = subexpression.matcher( expanded ).replaceAll( value );
    }

    return expanded;
  }
}
