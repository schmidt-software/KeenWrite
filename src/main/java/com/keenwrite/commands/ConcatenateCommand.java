/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.commands;

import com.keenwrite.util.AlphanumComparator;
import com.keenwrite.util.RangeValidator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.util.FileWalker.walk;
import static java.lang.System.lineSeparator;
import static java.nio.file.Files.readString;

/**
 * Responsible for concatenating files according to user-defined chapter ranges.
 */
public class ConcatenateCommand implements Callable<String> {
  /**
   * Sci-fi genres, which are can be longer than other genres, typically fall
   * below 150,000 words at 6 chars per word. This reduces re-allocations of
   * memory when concatenating files together when exporting novels.
   */
  private static final int DOCUMENT_LENGTH = 150_000 * 6;

  private final Path mParent;
  private final String mExtension;
  private final String mRange;

  public ConcatenateCommand(
    final Path parent,
    final String extension,
    final String range ) {
    assert parent != null;
    assert extension != null;
    assert range != null;

    mParent = parent;
    mExtension = extension;
    mRange = range;
  }

  public String call() throws IOException {
    final var glob = "**/*." + mExtension;
    final var files = new ArrayList<Path>();
    final var text = new StringBuilder( DOCUMENT_LENGTH );
    final var chapter = new AtomicInteger();
    final var eol = lineSeparator();

    final var validator = new RangeValidator( mRange );

    walk( mParent, glob, files::add );
    files.sort( new AlphanumComparator<>() );
    files.forEach( file -> {
      try {
        if( validator.test( chapter.incrementAndGet() ) ) {
          clue( "Main.status.export.concat", file );

          text.append( readString( file ) )
              .append( eol );
        }
      } catch( final IOException ex ) {
        clue( "Main.status.export.concat.io", file );
      }
    } );

    return text.toString();
  }
}
