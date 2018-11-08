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
package com.scrivenvar;

import static com.scrivenvar.Constants.GLOB_PREFIX_FILE;
import com.scrivenvar.predicates.files.FileTypePredicate;
import com.scrivenvar.service.Settings;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

/**
 * Provides common behaviours for factories that instantiate classes based on
 * file type.
 *
 * @author White Magic Software, Ltd.
 */
public class AbstractFileFactory {

  private final Settings settings = Services.load( Settings.class );

  /**
   * Determines the file type from the path extension. This should only be
   * called when it is known that the file type won't be a definition file
   * (e.g., YAML or other definition source), but rather an editable file
   * (e.g., Markdown, XML, etc.).
   *
   * @param path The path with a file name extension.
   *
   * @return The FileType for the given path.
   */
  public FileType lookup( final Path path ) {
    return lookup( path, GLOB_PREFIX_FILE );
  }

  /**
   * Creates a file type that corresponds to the given path.
   *
   * @param path Reference to a variable definition file.
   * @param prefix One of GLOB_PREFIX_DEFINITION or GLOB_PREFIX_FILE.
   *
   * @return The file type that corresponds to the given path.
   */
  protected FileType lookup( final Path path, final String prefix ) {
    final Settings properties = getSettings();
    final Iterator<String> keys = properties.getKeys( prefix );

    boolean found = false;
    FileType fileType = null;

    while( keys.hasNext() && !found ) {
      final String key = keys.next();
      final List<String> patterns = properties.getStringSettingList( key );
      final FileTypePredicate predicate = new FileTypePredicate( patterns );

      if( found = predicate.test( path.toFile() ) ) {
        // Remove the EXTENSIONS_PREFIX to get the filename extension mapped
        // to a standard name (as defined in the settings.properties file).
        final String suffix = key.replace( prefix + ".", "" );
        fileType = FileType.from( suffix );
      }
    }

    return fileType;
  }

  /**
   * Throws IllegalArgumentException because the given path could not be
   * recognized.
   *
   * @param type The detected path type (protocol, file extension, etc.).
   * @param path The path to a source of definitions.
   */
  protected void unknownFileType( final String type, final String path ) {
    throw new IllegalArgumentException(
      "Unknown type '" + type + "' for '" + path + "'."
    );
  }

  /**
   * Throws IllegalArgumentException because the extension for the given path
   * could not be recognized.
   *
   * @param path The path to a file that could not be loaded.
   */
  protected void unknownExtension( final Path path ) {
    throw new IllegalArgumentException(
      "Unknown extension for '" + path + "'."
    );
  }

  /**
   * Return the singleton Settings instance.
   *
   * @return A non-null instance.
   */
  private Settings getSettings() {
    return this.settings;
  }
}
