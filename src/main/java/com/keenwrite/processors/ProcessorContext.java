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
import org.renjin.repackaged.guava.base.Splitter;

import java.io.File;
import java.nio.file.Path;
import java.util.Locale;
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
    private Path mThemePath;

    private Supplier<Locale> mLocale;

    private Supplier<Map<String, String>> mDefinitions;
    private Supplier<Caret> mCaret = () -> Caret.builder().build();
    private boolean mConcatenate;

    private Supplier<Path> mImageDir;
    private Supplier<String> mImageServer;
    private Supplier<String> mImageOrder;

    private Supplier<String> mSigilBegan;
    private Supplier<String> mSigilEnded;

    private Supplier<Path> mRWorkingDir;
    private Supplier<String> mRScript = () -> "";

    private Supplier<Boolean> mCurlQuotes = () -> true;
    private Supplier<Boolean> mAutoClean = () -> true;

    public void setInputPath( final Path inputPath ) {
      assert inputPath != null;
      mInputPath = inputPath;
    }

    public void setOutputPath( final Path outputPath ) {
      assert outputPath != null;
      mOutputPath = outputPath;
    }

    public void setOutputPath( final File outputPath ) {
      assert outputPath != null;
      setOutputPath( outputPath.toPath() );
    }

    public void setExportFormat( final ExportFormat exportFormat ) {
      assert exportFormat != null;
      mExportFormat = exportFormat;
    }

    public void setLocale( final Supplier<Locale> locale ) {
      assert locale != null;
      mLocale = locale;
    }

    /**
     * Sets the list of fully interpolated key-value pairs to use when
     * substituting variable names back into the document as variable values.
     * This uses a {@link Callable} reference so that GUI and command-line
     * usage can insert their respective behaviours. That is, this method
     * prevents coupling the GUI to the CLI.
     *
     * @param supplier Defines how to retrieve the definitions.
     */
    public void setDefinitions( final Supplier<Map<String, String>> supplier ) {
      assert supplier != null;
      mDefinitions = supplier;
    }

    /**
     * Sets the source for deriving the {@link Caret}. Typically, this is
     * the text editor that has focus.
     *
     * @param caret The source for the currently active caret.
     */
    public void setCaret( final Supplier<Caret> caret ) {
      assert caret != null;
      mCaret = caret;
    }

    public void setThemePath( final Path themePath ) {
      assert themePath != null;
      mThemePath = themePath;
    }

    public void setConcatenate( final boolean concatenate ) {
      mConcatenate = concatenate;
    }

    public void setImageDir( final Supplier<File> imageDir ) {
      assert imageDir != null;
      mImageDir = () -> imageDir.get().toPath();
    }

    public void setImageOrder( final Supplier<String> imageOrder ) {
      assert imageOrder != null;
      mImageOrder = imageOrder;
    }

    public void setImageServer( final Supplier<String> imageServer ) {
      assert imageServer != null;
      mImageServer = imageServer;
    }

    public void setSigilBegan( final Supplier<String> sigilBegan ) {
      assert sigilBegan != null;
      mSigilBegan = sigilBegan;
    }

    public void setSigilEnded( final Supplier<String> sigilEnded ) {
      assert sigilEnded != null;
      mSigilEnded = sigilEnded;
    }

    public void setRWorkingDir( final Supplier<File> rWorkingDir ) {
      assert rWorkingDir != null;
      mRWorkingDir = () -> rWorkingDir.get().toPath();
    }

    public void setRScript( final Supplier<String> rScript ) {
      assert rScript != null;
      mRScript = rScript;
    }

    public void setCurlQuotes( final Supplier<Boolean> curlQuotes ) {
      assert curlQuotes != null;
      mCurlQuotes = curlQuotes;
    }

    public void setAutoClean( final Supplier<Boolean> autoClean ) {
      assert autoClean != null;
      mAutoClean = autoClean;
    }

    private Workspace mWorkspace;

    public void setWorkspace( final Workspace workspace ) {
      assert workspace != null;
      mWorkspace = workspace;
    }
  }

  public static GenericBuilder<Mutator, ProcessorContext> builder() {
    return GenericBuilder.of( Mutator::new, ProcessorContext::new );
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

  public Path getInputPath() {
    return mMutator.mInputPath;
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

  public Locale getLocale() {
    return mMutator.mLocale.get();
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
      getWorkspace().createDefinitionKeyOperator(), getDefinitions()
    );

    map.interpolate();

    return map;
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

  FileType getFileType() {
    return lookup( getInputPath() );
  }

  public Path getImageDir() {
    return mMutator.mImageDir.get();
  }

  public Iterable<String> getImageOrder() {
    assert mMutator.mImageOrder != null;

    final var order = mMutator.mImageOrder.get();
    final var token = order.contains( "," ) ? ',' : ' ';

    return Splitter.on( token ).split( token + order );
  }

  public String getImageServer() {
    return mMutator.mImageServer.get();
  }

  public Path getRWorkingDir() {
    return mMutator.mRWorkingDir.get();
  }

  public String getRScript() {
    return mMutator.mRScript.get();
  }

  public boolean getCurlQuotes() {
    return mMutator.mCurlQuotes.get();
  }

  public boolean getAutoClean() {
    return mMutator.mAutoClean.get();
  }

  public Workspace getWorkspace() {
    return mMutator.mWorkspace;
  }

  /**
   * Answers whether to process a single text file or all text files in
   * the same directory as a single text file. See {@link #getInputPath()}
   * for the file to process (or all files in its directory).
   *
   * @return {@code true} means to process all text files, {@code false}
   * means to process a single file.
   */
  public boolean getConcatenate() {
    return mMutator.mConcatenate;
  }

  public SigilKeyOperator createSigilOperator() {
    return getWorkspace().createSigilOperator( getInputPath() );
  }
}
