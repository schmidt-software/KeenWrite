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
package com.keenwrite.processors;

import com.keenwrite.Constants;
import com.keenwrite.ExportFormat;
import com.keenwrite.FileEditorController;
import com.keenwrite.FileType;
import com.keenwrite.preview.HtmlPreview;
import com.keenwrite.processors.markdown.CaretPosition;

import java.nio.file.Path;
import java.util.Map;

import static com.keenwrite.AbstractFileFactory.lookup;
import static com.keenwrite.Constants.DEFAULT_DIRECTORY;

/**
 * Provides a context for configuring a chain of {@link Processor} instances.
 */
public class ProcessorContext {
  private final HtmlPreview mHtmlPreview;
  private final Map<String, String> mResolvedMap;
  private final ExportFormat mExportFormat;
  private final Path mPath;
  private final CaretPosition mCaretPosition;

  /**
   * Creates a new context for use by the {@link ProcessorFactory} when
   * instantiating new {@link Processor} instances. Although all the
   * parameters are required, not all {@link Processor} instances will use
   * all parameters.
   *
   * @param htmlPreview  Where to display the final (HTML) output.
   * @param resolvedMap  Fully expanded interpolated strings.
   * @param tab          Tab containing path to the document to process.
   * @param exportFormat Indicate configuration options for export format.
   * @deprecated Use {@link ProcessorContext} with {@link Path}.
   */
  @Deprecated
  public ProcessorContext(
      final HtmlPreview htmlPreview,
      final Map<String, String> resolvedMap,
      final FileEditorController tab,
      final ExportFormat exportFormat ) {
    this( htmlPreview,
          resolvedMap,
          tab.getPath(),
          tab.getCaretPosition(),
          exportFormat );
  }

  /**
   * Creates a new context for use by the {@link ProcessorFactory} when
   * instantiating new {@link Processor} instances. Although all the
   * parameters are required, not all {@link Processor} instances will use
   * all parameters.
   *
   * @param htmlPreview   Where to display the final (HTML) output.
   * @param resolvedMap   Fully expanded interpolated strings.
   * @param path          Path to the document to process.
   * @param caretPosition Location of the caret in the edited document, which is
   *                      used to synchronize the scrollbars.
   * @param exportFormat  Indicate configuration options for export format.
   */
  public ProcessorContext(
      final HtmlPreview htmlPreview,
      final Map<String, String> resolvedMap,
      final Path path,
      final CaretPosition caretPosition,
      final ExportFormat exportFormat ) {
    assert htmlPreview != null;
    assert resolvedMap != null;
    assert path != null;
    assert caretPosition != null;
    assert exportFormat != null;

    mHtmlPreview = htmlPreview;
    mResolvedMap = resolvedMap;
    mPath = path;
    mCaretPosition = caretPosition;
    mExportFormat = exportFormat;
  }

  @SuppressWarnings("SameParameterValue")
  boolean isExportFormat( final ExportFormat format ) {
    return mExportFormat == format;
  }

  HtmlPreview getPreview() {
    return mHtmlPreview;
  }

  /**
   * Returns the variable map of interpolated definitions.
   *
   * @return A map to help dereference variables.
   */
  Map<String, String> getResolvedMap() {
    return mResolvedMap;
  }

  public ExportFormat getExportFormat() {
    return mExportFormat;
  }

  /**
   * Returns the current caret position in the document being edited and is
   * always up-to-date.
   *
   * @return Caret position in the document.
   */
  public CaretPosition getCaretPosition() {
    return mCaretPosition;
  }

  /**
   * Returns the directory that contains the file being edited.
   * When {@link Constants#DEFAULT_DOCUMENT} is created, the parent path is
   * {@code null}. This will get absolute path to the file before trying to
   * get te parent path, which should always be a valid path. In the unlikely
   * event that the base path cannot be determined by the path alone, the
   * default user directory is returned. This is necessary for the creation
   * of new files.
   *
   * @return Path to the directory containing a file being edited, or the
   * default user directory if the base path cannot be determined.
   */
  public Path getBasePath() {
    final var path = getPath().toAbsolutePath().getParent();
    return path == null ? DEFAULT_DIRECTORY : path;
  }

  public Path getPath() {
    return mPath;
  }

  FileType getFileType() {
    return lookup( getPath() );
  }
}
