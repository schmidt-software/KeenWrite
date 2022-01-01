/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors.r;

import com.keenwrite.processors.ProcessorContext;
import com.keenwrite.processors.VariableProcessor;
import com.keenwrite.sigils.RKeyOperator;

import java.util.function.UnaryOperator;

/**
 * Converts the keys of the resolved map from default form to R form, then
 * performs a substitution on the text. The default R variable syntax is
 * <pre>v$tree$leaf</pre>.
 */
public final class RVariableProcessor extends VariableProcessor {
  public RVariableProcessor(
    final InlineRProcessor irp, final ProcessorContext context ) {
    super( irp, context );
  }

  @Override
  protected UnaryOperator<String> createKeyOperator(
    final ProcessorContext context ) {
    return new RKeyOperator();
  }

  @Override
  protected String processValue( final String value ) {
    assert value != null;

    return escape( value );
  }

  /**
   * In R, single quotes and double quotes are interchangeable. Using single
   * quotes is simpler to code.
   *
   * @param value The text to convert into a valid quoted R string.
   * @return The quoted value with embedded quotes escaped as necessary.
   */
  public static String escape( final String value ) {
    return '\'' + escape( value, '\'', "\\'" ) + '\'';
  }

  /**
   * TODO: Make generic method for replacing text.
   *
   * @param haystack Search this string for the needle, must not be null.
   * @param needle   The character to find in the haystack.
   * @param thread   Replace the needle with this text, if the needle is found.
   * @return The haystack with the all instances of needle replaced with thread.
   */
  @SuppressWarnings( "SameParameterValue" )
  private static String escape(
    final String haystack, final char needle, final String thread ) {
    assert haystack != null;
    assert thread != null;

    int end = haystack.indexOf( needle );

    if( end < 0 ) {
      return haystack;
    }

    int start = 0;

    // Replace up to 32 occurrences before reallocating the internal buffer.
    final var sb = new StringBuilder( haystack.length() + 32 );

    while( end >= 0 ) {
      sb.append( haystack, start, end ).append( thread );
      start = end + 1;
      end = haystack.indexOf( needle, start );
    }

    return sb.append( haystack.substring( start ) ).toString();
  }
}
