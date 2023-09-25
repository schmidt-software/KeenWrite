/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.events.spelling;

import java.util.Map;

/**
 * Collates information about the lexicon. Fired when the lexicon has been
 * fully loaded into memory.
 */
public class LexiconLoadedEvent extends LexiconEvent {

  private final Map<String, Long> mLexicon;

  private LexiconLoadedEvent( final Map<String, Long> lexicon ) {
    mLexicon = lexicon;
  }

  public static void fire( final Map<String, Long> lexicon ) {
    new LexiconLoadedEvent( lexicon ).publish();
  }

  /**
   * Returns a word-frequency map used by the spell checking library.
   *
   * @return The lexicon that was loaded.
   */
  public Map<String, Long> getLexicon() {
    return mLexicon;
  }
}
