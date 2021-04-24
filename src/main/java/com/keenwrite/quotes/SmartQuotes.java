/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.quotes;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

import static java.util.Arrays.binarySearch;
import static java.util.Collections.sort;

/**
 * Responsible for converting straight quotes into smart quotes. This must be
 * used on plain text. The class will not parse HTML, TeX, or non-English text.
 */
public class SmartQuotes {

  /**
   * The main regex captures all words that contain an apostrophe. The terms
   * inner, outer, began, and ended define where the apostrophes can be found
   * in a particular word. The following text contains 3 word matches against
   * the "inner" pattern:
   *
   * <p>
   * 'Janes' said, ''E'll be spooky, Sam's son with the jack-o'-lantern!',"
   * said the O'Mally twins'---y'know---ghosts in unison.'
   * </p>
   */
  private static final Map<String, Pattern> PATTERNS = Map.ofEntries(
    // @formatter:off
    createEntry( "inner", "(?<![\\p{L}'])(?:\\p{L}+')+\\p{L}+(?![\\p{L}'])" ),
    createEntry( "began", "(?<!\\p{L})(?:'\\p{L}+)+(?![\\p{L}'])" ),
    createEntry( "ended", "(?<![\\p{L}'])(?:\\p{L}+')+(?!\\p{L})" ),
    createEntry( "outer", "(?<!\\p{L})'\\p{L}+'(?!\\p{L})" ),
    createEntry( "years", "'(?=\\d{2}s?)" ),
    createEntry( "+ings", "[\\p{L}]{2,}in'\\s?" ),
    createEntry( "prime", "((-?[0-9]\\d*(\\.\\d+)?)\\\\?'\\s?(-?[0-9]\\d*(\\.\\d+)?)\\\\?\")|((-?[0-9]\\d*(\\.\\d+)?)(''|\")\\s?(x|×)\\s?(-?[0-9]\\d*(\\.\\d+)?)(''|\"))|((-?[0-9]\\d*(\\.\\d+)?)'')" ),
    createEntry( "texop", "``" ),
    createEntry( "texcl", "''" ),
    createEntry( "white", "(?!\\s+)\"|\"(?!\\s+)" ),
    createEntry( "slash", "\\\\\"" )
    // @formatter:on
  );

  private static SimpleEntry<String, Pattern> createEntry(
    final String key, final String regex ) {
    return new SimpleEntry<>( key, Pattern.compile( regex ) );
  }

  /**
   * Left single quote replacement text.
   */
  private static final String QUOTE_SINGLE_LEFT = "&lsquo;";

  /**
   * Right single quote replacement text.
   */
  private static final String QUOTE_SINGLE_RIGHT = "&rsquo;";

  /**
   * Left double quote replacement text.
   */
  private static final String QUOTE_DOUBLE_LEFT = "&ldquo;";

  /**
   * Right double quote replacement text.
   */
  private static final String QUOTE_DOUBLE_RIGHT = "&rdquo;";

  /**
   * Apostrophe replacement text.
   */
  private static final String APOSTROPHE = "&apos;";

  /**
   * Prime replacement text.
   */
  private static final String SINGLE_PRIME = "&prime;";

  /**
   * Double prime replacement text.
   */
  private static final String DOUBLE_PRIME = "&Prime;";

  /**
   * Temporary single quote marker near end of Unicode private use area.
   */
  private static final String SQ = "\uF8FE";

  /**
   * Temporary double quote marker near end of Unicode private use area.
   */
  private static final String DQ = "\uF8FD";

  private final Map<String, String[]> CONTRACTIONS = Map.ofEntries(
    load( "inner" ),
    load( "began" ),
    load( "ended" ),
    load( "outer" ),
    load( "verbs" )
  );

  public SmartQuotes() {
  }

  /**
   * Replaces straight single and double quotes with curly quotes or primes,
   * depending on the context.
   *
   * @param text The text that may contain straight single or double quotes.
   * @return All single and double quotes replaced with typographically
   * correct quotation marks.
   */
  public String replace( String text ) {
    // Replace known contractions.
    text = contractions( text );

    // Replace miscellaneous verb contractions.
    text = verbs( text );

    // Replace primes and double-primes (e.g., 5'4").
    text = primes( text );

    // Replace decade contractions.
    text = decades( text );

    // Replace contractions of words ending in "ing" (e.g., washin').
    text = suffixes( text );

    // Replace double backticks.
    text = backticks( text );

    // Unescape straight double quotes.
    text = escapes( text );

    return text;
  }

  /**
   * Replaces all strings in the given text that match the given pattern,
   * provided the functor answers {@code true} to the matched regex.
   *
   * @param text    The text to perform a replacement.
   * @param pattern The regular expression pattern to match.
   * @param filter  Controls whether a text replacement is made.
   * @return The given text with matching patterns replaced, conditionally.
   */
  private String replace( final String text,
                          final Pattern pattern,
                          final Function<String, Boolean> filter,
                          final Function<String, String> subst ) {
    final var sb = new StringBuilder( text.length() * 2 );
    final var matcher = pattern.matcher( text );

    while( matcher.find() ) {
      final var match = matcher.group( 0 );
      if( filter.apply( match ) ) {
        matcher.appendReplacement( sb, subst.apply( match ) );
      }
    }

    matcher.appendTail( sb );
    return sb.toString();
  }

  /**
   * Convenience method that always performs string replacement upon a match,
   * unconditionally.
   */
  private String apostrophize( final String text, final Pattern pattern ) {
    return apostrophize( text, pattern, ( match ) -> true );
  }

  private String apostrophize( final String text, final String pattern ) {
    return apostrophize( text, PATTERNS.get( pattern ) );
  }

  private String decades( final String text ) {
    return apostrophize( text, "years" );
  }

  private String suffixes( final String text ) {
    return apostrophize( text, "+ings" );
  }

  /**
   * Convenience method that replaces each straight quote in the given {@code
   * text} that passes through the given filter with an {@link #APOSTROPHE}.
   */
  private String apostrophize(
    final String text,
    final Pattern pattern,
    final Function<String, Boolean> filter ) {
    return replace(
      text,
      pattern,
      filter,
      ( match ) -> match.replaceAll( "'", APOSTROPHE ) );
  }

  private String contractions( String text ) {
    final var elements = new String[]{"inner", "began", "ended", "outer"};

    for( final var item : elements ) {
      final var pattern = PATTERNS.get( item );
      final var contractions = CONTRACTIONS.get( item );

      text = apostrophize(
        text,
        pattern,
        ( match ) -> binarySearch( contractions, match.toLowerCase() ) >= 0
      );
    }

    return text;
  }

  /**
   * Replaces verb endings, such as 'll and 've, with words not explicitly
   * listed as contractions in the dictionary sources.
   *
   * @param text The text to replace.
   * @return The given text with matching patterns replaced.
   */
  private String verbs( String text ) {
    for( final var contraction : CONTRACTIONS.get( "verbs" ) ) {
      final var pattern = Pattern.compile( "[\\p{L}]+" + contraction );
      text = apostrophize( text, pattern );
    }

    return text;
  }

  private String primes( final String text ) {
    System.out.println( "REPLACE: " + text);
    return replace(
      text,
      PATTERNS.get( "prime" ),
      ( match ) -> true,
      ( match ) -> match.replaceAll( "''", DOUBLE_PRIME )
                        .replaceAll( "\"", DOUBLE_PRIME )
                        .replaceAll( "'", SINGLE_PRIME )
                        .replaceAll( "\\\\", "" )
    );
  }

  /**
   * Replace all double backticks with opening double quote.
   */
  private String backticks( String text ) {
    final var sb = new StringBuilder( text.length() * 2 );
    final var opening = PATTERNS.get( "texop" );
    final var opener = opening.matcher( text );
    var count = 0;

    while( opener.find() ) {
      count++;
      opener.appendReplacement( sb, QUOTE_DOUBLE_LEFT );
    }

    opener.appendTail( sb );

    if( count > 0 ) {
      text = sb.toString();
      sb.setLength( 0 );

      final var closing = PATTERNS.get( "texcl" );
      final var closer = closing.matcher( text );
      while( count > 0 && closer.find() ) {
        count--;
        closer.appendReplacement( sb, QUOTE_DOUBLE_RIGHT );
      }

      closer.appendTail( sb );
    }

    return sb.toString();
  }

  private String escapes( final String text ) {
    return replace(
      text,
      PATTERNS.get( "slash" ),
      ( match ) -> true,
      ( match ) -> match.replaceAll( "\\\\", "" )
    );
  }

  /**
   * Reads the list of words containing contractions.
   */
  @SuppressWarnings( "SameParameterValue" )
  private SimpleEntry<String, String[]> load( final String prefix ) {
    // Allocate enough elements to hold all the contractions.
    final var result = new ArrayList<String>( 1024 );

    try( final var in = openResource( prefix + ".txt" ) ) {
      for( String line; ((line = in.readLine()) != null); ) {
        result.add( line );
      }

      sort( result );
    } catch( final Exception ex ) {
      throw new RuntimeException( ex );
    }

    return new SimpleEntry<>( prefix, result.toArray( new String[ 0 ] ) );
  }

  private BufferedReader openResource( final String filename ) {
    final var in = getClass().getResourceAsStream( filename );
    assert in != null;

    return new BufferedReader( new InputStreamReader( in ) );
  }
}
