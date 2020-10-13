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

import com.keenwrite.ExportFormat;
import com.keenwrite.FileEditorTab;
import com.keenwrite.FileType;
import com.keenwrite.preview.HTMLPreviewPane;
import com.keenwrite.processors.markdown.CaretPosition;

import java.nio.file.Path;
import java.util.Map;

import static com.keenwrite.AbstractFileFactory.lookup;

/**
 * Provides a context for configuring a chain of {@link Processor} instances.
 */
public class ProcessorContext {
  private final HTMLPreviewPane mPreviewPane;
  private final Map<String, String> mResolvedMap;
  private final ExportFormat mExportFormat;
  private final FileEditorTab mTab;
  private final Path mPath;
  private final FileType mFileType;

  /**
   * Creates a new context for use by the {@link ProcessorFactory} when
   * instantiating new {@link Processor} instances. Although all the
   * parameters are required, not all {@link Processor} instances will use
   * all parameters.
   *
   * @param previewPane Where to display the final (HTML) output.
   * @param resolvedMap Fully expanded interpolated strings.
   * @param tab         Tab containing path to the document to process.
   * @param format      Indicate configuration options for export format.
   */
  public ProcessorContext(
      final HTMLPreviewPane previewPane,
      final Map<String, String> resolvedMap,
      final FileEditorTab tab,
      final ExportFormat format ) {
    mPreviewPane = previewPane;
    mResolvedMap = resolvedMap;
    mTab = tab;
    mPath = mTab.getPath();
    mFileType = lookup( mPath );
    mExportFormat = format;
  }

  HTMLPreviewPane getPreviewPane() {
    return mPreviewPane;
  }

  Map<String, String> getResolvedMap() {
    return mResolvedMap;
  }

  public Path getPath() {
    return mPath;
  }

  public CaretPosition getCaretPosition() {
    return mTab.getCaretPosition();
  }

  FileType getFileType() {
    return mFileType;
  }

  public ExportFormat getExportFormat() {
    return mExportFormat;
  }

  @SuppressWarnings("SameParameterValue")
  boolean isExportFormat( final ExportFormat format ) {
    return mExportFormat == format;
  }
}
