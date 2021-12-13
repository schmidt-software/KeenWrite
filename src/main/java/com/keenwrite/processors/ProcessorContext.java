/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors;

import com.keenwrite.Caret;
import com.keenwrite.ExportFormat;
import com.keenwrite.constants.Constants;
import com.keenwrite.editors.TextDefinition;
import com.keenwrite.io.FileType;
import com.keenwrite.preferences.Workspace;
import com.keenwrite.preview.HtmlPreview;
import com.keenwrite.util.GenericBuilder;
import javafx.beans.property.ObjectProperty;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

import static com.keenwrite.AbstractFileFactory.lookup;
import static com.keenwrite.constants.Constants.DEFAULT_DIRECTORY;

/**
 * Provides a context for configuring a chain of {@link Processor} instances.
 */
public final class ProcessorContext {

  private final Mutator mMutator;

  public static class Mutator {
    private HtmlPreview mHtmlPreview;
    private ObjectProperty<TextDefinition> mTextDefinition;
    private Path mInputPath;
    private Path mOutputPath;
    private Caret mCaret;
    private ExportFormat mExportFormat;
    private Workspace mWorkspace;

    public void setHtmlPreview( final HtmlPreview htmlPreview ) {
      mHtmlPreview = htmlPreview;
    }

    public void setTextDefinition(
      final ObjectProperty<TextDefinition> textDefinition ) {
      mTextDefinition = textDefinition;
    }

    public void setInputPath( final Path inputPath ) {
      mInputPath = inputPath;
    }

    public void setInputPath( final File inputPath ) {
      setInputPath( inputPath.toPath() );
    }

    public void setOutputPath( final Path outputPath ) {
      mOutputPath = outputPath;
    }

    public void setOutputPath( final File outputPath ) {
      setOutputPath( outputPath.toPath() );
    }

    public void setCaret( final Caret caret ) {
      mCaret = caret;
    }

    public void setExportFormat( final ExportFormat exportFormat ) {
      mExportFormat = exportFormat;
    }

    public void setWorkspace( final Workspace workspace ) {
      mWorkspace = workspace;
    }
  }

  public static GenericBuilder<Mutator, ProcessorContext> builder() {
    return GenericBuilder.of(
      Mutator::new,
      ProcessorContext::new
    );
  }

  /**
   * @param inputPath      Path to the document to process.
   * @param outputPath     Fully qualified filename to use when exporting.
   * @param format         Indicate configuration options for export format.
   * @param preview        Where to display the final (HTML) output.
   * @param textDefinition Source for fully expanded interpolated strings.
   * @param workspace      Persistent user preferences settings.
   * @param caret          Location of the caret in the edited document,
   *                       which is used to synchronize the scrollbars.
   * @return A context that may be used for processing documents.
   */
  public static ProcessorContext create(
    final Path inputPath,
    final Path outputPath,
    final ExportFormat format,
    final HtmlPreview preview,
    final ObjectProperty<TextDefinition> textDefinition,
    final Workspace workspace,
    final Caret caret ) {
    return builder()
      .with( Mutator::setInputPath, inputPath )
      .with( Mutator::setOutputPath, outputPath )
      .with( Mutator::setExportFormat, format )
      .with( Mutator::setHtmlPreview, preview )
      .with( Mutator::setTextDefinition, textDefinition )
      .with( Mutator::setWorkspace, workspace )
      .with( Mutator::setCaret, caret )
      .build();
  }

  /**
   * @param inputPath Path to the document to process.
   * @param format    Indicate configuration options for export format.
   * @return A context that may be used for processing documents.
   */
  public static ProcessorContext create(
    final Path inputPath,
    final ExportFormat format ) {
    return builder()
      .with( Mutator::setInputPath, inputPath )
      .with( Mutator::setExportFormat, format )
      .build();
  }

  /**
   * @param inputPath  Path to the document to process.
   * @param outputPath Fully qualified filename to use when exporting.
   * @param format     Indicate configuration options for export format.
   * @return A context that may be used for processing documents.
   */
  public static ProcessorContext create(
    final Path inputPath, final Path outputPath, final ExportFormat format ) {
    return builder()
      .with( Mutator::setInputPath, inputPath )
      .with( Mutator::setOutputPath, outputPath )
      .with( Mutator::setExportFormat, format )
      .build();
  }

  /**
   * Creates a new context for use by the {@link ProcessorFactory} when
   * instantiating new {@link Processor} instances. Although all the
   * parameters are required, not all {@link Processor} instances will use
   * all parameters.
   */
  private ProcessorContext( final Mutator mutator ) {
    assert mutator != null;

    mMutator = mutator;
  }

  public boolean isExportFormat( final ExportFormat format ) {
    return mMutator.mExportFormat == format;
  }

  HtmlPreview getPreview() {
    return mMutator.mHtmlPreview;
  }

  /**
   * Returns the variable map of interpolated definitions.
   *
   * @return A map to help dereference variables.
   */
  Map<String, String> getResolvedMap() {
    return mMutator.mTextDefinition.get().getDefinitions();
  }

  /**
   * Fully qualified file name to use when exporting (e.g., document.pdf).
   *
   * @return Full path to a file name.
   */
  public Path getOutputPath() {
    return mMutator.mOutputPath;
  }

  public ExportFormat getExportFormat() {
    return mMutator.mExportFormat;
  }

  /**
   * Returns the current caret position in the document being edited and is
   * always up-to-date.
   *
   * @return Caret position in the document.
   */
  public Caret getCaret() {
    return mMutator.mCaret;
  }

  /**
   * Returns the directory that contains the file being edited. When
   * {@link Constants#DOCUMENT_DEFAULT} is created, the parent path is
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
    return mMutator.mInputPath;
  }

  FileType getFileType() {
    return lookup( getDocumentPath() );
  }

  public Workspace getWorkspace() {
    return mMutator.mWorkspace;
  }
}
