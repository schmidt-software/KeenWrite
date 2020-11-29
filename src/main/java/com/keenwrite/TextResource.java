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
import javafx.scene.Node;

import java.nio.file.Files;
import java.nio.file.Path;

import static com.keenwrite.StatusBarNotifier.clue;

/**
 * A text resource can be persisted and retrieved from its persisted location.
 */
public interface TextResource {
  /**
   * Sets the text string that to be changed through some graphical user
   * interface. For example, a YAML document must be parsed from the given
   * text string into a tree view with which the user may interact.
   *
   * @param text The new content for the resource.
   */
  void setText( String text );

  /**
   * Returns the text string that may have been modified by the user through
   * some graphical user interface.
   *
   * @return The text value, based on the value set from
   * {@link #setText(String)}, but possibly mutated.
   */
  String getText();

  /**
   * Returns the file name, without any directory components, for this instance.
   * Useful for showing as a tab title.
   *
   * @return The file name value returned from {@link #getFile()}.
   */
  default String getFilename() {
    final var filename = getFile().toPath().getFileName();
    return filename == null ? "" : filename.toString();
  }

  /**
   * Returns the fully qualified {@link File} to the editable text resource.
   * Useful for showing as a tab tooltip, saving the file, or reading it.
   *
   * @return A non-null {@link File} instance.
   */
  File getFile();

  /**
   * Returns the fully qualified {@link Path} to the editable text resource.
   * This delegates to {@link #getFile()}.
   *
   * @return A non-null {@link Path} instance.
   */
  default Path getPath() {
    return getFile().toPath();
  }

  /**
   * Read the file contents and update the text accordingly. If the file
   * cannot be read then no changes will happen to the text. Fails silently.
   *
   * @param path The fully qualified {@link Path}, including a file name, to
   *             fully read into the editor.
   */
  default void readFile( final Path path ) {
    try {
      setText( Files.readString( path ) );
    } catch( final Exception ex ) {
      clue( ex );
    }
  }

  /**
   * Read the file contents and update the text accordingly. If the file
   * cannot be read then no changes will happen to the text. This delegates
   * to {@link #readFile(Path)}.
   *
   * @param file The {@link File} to fully read into the editor.
   */
  default void readFile( final File file ) {
    readFile( file.toPath() );
  }

  /**
   * Returns the node associated with this {@link TextResource}.
   *
   * @return The view component for the {@link TextResource}.
   */
  Node getNode();
}
