/*
 * Copyright 2016 White Magic Software, Ltd.
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
package com.scrivenvar.predicates.files;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.util.Collection;
import java.util.function.Predicate;

/**
 * Responsible for testing whether a given path (to a file) matches one of the
 * filename extension patterns provided during construction.
 *
 * @author White Magic Software, Ltd.
 */
public class FileTypePredicate implements Predicate<File> {

  private final PathMatcher mMatcher;

  /**
   * Constructs a new instance given a set of file extension globs.
   *
   * @param patterns Comma-separated list of globbed extensions including the
   * Kleene star (e.g., <code>*.md,*.markdown,*.txt</code>).
   */
  public FileTypePredicate( final String patterns ) {
    mMatcher = FileSystems.getDefault().getPathMatcher(
      "glob:**{" + patterns + "}"
    );
  }

  /**
   * Constructs a new instance given a list of file extension globs, each must
   * include the Kleene star (a.k.a. asterisk).
   *
   * @param patterns Collection of globbed extensions.
   */
  public FileTypePredicate( final Collection<String> patterns ) {
    this( String.join( ",", patterns ) );
  }

  /**
   * Returns true if the file matches the patterns defined during construction.
   *
   * @param file The filename to match against the given glob patterns.
   *
   * @return false The filename does not match the glob patterns.
   */
  @Override
  public boolean test( final File file ) {
    return getMatcher().matches( file.toPath() );
  }

  private PathMatcher getMatcher() {
    return mMatcher;
  }
}
