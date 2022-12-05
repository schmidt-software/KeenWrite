/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.spelling.impl;

import com.keenwrite.events.spelling.LexiconLoadedEvent;
import com.keenwrite.exceptions.MissingFileException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;

import static com.keenwrite.constants.Constants.LEXICONS_DIRECTORY;
import static com.keenwrite.events.StatusEvent.clue;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Responsible for loading a set of single words, asynchronously.
 */
public final class Lexicon {
  /**
   * Most lexicons have 100,000 words.
   */
  private static final int LEXICON_CAPACITY = 100_000;

  /**
   * The word-frequency entries are tab-delimited.
   */
  private static final char DELIMITER = '\t';

  /**
   * Load the lexicon into memory then fire an event indicating that the
   * word-frequency pairs are available to use for spellchecking. This
   * happens asynchronously so that the UI can load faster.
   *
   * @param locale The locale having a corresponding lexicon to load.
   */
  public static void read( final Locale locale ) {
    assert locale != null;

    new Thread( read( toResourcePath( locale ) ) ).start();
  }

  private static Runnable read( final String path ) {
    return () -> {
      try( final var resource = openResource( path ) ) {
        read( resource );
      } catch( final Exception ex ) {
        clue( ex );
      }
    };
  }

  private static void read( final InputStream resource ) {
    try( final var input = new InputStreamReader( resource, UTF_8 );
         final var reader = new BufferedReader( input ) ) {
      read( reader );
    } catch( final Exception ex ) {
      clue( ex );
    }
  }

  private static void read( final BufferedReader reader ) {
    try {
      long count = 0;
      final var lexicon = new HashMap<String, Long>( LEXICON_CAPACITY );
      String line;

      while( (line = reader.readLine()) != null ) {
        final var index = line.indexOf( DELIMITER );
        final var word = line.substring( 0, index == -1 ? 0 : index );
        final var frequency = parse( line.substring( index + 1 ) );

        lexicon.put( word, frequency );

        // Slower machines may benefit users by showing a loading message.
        if( ++count % 25_000 == 0 ) {
          status( "loading", count );
        }
      }

      // Indicate that loading the lexicon is finished.
      status( "loaded", count );
      LexiconLoadedEvent.fire( lexicon );
    } catch( final Exception ex ) {
      clue( ex );
    }
  }

  /**
   * Prevents autoboxing and uses cached values when possible. A return value
   * of 0L means that the word will receive the lowest priority. If there's
   * an error (i.e., data corruption) parsing the number, the spell checker
   * will still work, but be suboptimal for all erroneous entries.
   *
   * @param number The numeric value to parse into a long object.
   * @return The parsed value, or 0L if the number couldn't be parsed.
   */
  private static Long parse( final String number ) {
    try {
      return Long.valueOf( number );
    } catch( final NumberFormatException ex ) {
      clue( ex );
      return 0L;
    }
  }

  private static InputStream openResource( final String path )
    throws MissingFileException {
    final var resource = Lexicon.class.getResourceAsStream( path );

    if( resource == null ) {
      throw new MissingFileException( path );
    }

    return resource;
  }

  /**
   * Convert a {@link Locale} into a path that can be loaded as a resource.
   *
   * @param locale The {@link Locale} to convert to a resource.
   * @return The slash-separated path to a lexicon resource file.
   */
  private static String toResourcePath( final Locale locale ) {
    final var language = locale.getLanguage();
    return format( "/%s/%s.txt", LEXICONS_DIRECTORY, language );
  }

  private static void status( final String s, final long count ) {
    clue( "Main.status.lexicon." + s, count );
  }
}
