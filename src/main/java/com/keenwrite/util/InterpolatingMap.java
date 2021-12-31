package com.keenwrite.util;

import com.keenwrite.sigils.SigilOperator;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
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

  private final SigilOperator mOperator;

  /**
   * @param operator Contains the opening and closing sigils that mark
   *                 where variable names begin and end.
   */
  public InterpolatingMap( final SigilOperator operator ) {
    super( INITIAL_CAPACITY );

    assert operator != null;
    mOperator = operator;
  }

  /**
   * @param operator Contains the opening and closing sigils that mark
   *                 where variable names begin and end.
   * @param m        The initial {@link Map} to copy into this instance.
   */
  public InterpolatingMap(
    final SigilOperator operator, final Map<String, String> m ) {
    this( operator );
    putAll( m );
  }

  /**
   * Interpolates a single text string based on values in this map.
   *
   * @param text The text string to interpolate.
   * @return The text string with all variable definitions resolved.
   */
  public String interpolate( final String text ) {
    assert text != null;

    final var failures = new AtomicInteger();

    return interpolate( text, createPattern(), failures );
  }

  /**
   * Interpolates all values in the map that reference other values by way
   * of key names. Performs a non-greedy match of key names delimited by
   * definition tokens. This operation modifies the map directly.
   *
   * @return The number of failed substitutions.
   */
  public int interpolate() {
    final var failures = new AtomicInteger();

    for( final var k : keySet() ) {
      replace( k, interpolate( get( k ), createPattern(), failures ) );
    }

    return failures.get();
  }

  /**
   * Given a value with zero or more key references, this will resolve all
   * the values, recursively. If a key cannot be de-referenced, the value will
   * contain the key name, including the original sigils.
   *
   * @param value    Value containing zero or more key references.
   * @param pattern  The regular expression pattern to match variable key names.
   * @param failures Incremented when a variable replacement fails.
   * @return The given value with all embedded key references interpolated.
   */
  private String interpolate(
    String value, final Pattern pattern, final AtomicInteger failures ) {
    assert value != null;
    assert pattern != null;
    assert failures != null;

    final var matcher = pattern.matcher( value );

    while( matcher.find() ) {
      final var keyName = matcher.group( GROUP_DELIMITED );
      final var mapValue = get( keyName );

      if( mapValue == null ) {
        failures.incrementAndGet();
      }
      else {
        final var keyValue = interpolate( mapValue, pattern, failures );
        value = value.replace( mOperator.entoken( keyName ), keyValue );
      }
    }

    return value;
  }

  private Pattern createPattern() {
    final var sigils = mOperator.getSigils();
    return compile( format(
      "%s(.*?)%s", quote( sigils.getBegan() ), quote( sigils.getEnded() )
    ) );
  }
}
