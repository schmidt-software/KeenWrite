/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.ui.explorer;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Responsible for providing the user with a way to select a file.
 */
public interface FilePicker {

  /**
   * Establishes the default file name to use when the UI is displayed. The
   * path portion of the file, if any, is ignored.
   *
   * @param file The initial {@link File} to choose when prompting the user
   *             to select a file.
   */
  default void setInitialFilename( File file ) {}

  /**
   * Establishes the directory to browse when the UI is displayed.
   *
   * @param path The initial {@link Path} to use when navigating the system.
   */
  default void setInitialDirectory( Path path ) {}

  /**
   * Sets the list of file names to display. For example, a single call to
   * this method with values of ("**.pdf", "Portable Document Format (PDF)")
   * would display only a file listing of PDF files.
   *
   * @param glob Pattern that allows matching file names to be listed.
   * @param text Human-readable description of the pattern.
   */
  default void addIncludeFileFilter( String glob, String text ) {}

  /**
   * Sets the list of file names to suppress. For example, a single call to
   * this method with values of (".*") would prevent listing files that begin
   * with a period.
   *
   * @param glob Pattern that allows matching file names to be suppressed.
   */
  default void addExcludeFileFilter( String glob ) {}

  /**
   * Returns the list of {@link File} objects selected by the user.
   *
   * @return A list of {@link File} objects, empty when nothing was selected.
   */
  Optional<List<File>> choose();
}
