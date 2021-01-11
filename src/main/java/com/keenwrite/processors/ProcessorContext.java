/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors;

import com.keenwrite.Constants;
import com.keenwrite.ExportFormat;
import com.keenwrite.io.FileType;
import com.keenwrite.preferences.Workspace;
import com.keenwrite.preview.HtmlPreview;
import com.keenwrite.Caret;

import java.nio.file.Path;
import java.util.Map;

import static com.keenwrite.AbstractFileFactory.lookup;
import static com.keenwrite.Constants.DEFAULT_DIRECTORY;

/**
 * Provides a context for configuring a chain of {@link Processor} instances.
 */
public final class ProcessorContext {
  private final HtmlPreview mHtmlPreview;
  private final Map<String, String> mResolvedMap;
  private final Path mDocumentPath;
  private final Caret mCaret;
  private final ExportFormat mExportFormat;
  private final Workspace mWorkspace;

  /**
   * Creates a new context for use by the {@link ProcessorFactory} when
   * instantiating new {@link Processor} instances. Although all the
   * parameters are required, not all {@link Processor} instances will use
   * all parameters.
   *
   * @param htmlPreview  Where to display the final (HTML) output.
   * @param resolvedMap  Fully expanded interpolated strings.
   * @param documentPath         Path to the document to process.
   * @param caret        Location of the caret in the edited document, which is
   *                     used to synchronize the scrollbars.
   * @param exportFormat Indicate configuration options for export format.
   */
  public ProcessorContext(
    final HtmlPreview htmlPreview,
    final Map<String, String> resolvedMap,
    final Path documentPath,
    final Caret caret,
    final ExportFormat exportFormat,
    final Workspace workspace ) {
    assert htmlPreview != null;
    assert resolvedMap != null;
    assert documentPath != null;
    assert caret != null;
    assert exportFormat != null;
    assert workspace != null;

    mHtmlPreview = htmlPreview;
    mResolvedMap = resolvedMap;
    mDocumentPath = documentPath;
    mCaret = caret;
    mExportFormat = exportFormat;
    mWorkspace = workspace;
  }

  @SuppressWarnings( "SameParameterValue" )
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
  public Caret getCaret() {
    return mCaret;
  }

  /**
   * Returns the directory that contains the file being edited.
   * When {@link Constants#DOCUMENT_DEFAULT} is created, the parent path is
   * {@code null}. This will get absolute path to the file before trying to
   * get te parent path, which should always be a valid path. In the unlikely
   * event that the base path cannot be determined by the path alone, the
   * default user directory is returned. This is necessary for the creation
   * of new files.
   *
   * @return Path to the directory containing a file being edited, or the
   * default user directory if the base path cannot be determined.
   */
  public Path getBaseDir() {
    final var path = getDocumentPath().toAbsolutePath().getParent();
    return path == null ? DEFAULT_DIRECTORY : path;
  }

  public Path getDocumentPath() {
    return mDocumentPath;
  }

  FileType getFileType() {
    return lookup( getDocumentPath() );
  }

  public Workspace getWorkspace() {
    return mWorkspace;
  }
}
