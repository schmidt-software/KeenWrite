/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite;

import com.keenwrite.io.File;
import com.keenwrite.service.Options;

import java.nio.file.Path;
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
    saveCaretPositions();
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
   * Restores the caret position for the given path.
   *
   * @param path The path to a file that was opened previously.
   * @return The stored caret position or 0 if the path has no associated
   * caret position persisted.
   */
  public int restoreCaretPosition( final Path path ) {
    return 0;
  }

  /**
   * Updates the dictionary to include project-specific words.
   * <p>
   * TODO: Implementation
   */
  public void restoreDictionary() {
  }

  /**
   * Stores all the caret positions for all the existing paths. If the caret
   * position is 0 for a given file, this will remove its entry from the
   * persistent store---the default position is 0 in the absence of a path.
   * <p>
   * TODO: Implementation
   */
  private void saveCaretPositions() {
  }
}
