/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors;

import com.keenwrite.preferences.Workspace;
import com.keenwrite.sigils.RSigilOperator;
import com.keenwrite.sigils.SigilOperator;
import com.keenwrite.sigils.YamlSigilOperator;

import java.util.HashMap;
import java.util.Map;

import static com.keenwrite.preferences.Workspace.*;

/**
 * Converts the keys of the resolved map from default form to R form, then
 * performs a substitution on the text. The default R variable syntax is
 * {@code v$tree$leaf}.
 */
public class RVariableProcessor extends DefinitionProcessor {

  private final SigilOperator mSigilOperator;

  public RVariableProcessor(
    final InlineRProcessor irp, final ProcessorContext context ) {
    super( irp, context );
    mSigilOperator = createSigilOperator( context.getWorkspace() );
  }

  /**
   * Returns the R-based version of the interpolated variable definitions.
   *
   * @return Variable names transmogrified from the default syntax to R syntax.
   */
  @Override
  protected Map<String, String> getDefinitions() {
    return toR( super.getDefinitions() );
  }

  /**
   * Converts the given map from regular variables to R variables.
   *
   * @param map Map of variable names to values.
   * @return Map of R variables.
   */
  private Map<String, String> toR( final Map<String, String> map ) {
    final var rMap = new HashMap<String, String>( map.size() );

    for( final var entry : map.entrySet() ) {
      final var key = entry.getKey();
      rMap.put( mSigilOperator.entoken( key ), toRValue( map.get( key ) ) );
    }

    return rMap;
  }

  private String toRValue( final String value ) {
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
  @SuppressWarnings("SameParameterValue")
  private String escape(
    final String haystack, final char needle, final String thread ) {
    int end = haystack.indexOf( needle );

    if( end < 0 ) {
      return haystack;
    }

    final int length = haystack.length();
    int start = 0;

    // Replace up to 32 occurrences before the string reallocates its buffer.
    final var sb = new StringBuilder( length + 32 );

    while( end >= 0 ) {
      sb.append( haystack, start, end ).append( thread );
      start = end + 1;
      end = haystack.indexOf( needle, start );
    }

    return sb.append( haystack.substring( start ) ).toString();
  }

  private SigilOperator createSigilOperator( final Workspace workspace ) {
    final var tokens = workspace.toTokens(
      KEY_R_DELIM_BEGAN, KEY_R_DELIM_ENDED );
    final var antecedent = createDefinitionOperator( workspace );
    return new RSigilOperator( tokens, antecedent );
  }

  private SigilOperator createDefinitionOperator(
    final Workspace workspace ) {
    final var tokens = workspace.toTokens(
      KEY_DEF_DELIM_BEGAN, KEY_DEF_DELIM_ENDED );
    return new YamlSigilOperator( tokens );
  }
}
