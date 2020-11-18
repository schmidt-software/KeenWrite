/* Copyright 2020 White Magic Software, Ltd.
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
package com.keenwrite;

import com.keenwrite.io.File;
import com.keenwrite.service.Options;

import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for defining behaviours for separate projects. A workspace has
 * the ability to save and restore a session, including the window dimensions,
 * tab setup, files, and user preferences.
 */
public class Workspace {
  /**
   * This variable must be declared before all other variables to prevent
   * subsequent initializations from failing due to missing user preferences.
   */
  private static final Options sOptions = Services.load( Options.class );

  /**
   * Constructs a new workspace with the given identifier.
   *
   * @param name The unique identifier for this workspace.
   */
  public Workspace( final String name ) {
  }

  /**
   * Saves the current workspace.
   */
  public void save() {
  }

  /**
   * Returns the list of files opened for this {@link Workspace}.
   *
   * @return A non-null, possibly empty list of {@link File} instances.
   */
  public List<File> restoreFiles() {
    final var filenames = sOptions.getStrings( "file" );
    final var files = new ArrayList<File>();

    for( final var filename : filenames ) {
      files.add( new File( filename ) );
    }

    return files;
  }

  /**
   * Updates the dictionary to include project-specific words.
   *
   * TODO: Implementation
   */
  public void restoreDictionary() {
  }
}
