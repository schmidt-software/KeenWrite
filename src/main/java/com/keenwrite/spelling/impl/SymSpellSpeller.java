/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.spelling.impl;

import com.keenwrite.exceptions.MissingFileException;
import com.keenwrite.spelling.api.SpellCheckListener;
import com.keenwrite.spelling.api.SpellChecker;
import io.gitlab.rxp90.jsymspell.SymSpell;
import io.gitlab.rxp90.jsymspell.SymSpellBuilder;
import io.gitlab.rxp90.jsymspell.Verbosity;
import io.gitlab.rxp90.jsymspell.api.SuggestItem;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.keenwrite.constants.Constants.LEXICONS_DIRECTORY;
import static com.keenwrite.events.StatusEvent.clue;
import static io.gitlab.rxp90.jsymspell.Verbosity.ALL;
import static io.gitlab.rxp90.jsymspell.Verbosity.CLOSEST;
import static java.lang.Character.isLetter;
import static java.lang.Long.parseLong;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Responsible for spell checking using {@link SymSpell}.
 */
public class SymSpellSpeller implements SpellChecker {
  private final BreakIterator mBreakIterator = BreakIterator.getWordInstance();
  private final SymSpell mSymSpell;

  /**
   * Creates a new spellchecker for a lexicon of words in the specified file.
   *
   * @param filename Lexicon language file (e.g., "en.txt").
   * @return An instance of {@link SpellChecker} that can check if a word
   * is correct and suggest alternatives, or {@link PermissiveSpeller} if the
   * lexicon cannot be loaded.
   */
  public static SpellChecker forLexicon( final String filename ) {
    try {
      final var lexicon = readLexicon( filename );
      return SymSpellSpeller.forLexicon( lexicon );
    } catch( final Exception ex ) {
      clue( ex );
      return new PermissiveSpeller();
    }
  }

  private static SpellChecker forLexicon( final Map<String, Long> lexicon ) {
    assert lexicon != null && !lexicon.isEmpty();

    try {
      return new SymSpellSpeller(
        new SymSpellBuilder()
          .setUnigramLexicon( lexicon )
          .build()
      );
    } catch( final Exception ex ) {
      clue( ex );
      return new PermissiveSpeller();
    }
  }

  /**
   * Prevent direct instantiation so that only the {@link SpellChecker}
   * interface is available.
   *
   * @param symSpell The implementation-specific spell checker.
   */
  private SymSpellSpeller( final SymSpell symSpell ) {
    mSymSpell = symSpell;
  }

  /**
   * This expensive operation is only called for viable words, not for
   * single punctuation characters or whitespace.
   *
   * @param lexeme The word to check for correctness.
   * @return {@code false} if the word is not in the lexicon.
   */
  @Override
  public boolean inLexicon( final String lexeme ) {
    assert lexeme != null;
    assert !lexeme.isBlank();

    final var words = lookup( lexeme, CLOSEST );
    return !words.isEmpty() && lexeme.equals( words.get( 0 ).getSuggestion() );
  }

  @Override
  public List<String> suggestions( final String lexeme, int count ) {
    final List<String> result = new ArrayList<>( count );

    for( final var item : lookup( lexeme, ALL ) ) {
      if( count-- > 0 ) {
        result.add( item.getSuggestion() );
      }
      else {
        break;
      }
    }

    return result;
  }

  @Override
  public void proofread(
    final String text,
    final SpellCheckListener consumer ) {
    assert text != null;
    assert consumer != null;

    mBreakIterator.setText( text );

    int boundaryIndex = mBreakIterator.first();
    int previousIndex = 0;

    while( boundaryIndex != BreakIterator.DONE ) {
      final var lex =
        text.substring( previousIndex, boundaryIndex ).toLowerCase();

      // Get the lexeme for the possessive.
      final var pos = lex.endsWith( "'s" ) || lex.endsWith( "’s" );
      final var lexeme = pos ? lex.substring( 0, lex.length() - 2 ) : lex;

      if( isWord( lexeme ) && !inLexicon( lexeme ) ) {
        consumer.accept( lex, previousIndex, boundaryIndex );
      }

      previousIndex = boundaryIndex;
      boundaryIndex = mBreakIterator.next();
    }
  }

  @SuppressWarnings( "SameParameterValue" )
  private static Map<String, Long> readLexicon( final String filename )
    throws Exception {
    final var path = '/' + LEXICONS_DIRECTORY + '/' + filename;
    final var map = new HashMap<String, Long>();

    try( final var resource =
           SymSpellSpeller.class.getResourceAsStream( path ) ) {
      if( resource == null ) {
        throw new MissingFileException( path );
      }

      try( final var isr = new InputStreamReader( resource, UTF_8 );
           final var reader = new BufferedReader( isr ) ) {
        String line;

        while( (line = reader.readLine()) != null ) {
          final String[] tokens = line.split( "\\t" );
          map.put( tokens[ 0 ], parseLong( tokens[ 1 ] ) );
        }
      }
    }

    return map;
  }

  /**
   * Answers whether the given string is likely a word by checking the first
   * character.
   *
   * @param word The word to check.
   * @return {@code true} if the word begins with a letter.
   */
  private boolean isWord( final String word ) {
    return !word.isBlank() && isLetter( word.charAt( 0 ) );
  }

  /**
   * Returns a list of {@link SuggestItem} instances that provide alternative
   * spellings for the given lexeme.
   *
   * @param lexeme A word to look up in the lexicon.
   * @param v      Influences the number of results returned.
   * @return Alternative lexemes.
   */
  private List<SuggestItem> lookup( final String lexeme, final Verbosity v ) {
    return mSymSpell.lookup( lexeme, v );
  }
}
