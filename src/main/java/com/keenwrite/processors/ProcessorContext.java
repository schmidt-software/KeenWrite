/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors;

import com.keenwrite.ExportFormat;
import com.keenwrite.collections.InterpolatingMap;
import com.keenwrite.constants.Constants;
import com.keenwrite.editors.common.Caret;
import com.keenwrite.io.FileType;
import com.keenwrite.preferences.Workspace;
import com.keenwrite.sigils.SigilKeyOperator;
import com.keenwrite.util.GenericBuilder;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import static com.keenwrite.constants.Constants.*;
import static com.keenwrite.io.FileType.UNKNOWN;
import static com.keenwrite.predicates.PredicateFactory.createFileTypePredicate;

/**
 * Provides a context for configuring a chain of {@link Processor} instances.
 */
public final class ProcessorContext {

  private final Mutator mMutator;

  /**
   * Determines the file type from the path extension. This should only be
   * called when it is known that the file type won't be a definition file
   * (e.g., YAML or other definition source), but rather an editable file
   * (e.g., Markdown, R Markdown, etc.).
   *
   * @param path The path with a file name extension.
   * @return The FileType for the given path.
   */
  private static FileType lookup( final Path path ) {
    assert path != null;

    final var prefix = GLOB_PREFIX_FILE;
    final var keys = sSettings.getKeys( prefix );

    var found = false;
    var fileType = UNKNOWN;

    while( keys.hasNext() && !found ) {
      final var key = keys.next();
      final var patterns = sSettings.getStringSettingList( key );
      final var predicate = createFileTypePredicate( patterns );

      if( predicate.test( path.toFile() ) ) {
        // Remove the EXTENSIONS_PREFIX to get the file name extension mapped
        // to a standard name (as defined in the settings.properties file).
        final String suffix = key.replace( prefix + '.', "" );
        fileType = FileType.from( suffix );
        found = true;
      }
    }

    return fileType;
  }

  /**
   * Responsible for populating the instance variables required by the
   * context.
   */
  public static class Mutator {
    private Path mInputPath;
    private Path mOutputPath;
    private ExportFormat mExportFormat;
    private Supplier<Map<String, String>> mDefinitions;
    private Supplier<Caret> mCaret;
    private Workspace mWorkspace;

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

    /**
     * Sets the list of fully interpolated key-value pairs to use when
     * substituting variable names back into the document as variable values.
     * This uses a {@link Callable} reference so that GUI and command-line
     * usage can insert their respective behaviours. That is, this method
     * prevents coupling the GUI to the CLI.
     *
     * @param definitions Defines how to retrieve the definitions.
     */
    public void setDefinitions(
      final Supplier<Map<String, String>> definitions ) {
      mDefinitions = definitions;
    }

    /**
     * Sets the source for deriving the {@link Caret}. Typically, this is
     * the text editor that has focus.
     *
     * @param caret The source for the currently active caret.
     */
    public void setCaret( final Supplier<Caret> caret ) {
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
    return GenericBuilder.of( Mutator::new, ProcessorContext::new );
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
   * Creates a new context for use by the {@link ProcessorFactory} when
   * instantiating new {@link Processor} instances. Although all the
   * parameters are required, not all {@link Processor} instances will use
   * all parameters.
   */
  private ProcessorContext( final Mutator mutator ) {
    assert mutator != null;

    mMutator = mutator;
  }

  /**
   * Returns the variable map of definitions, without interpolation.
   *
   * @return A map to help dereference variables.
   */
  public Map<String, String> getDefinitions() {
    return mMutator.mDefinitions.get();
  }

  /**
   * Returns the variable map of definitions, with interpolation.
   *
   * @return A map to help dereference variables.
   */
  public InterpolatingMap getInterpolatedDefinitions() {
    final var map = new InterpolatingMap(
      createDefinitionSigilOperator(), getDefinitions()
    );

    map.interpolate();

    return map;
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
  public Supplier<Caret> getCaret() {
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
    final var path = getInputPath().toAbsolutePath().getParent();
    return path == null ? DEFAULT_DIRECTORY : path;
  }

  public Path getInputPath() {
    return mMutator.mInputPath;
  }

  FileType getFileType() {
    return lookup( getInputPath() );
  }

  public Workspace getWorkspace() {
    return mMutator.mWorkspace;
  }

  public SigilKeyOperator createSigilOperator() {
    return getWorkspace().createSigilOperator( getInputPath() );
  }

  public SigilKeyOperator createDefinitionSigilOperator() {
    return getWorkspace().createDefinitionKeyOperator();
  }
}
