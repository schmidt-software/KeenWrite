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
    private Path mInputPath;
    private Path mOutputPath;
    private Path mThemePath;
    private boolean mAutoClean;

    /**
     * @param inputPath The input document to typeset.
     */
    public void setInputPath( final Path inputPath ) {
      mInputPath = inputPath;
    }

    /**
     * @param outputPath Path to the finished typeset document to create.
     */
    public void setOutputPath( final Path outputPath ) {
      mOutputPath = outputPath;
    }

    /**
     * @param themePath Fully qualified path to the theme directory, which
     *                  ends with the selected theme name.
     */
    public void setThemePath( final Path themePath ) {
      mThemePath = themePath;
    }

    /**
     * @param autoClean {@code true} to remove all temporary files after the
     *                  typesetter produces a PDF file.
     */
    public void setAutoClean( final boolean autoClean ) {
      mAutoClean = autoClean;
    }

    public Path getInputPath() {
      return mInputPath;
    }

    public Path getOutputPath() {
      return mOutputPath;
    }

    public Path getThemePath() {
      return mThemePath;
    }

    public boolean isAutoClean() {
      return mAutoClean;
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
    final var themePath = getThemePath();
    final var inputDoc = getInputPath().toString();
    final var outputDoc = getOutputPath().getFileName();

    final var args = new LinkedList<String>();

    args.add( "--autogenerate" );
    args.add( "--script" );
    args.add( "mtx-context" );
    args.add( "--batchmode" );
    args.add( "--nonstopmode" );
    args.add( "--purgeall" );
    args.add( "--path='" + themePath + "'" );
    args.add( "--environment='main'" );
    args.add( "--result='" + outputDoc + "'" );
    args.add( inputDoc );

    return args;
  }

  protected Path getInputPath() {
    return mMutator.getInputPath();
  }

  protected Path getOutputPath() {
    return mMutator.getOutputPath();
  }

  private Path getThemePath() {
    return mMutator.getThemePath();
  }

  /**
   * Answers whether logs and other files should be deleted upon error. The
   * log files are useful for debugging.
   *
   * @return {@code true} to delete generated files.
   */
  public boolean autoclean() {
    return mMutator.isAutoClean();
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
