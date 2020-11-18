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

import java.nio.file.Files;
import java.nio.file.Path;

import static com.keenwrite.StatusBarNotifier.clue;

/**
 * Responsible for communications between the definition model (the source)
 * and the definition view (the on-screen hierarchical editor). A definition
 * editor edits a model and is decoupled from where the model's data is loaded.
 * <p>
 * This controller, from the model-view-controller (MVC) paradigm, allows how
 * the main application provides a tab-based user interface.
 * </p>
 */
public class EditorController<View extends TextResource> {
  /**
   * The "model" of the MVC pattern.
   */
  private final Path mPath;

  /**
   * The "view" of the MVC pattern.
   */
  private final View mView;

  /**
   * Constructs a controller responsible for reading and writing the contents
   * at the given {@code path} that are displayed and changed using the given
   * {@code view}.
   *
   * @param path The path to the data source to read into the view.
   * @param view The user interface component for editing the data.
   */
  public EditorController( final Path path, final View view ) {
    assert path != null;
    assert view != null;

    mPath = path;
    mView = view;

    mView.setText( read( path ) );
  }

  protected View getView() {
    return mView;
  }

  protected String getFilename() {
    final var filename = getPath().getFileName();
    return filename == null ? "" : filename.toString();
  }

  private Path getPath() {
    return mPath;
  }

  private String read( final Path path ) {
    try {
      return Files.readString( path );
    } catch( final Exception ex ) {
      clue( ex );
    }

    return "";
  }
}
