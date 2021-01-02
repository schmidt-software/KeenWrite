/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite;

import com.keenwrite.io.FileType;

import java.nio.file.Path;

import static com.keenwrite.Constants.GLOB_PREFIX_FILE;
import static com.keenwrite.Constants.sSettings;
import static com.keenwrite.io.FileType.UNKNOWN;
import static com.keenwrite.predicates.PredicateFactory.createFileTypePredicate;

/**
 * Provides common behaviours for factories that instantiate classes based on
 * file type.
 */
public abstract class AbstractFileFactory {

  /**
   * Determines the file type from the path extension. This should only be
   * called when it is known that the file type won't be a definition file
   * (e.g., YAML or other definition source), but rather an editable file
   * (e.g., Markdown, XML, etc.).
   *
   * @param path The path with a file name extension.
   * @return The FileType for the given path.
   */
  public static FileType lookup( final Path path ) {
    assert path != null;

    return lookup( path, GLOB_PREFIX_FILE );
  }

  /**
   * Creates a file type that corresponds to the given path.
   *
   * @param path   Reference to a variable definition file.
   * @param prefix One of GLOB_PREFIX_DEFINITION or GLOB_PREFIX_FILE.
   * @return The file type that corresponds to the given path.
   */
  protected static FileType lookup( final Path path, final String prefix ) {
    assert path != null;
    assert prefix != null;

    final var keys = sSettings.getKeys( prefix );

    var found = false;
    var fileType = UNKNOWN;

    while( keys.hasNext() && !found ) {
      final var key = keys.next();
      final var patterns = sSettings.getStringSettingList( key );
      final var predicate = createFileTypePredicate( patterns );

      if( found = predicate.test( path.toFile() ) ) {
        // Remove the EXTENSIONS_PREFIX to get the file name extension mapped
        // to a standard name (as defined in the settings.properties file).
        final String suffix = key.replace( prefix + '.', "" );
        fileType = FileType.from( suffix );
      }
    }

    return fileType;
  }
}
