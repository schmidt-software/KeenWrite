/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.processors;

import com.keenwrite.ExportFormat;
import com.keenwrite.collections.InterpolatingMap;
import com.keenwrite.constants.Constants;
import com.keenwrite.editors.common.Caret;
import com.keenwrite.io.FileType;
import com.keenwrite.io.MediaType;
import com.keenwrite.io.MediaTypeExtension;
import com.keenwrite.sigils.PropertyKeyOperator;
import com.keenwrite.sigils.SigilKeyOperator;
import com.keenwrite.util.GenericBuilder;
import org.renjin.repackaged.guava.base.Splitter;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import static com.keenwrite.Bootstrap.USER_CACHE_DIR;
import static com.keenwrite.Bootstrap.USER_DATA_DIR;
import static com.keenwrite.constants.Constants.*;
import static com.keenwrite.io.FileType.UNKNOWN;
import static com.keenwrite.io.MediaType.TEXT_PROPERTIES;
import static com.keenwrite.io.SysFile.toFile;
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

      if( predicate.test( toFile( path ) ) ) {
        // Remove the EXTENSIONS_PREFIX to get the file name extension mapped
        // to a standard name (as defined in the settings.properties file).
        final String suffix = key.replace( prefix + '.', "" );
        fileType = FileType.from( suffix );
        found = true;
      }
    }

    return fileType;
  }

  public boolean isExportFormat( final ExportFormat exportFormat ) {
    return mMutator.mExportFormat == exportFormat;
  }

  /**
   * Responsible for populating the instance variables required by the
   * context.
   */
  public static class Mutator {
    private Path mSourcePath;
    private Path mTargetPath;
    private ExportFormat mExportFormat;
    private Supplier<Boolean> mConcatenate = () -> true;
    private Supplier<String> mChapters = () -> "";

    private Supplier<Path> mThemeDir = USER_DIRECTORY::toPath;
    private Supplier<Locale> mLocale = () -> Locale.ENGLISH;

    private Supplier<Map<String, String>> mDefinitions = HashMap::new;
    private Supplier<Map<String, String>> mMetadata = HashMap::new;
    private Supplier<Caret> mCaret = () -> Caret.builder().build();

    private Supplier<Path> mFontDir = () -> getFontDirectory().toPath();

    private Supplier<Path> mImageDir = USER_DIRECTORY::toPath;
    private Supplier<String> mImageServer = () -> DIAGRAM_SERVER_NAME;
    private Supplier<String> mImageOrder = () -> PERSIST_IMAGES_DEFAULT;

    private Supplier<Path> mCacheDir = USER_CACHE_DIR::toPath;

    private Supplier<String> mSigilBegan = () -> DEF_DELIM_BEGAN_DEFAULT;
    private Supplier<String> mSigilEnded = () -> DEF_DELIM_ENDED_DEFAULT;

    private Supplier<Path> mRWorkingDir = USER_DIRECTORY::toPath;
    private Supplier<String> mRScript = () -> "";

    private Supplier<Boolean> mCurlQuotes = () -> true;
    private Supplier<Boolean> mAutoRemove = () -> true;

    public void setSourcePath( final Path sourcePath ) {
      assert sourcePath != null;
      mSourcePath = sourcePath;
    }

    public void setTargetPath( final Path outputPath ) {
      assert outputPath != null;
      mTargetPath = outputPath;
    }

    public void setThemeDir( final Supplier<Path> themeDir ) {
      assert themeDir != null;
      mThemeDir = themeDir;
    }

    public void setCacheDir( final Supplier<File> cacheDir ) {
      assert cacheDir != null;

      mCacheDir = () -> {
        final var dir = cacheDir.get();

        return (dir == null ? toFile( USER_DATA_DIR ) : dir).toPath();
      };
    }

    public void setImageDir( final Supplier<File> imageDir ) {
      assert imageDir != null;

      mImageDir = () -> {
        final var dir = imageDir.get();

        return (dir == null ? USER_DIRECTORY : dir).toPath();
      };
    }

    public void setImageOrder( final Supplier<String> imageOrder ) {
      assert imageOrder != null;
      mImageOrder = imageOrder;
    }

    public void setImageServer( final Supplier<String> imageServer ) {
      assert imageServer != null;
      mImageServer = imageServer;
    }

    public void setFontDir( final Supplier<File> fontDir ) {
      assert fontDir != null;

      mFontDir = () -> {
        final var dir = fontDir.get();

        return (dir == null ? USER_DIRECTORY : dir).toPath();
      };
    }

    public void setExportFormat( final ExportFormat exportFormat ) {
      assert exportFormat != null;
      mExportFormat = exportFormat;
    }

    public void setConcatenate( final Supplier<Boolean> concatenate ) {
      mConcatenate = concatenate;
    }

    public void setChapters( final Supplier<String> chapters ) {
      mChapters = chapters;
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
     * Sets metadata to use in the document header. These are made available
     * to the typesetting engine as {@code \documentvariable} values.
     *
     * @param metadata The key/value pairs to publish as document metadata.
     */
    public void setMetadata( final Supplier<Map<String, String>> metadata ) {
      assert metadata != null;
      mMetadata = metadata.get() == null ? HashMap::new : metadata;
    }

    /**
     * Sets document variables to use when building the document. These
     * variables will override existing key/value pairs, or be added as
     * new key/value pairs if not already defined. This allows users to
     * inject variables into the document from the command-line, allowing
     * for dynamic assignment of in-text values when building documents.
     *
     * @param overrides The key/value pairs to add (or override) as variables.
     */
    public void setOverrides( final Supplier<Map<String, String>> overrides ) {
      assert overrides != null;
      assert mDefinitions != null;
      assert mDefinitions.get() != null;

      final var map = overrides.get();

      if( map != null ) {
        mDefinitions.get().putAll( map );
      }
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

    public void setSigilBegan( final Supplier<String> sigilBegan ) {
      assert sigilBegan != null;
      mSigilBegan = sigilBegan;
    }

    public void setSigilEnded( final Supplier<String> sigilEnded ) {
      assert sigilEnded != null;
      mSigilEnded = sigilEnded;
    }

    public void setRWorkingDir( final Supplier<Path> rWorkingDir ) {
      assert rWorkingDir != null;

      mRWorkingDir = rWorkingDir;
    }

    public void setRScript( final Supplier<String> rScript ) {
      assert rScript != null;
      mRScript = rScript;
    }

    public void setCurlQuotes( final Supplier<Boolean> curlQuotes ) {
      assert curlQuotes != null;
      mCurlQuotes = curlQuotes;
    }

    public void setAutoRemove( final Supplier<Boolean> autoRemove ) {
      assert autoRemove != null;
      mAutoRemove = autoRemove;
    }

    private boolean isExportFormat( final ExportFormat format ) {
      return mExportFormat == format;
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

  public Path getSourcePath() {
    return mMutator.mSourcePath;
  }

  /**
   * Answers what type of input document is to be processed.
   *
   * @return The input document's {@link MediaType}.
   */
  public MediaType getSourceType() {
    return MediaTypeExtension.fromPath( mMutator.mSourcePath );
  }

  /**
   * Fully qualified file name to use when exporting (e.g., document.pdf).
   *
   * @return Full path to a file name.
   */
  public Path getTargetPath() {
    return mMutator.mTargetPath;
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
    return new InterpolatingMap(
      createDefinitionKeyOperator(), getDefinitions()
    ).interpolate();
  }

  public Map<String, String> getMetadata() {
    return mMutator.mMetadata.get();
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
    final var path = getSourcePath().toAbsolutePath().getParent();
    return path == null ? DEFAULT_DIRECTORY : path;
  }

  FileType getSourceFileType() {
    return lookup( getSourcePath() );
  }

  public Path getThemeDir() {
    return mMutator.mThemeDir.get();
  }

  public Path getImageDir() {
    return mMutator.mImageDir.get();
  }

  public Path getCacheDir() {
    return mMutator.mCacheDir.get();
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

  public Path getFontDir() {
    return mMutator.mFontDir.get();
  }

  public boolean getAutoRemove() {
    return mMutator.mAutoRemove.get();
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

  /**
   * Answers whether to process a single text file or all text files in
   * the same directory as a single text file. See {@link #getSourcePath()}
   * for the file to process (or all files in its directory).
   *
   * @return {@code true} means to process all text files, {@code false}
   * means to process a single file.
   */
  public boolean getConcatenate() {
    return mMutator.mConcatenate.get();
  }

  public String getChapters() {
    return mMutator.mChapters.get();
  }

  public SigilKeyOperator createKeyOperator() {
    return createKeyOperator( getSourcePath() );
  }

  /**
   * Returns the sigil operator for the given {@link Path}.
   *
   * @param path The type of file being edited, from its extension.
   */
  private SigilKeyOperator createKeyOperator( final Path path ) {
    assert path != null;

    return MediaType.fromFilename( path ) == TEXT_PROPERTIES
      ? createPropertyKeyOperator()
      : createDefinitionKeyOperator();
  }

  private SigilKeyOperator createPropertyKeyOperator() {
    return new PropertyKeyOperator();
  }

  private SigilKeyOperator createDefinitionKeyOperator() {
    final var began = mMutator.mSigilBegan.get();
    final var ended = mMutator.mSigilEnded.get();

    return new SigilKeyOperator( began, ended );
  }
}
