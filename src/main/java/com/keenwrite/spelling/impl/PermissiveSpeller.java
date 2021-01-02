/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.spelling.impl;

import com.keenwrite.spelling.api.SpellCheckListener;
import com.keenwrite.spelling.api.SpellChecker;

import java.util.List;

/**
 * Responsible for spell checking in the event that a real spell checking
 * implementation cannot be created (for any reason). Does not perform any
 * spell checking and indicates that any given lexeme is in the lexicon.
 */
public class PermissiveSpeller implements SpellChecker {
  /**
   * Returns {@code true}, ignoring the given word.
   *
   * @param ignored Unused.
   * @return {@code true}
   */
  @Override
  public boolean inLexicon( final String ignored ) {
    return true;
  }

  /**
   * Returns an array with the given lexeme.
   *
   * @param lexeme  The word to return.
   * @param ignored Unused.
   * @return A suggestion list containing the given lexeme.
   */
  @Override
  public List<String> suggestions( final String lexeme, final int ignored ) {
    return List.of( lexeme );
  }

  /**
   * Performs no action.
   *
   * @param text    Unused.
   * @param ignored Uncalled.
   */
  @Override
  public void proofread(
      final String text, final SpellCheckListener ignored ) {
  }
}
