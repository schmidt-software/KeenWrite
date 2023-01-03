/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.typesetting;

import com.keenwrite.util.GenericBuilder;
import com.keenwrite.util.Time;

import java.nio.file.Path;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.util.Time.toElapsedTime;
import static java.lang.System.currentTimeMillis;
import static java.time.Duration.ofMillis;

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
    private Path mFontsPath;
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
     * @param imagePath Fully qualified path to the "images" directory.
     */
    public void setImagesPath( final Path imagePath ) {
      mImagesPath = imagePath;
    }

    /**
     * @param fontsPath Fully qualified path to the "fonts" directory.
     */
    public void setFontsPath( final Path fontsPath ) {
      mFontsPath = fontsPath;
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

    public Path getFontsPath() {
      return mFontsPath;
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
    final Callable<Boolean> typesetter;

    if( HostTypesetter.isReady() ) {
      typesetter = new HostTypesetter( mMutator );
    }
    else if( GuestTypesetter.isReady() ) {
      typesetter = new GuestTypesetter( mMutator );
    }
    else {
      throw new TypesetterNotFoundException( TYPESETTER_EXE );
    }

    final var outputPath = getTargetPath();
    final var prefix = "Main.status.typeset";

    clue( prefix + ".began", outputPath );

    final var time = currentTimeMillis();
    final var success = typesetter.call();
    final var suffix = success ? ".success" : ".failure";

    clue( prefix + ".ended" + suffix, outputPath, since( time ) );
  }

  /**
   * Generates the command-line arguments used to invoke the typesetter.
   */
  @SuppressWarnings( "SpellCheckingInspection" )
  List<String> options() {
    final var args = commonOptions();

    final var sourcePath = getSourcePath().toString();
    final var targetPath = getTargetPath().getFileName();
    final var themesPath = getThemesPath();
    final var imagesPath = getImagesPath();

    args.add( "--arguments=imagedir=" + imagesPath );
    args.add( "--path='" + themesPath + "'" );
    args.add( "--result='" + targetPath + "'" );
    args.add( sourcePath );

    return args;
  }

  @SuppressWarnings( "SpellCheckingInspection" )
  List<String> commonOptions() {
    final var args = new LinkedList<String>();

    args.add( "--autogenerate" );
    args.add( "--script" );
    args.add( "mtx-context" );
    args.add( "--batchmode" );
    args.add( "--nonstopmode" );
    args.add( "--purgeall" );
    args.add( "--environment='main'" );

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

  protected Path getFontsPath() {
    return mMutator.getFontsPath();
  }

  /**
   * Answers whether logs and other files should be deleted upon error. The
   * log files are useful for debugging.
   *
   * @return {@code true} to delete generated files.
   */
  public boolean autoRemove() {
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

  /**
   * Calculates the time that has elapsed from the current time to the
   * given moment in time.
   *
   * @param start The starting time, which should be before the current time.
   * @return A human-readable formatted time.
   * @see Time#toElapsedTime(Duration)
   */
  private static String since( final long start ) {
    return toElapsedTime( ofMillis( currentTimeMillis() - start ) );
  }
}
