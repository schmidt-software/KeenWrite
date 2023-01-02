/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.typesetting;

import java.util.Scanner;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static com.keenwrite.events.StatusEvent.clue;

/**
 * Responsible for parsing the output from the typesetting engine and
 * updating the status bar to provide assurance that typesetting is
 * executing.
 *
 * <p>
 * Example lines written to standard output:
 * </p>
 * <pre>{@code
 * pages           > flushing realpage 15, userpage 15, subpage 15
 * pages           > flushing realpage 16, userpage 16, subpage 16
 * pages           > flushing realpage 1, userpage 1, subpage 1
 * pages           > flushing realpage 2, userpage 2, subpage 2
 * }</pre>
 * <p>
 * The lines are parsed; the first number is displayed as a status bar
 * message.
 * </p>
 */
class PaginationListener implements Consumer<String> {
  private static final Pattern DIGITS = Pattern.compile( "\\D+" );

  private int mPageCount = 1;
  private int mPassCount = 1;
  private int mPageTotal = 0;

  public PaginationListener() { }

  @Override
  public void accept( final String line ) {
    if( line.startsWith( "pages" ) ) {
      final var scanner = new Scanner( line ).useDelimiter( DIGITS );
      final var digits = scanner.next();
      final var page = Integer.parseInt( digits );

      // If the page number is less than the previous page count, it
      // means that the typesetting engine has started another pass.
      if( page < mPageCount ) {
        mPassCount++;
        mPageTotal = mPageCount;
      }

      mPageCount = page;

      // Inform the user of pages being typeset.
      clue( "Main.status.typeset.page",
            mPageCount, mPageTotal < 1 ? "?" : mPageTotal, mPassCount
      );
    }
  }
}
