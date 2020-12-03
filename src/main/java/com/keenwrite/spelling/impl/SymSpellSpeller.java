/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.spelling.impl;

import com.keenwrite.exceptions.MissingFileException;
import com.keenwrite.spelling.api.SpellCheckListener;
import com.keenwrite.spelling.api.SpellChecker;
import io.gitlab.rxp90.jsymspell.SuggestItem;
import io.gitlab.rxp90.jsymspell.SymSpell;
import io.gitlab.rxp90.jsymspell.SymSpellBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.keenwrite.Constants.LEXICONS_DIRECTORY;
import static com.keenwrite.StatusBarNotifier.clue;
import static io.gitlab.rxp90.jsymspell.SymSpell.Verbosity;
import static io.gitlab.rxp90.jsymspell.SymSpell.Verbosity.ALL;
import static io.gitlab.rxp90.jsymspell.SymSpell.Verbosity.CLOSEST;
import static java.lang.Character.isLetter;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Responsible for spell checking using {@link SymSpell}.
 */
public class SymSpellSpeller implements SpellChecker {
  private final BreakIterator mBreakIterator = BreakIterator.getWordInstance();

  private final SymSpell mSymSpell;

  /**
   * Creates a new spellchecker for a lexicon of words found in the specified
   * file.
   *
   * @param filename Lexicon language file (e.g., "en.txt").
   * @return An instance of {@link SpellChecker} that can check if a word
   * is correct and suggest alternatives, or {@link PermissiveSpeller} if the
   * lexicon cannot be loaded.
   */
  public static SpellChecker forLexicon( final String filename ) {
    try {
      final Collection<String> lexicon = readLexicon( filename );
      return SymSpellSpeller.forLexicon( lexicon );
    } catch( final Exception ex ) {
      clue( ex );
      return new PermissiveSpeller();
    }
  }

  private static SpellChecker forLexicon(
      final Collection<String> lexiconWords ) {
    assert lexiconWords != null && !lexiconWords.isEmpty();

    final SymSpellBuilder builder = new SymSpellBuilder()
        .setLexiconWords( lexiconWords );

    return new SymSpellSpeller( builder.build() );
  }

  /**
   * Prevent direct instantiation so that only the {@link SpellChecker}
   * interface
   * is available.
   *
   * @param symSpell The implementation-specific spell checker.
   */
  private SymSpellSpeller( final SymSpell symSpell ) {
    mSymSpell = symSpell;
  }

  @Override
  public boolean inLexicon( final String lexeme ) {
    return lookup( lexeme, CLOSEST ).size() == 1;
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
      final String text, final SpellCheckListener consumer ) {
    assert text != null;
    assert consumer != null;

    mBreakIterator.setText( text );

    int boundaryIndex = mBreakIterator.first();
    int previousIndex = 0;

    while( boundaryIndex != BreakIterator.DONE ) {
      final var lex = text.substring( previousIndex, boundaryIndex )
                          .toLowerCase();

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

  @SuppressWarnings("SameParameterValue")
  private static Collection<String> readLexicon( final String filename )
      throws Exception {
    final var path = '/' + LEXICONS_DIRECTORY + '/' + filename;

    try( final var resource =
             SymSpellSpeller.class.getResourceAsStream( path ) ) {
      if( resource == null ) {
        throw new MissingFileException( path );
      }

      try( final var isr = new InputStreamReader( resource, UTF_8 );
           final var reader = new BufferedReader( isr ) ) {
        return reader.lines().collect( Collectors.toList() );
      }
    }
  }

  /**
   * Answers whether the given string is likely a word by checking the first
   * character.
   *
   * @param word The word to check.
   * @return {@code true} if the word begins with a letter.
   */
  private boolean isWord( final String word ) {
    return !word.isEmpty() && isLetter( word.charAt( 0 ) );
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
    return getSpeller().lookup( lexeme, v );
  }

  private SymSpell getSpeller() {
    return mSymSpell;
  }
}
