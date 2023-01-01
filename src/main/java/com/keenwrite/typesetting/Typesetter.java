/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.typesetting;

import com.keenwrite.util.GenericBuilder;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Responsible for typesetting a document using either a typesetter installed
 * on the computer ({@link HostTypesetter} or installed within a container
 * ({@link GuestTypesetter}).
 */
public class Typesetter {
  /**
   * Name of the executable program that can typeset documents.
   */
  static final String TYPESETTER_EXE = "mtxrun";

  public static GenericBuilder<Mutator, Typesetter> builder() {
    return GenericBuilder.of( Mutator::new, Typesetter::new );
  }

  public static final class Mutator {
    private Path mSourcePath;
    private Path mTargetPath;
    private Path mThemesPath;
    private Path mImagesPath;
    private boolean mAutoRemove;

    /**
     * @param inputPath The input document to typeset.
     */
    public void setSourcePath( final Path inputPath ) {
      mSourcePath = inputPath;
    }

    /**
     * @param outputPath Path to the finished typeset document to create.
     */
    public void setTargetPath( final Path outputPath ) {
      mTargetPath = outputPath;
    }

    /**
     * @param themePath Fully qualified path to the theme directory, which
     *                  ends with the selected theme name.
     */
    public void setThemesPath( final Path themePath ) {
      mThemesPath = themePath;
    }

    /**
     * @param imagePath Fully qualified path to the images directory.
     */
    public void setImagesPath( final Path imagePath ) {
      mImagesPath = imagePath;
    }

    /**
     * @param remove {@code true} to remove all temporary files after the
     *                  typesetter produces a PDF file.
     */
    public void setAutoRemove( final boolean remove ) {
      mAutoRemove = remove;
    }

    public Path getSourcePath() {
      return mSourcePath;
    }

    public Path getTargetPath() {
      return mTargetPath;
    }

    public Path getThemesPath() {
      return mThemesPath;
    }

    public Path getImagesPath() {
      return mImagesPath;
    }

    public boolean isAutoRemove() {
      return mAutoRemove;
    }
  }

  private final Mutator mMutator;

  /**
   * Creates a new {@link Typesetter} instance capable of configuring the
   * typesetter used to generate a typeset document.
   */
  Typesetter( final Mutator mutator ) {
    assert mutator != null;

    mMutator = mutator;
  }

  public void typeset() throws Exception {
    final Callable<Void> typesetter;

    if( HostTypesetter.isReady() ) {
      typesetter = new HostTypesetter( mMutator );
    }
    else if( GuestTypesetter.isReady() ) {
      typesetter = new GuestTypesetter( mMutator );
    }
    else {
      throw new TypesetterNotFoundException( TYPESETTER_EXE );
    }

    typesetter.call();
  }

  /**
   * Generates the command-line arguments used to invoke the typesetter.
   */
  List<String> options() {
    final var inputDoc = getSourcePath().toString();
    final var outputDoc = getTargetPath().getFileName();
    final var themePath = getThemesPath();
    final var imagePath = getImagesPath();

    final var args = new LinkedList<String>();

    args.add( "--autogenerate" );
    args.add( "--script" );
    args.add( "mtx-context" );
    args.add( "--arguments=imagedir=" + imagePath );
    args.add( "--batchmode" );
    args.add( "--nonstopmode" );
    args.add( "--purgeall" );
    args.add( "--path='" + themePath + "'" );
    args.add( "--environment='main'" );
    args.add( "--result='" + outputDoc + "'" );
    args.add( inputDoc );

    return args;
  }

  protected Path getSourcePath() {
    return mMutator.getSourcePath();
  }

  protected Path getTargetPath() {
    return mMutator.getTargetPath();
  }

  protected Path getThemesPath() {
    return mMutator.getThemesPath();
  }

  protected Path getImagesPath() {
    return mMutator.getImagesPath();
  }

  /**
   * Answers whether logs and other files should be deleted upon error. The
   * log files are useful for debugging.
   *
   * @return {@code true} to delete generated files.
   */
  public boolean autoclean() {
    return mMutator.isAutoRemove();
  }

  public static boolean canRun() {
    return hostCanRun() || guestCanRun();
  }

  private static boolean hostCanRun() {
    return HostTypesetter.isReady();
  }

  private static boolean guestCanRun() {
    return GuestTypesetter.isReady();
  }
}
