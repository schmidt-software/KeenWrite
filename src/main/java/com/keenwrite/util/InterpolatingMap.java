package com.keenwrite.util;

import com.keenwrite.sigils.SigilOperator;
import com.keenwrite.sigils.Sigils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.util.regex.Pattern.compile;
import static java.util.regex.Pattern.quote;

public class InterpolatingMap extends ConcurrentHashMap<String, String> {
  private static final int GROUP_DELIMITED = 1;

  /**
   * Used to override the default initial capacity in {@link HashMap}.
   */
  private static final int INITIAL_CAPACITY = 1 << 8;

  public InterpolatingMap() {
    super( INITIAL_CAPACITY );
  }

  /**
   * Interpolates all values in the map that reference other values by way
   * of key names. Performs a non-greedy match of key names delimited by
   * definition tokens. This operation modifies the map directly.
   *
   * @param operator Contains the opening and closing sigils that mark
   *                 where variable names begin and end.
   * @return {@code this}
   */
  public Map<String, String> interpolate( final SigilOperator operator ) {
    sigilize( operator );
    interpolate( operator.getSigils() );
    return this;
  }

  /**
   * Wraps each key in this map with the starting and ending sigils provided
   * by the given {@link SigilOperator}. This operation modifies the map
   * directly.
   *
   * @param operator Container for starting and ending sigils.
   */
  private void sigilize( final SigilOperator operator ) {
    forEach( ( k, v ) -> put( operator.entoken( k ), v ) );
  }

  /**
   * Interpolates all values in the map that reference other values by way
   * of key names. Performs a non-greedy match of key names delimited by
   * definition tokens. This operation modifies the map directly.
   *
   * @param sigils Contains the opening and closing sigils that mark
   *               where variable names begin and end.
   */
  private void interpolate( final Sigils sigils ) {
    final var pattern = compile(
      format(
        "(%s.*?%s)", quote( sigils.getBegan() ), quote( sigils.getEnded() )
      )
    );

    replaceAll( ( k, v ) -> resolve( v, pattern ) );
  }

  /**
   * Given a value with zero or more key references, this will resolve all
   * the values, recursively. If a key cannot be de-referenced, the value will
   * contain the key name.
   *
   * @param value   Value containing zero or more key references.
   * @param pattern The regular expression pattern to match variable key names.
   * @return The given value with all embedded key references interpolated.
   */
  private String resolve( String value, final Pattern pattern ) {
    final var matcher = pattern.matcher( value );

    while( matcher.find() ) {
      final var keyName = matcher.group( GROUP_DELIMITED );
      final var mapValue = get( keyName );
      final var keyValue = mapValue == null
        ? keyName
        : resolve( mapValue, pattern );

      value = value.replace( keyName, keyValue );
    }

    return value;
  }
}
