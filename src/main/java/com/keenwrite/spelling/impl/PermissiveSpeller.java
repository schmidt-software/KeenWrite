/*
 * Copyright 2020 White Magic Software, Ltd.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
