package com.keenwrite.typesetting;

import java.io.*;
import java.util.Map;
import java.util.Scanner;
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
class PaginationListener extends Thread implements Runnable {
  private static final Pattern DIGITS = Pattern.compile( "\\D+" );

  private final InputStream mInputStream;

  private final Map<String, String> mCache;

  public PaginationListener(
    final InputStream in, final Map<String, String> cache ) {
    assert in != null;
    assert cache != null;

    mInputStream = in;
    mCache = cache;
  }

  @Override
  public void run() {
    try( final var reader = createReader( mInputStream ) ) {
      int pageCount = 1;
      int passCount = 1;
      int pageTotal = 0;
      String line;

      while( (line = reader.readLine()) != null ) {
        mCache.put( line, "" );

        if( line.startsWith( "pages" ) ) {
          // The bottleneck will be the typesetting engine writing to stdout,
          // not the parsing of stdout.
          final var scanner = new Scanner( line ).useDelimiter( DIGITS );
          final var digits = scanner.next();
          final var page = Integer.parseInt( digits );

          // If the page number is less than the previous page count, it
          // means that the typesetting engine has started another pass.
          if( page < pageCount ) {
            passCount++;
            pageTotal = pageCount;
          }

          pageCount = page;

          // Inform the user of pages being typeset.
          clue( "Main.status.typeset.page",
                pageCount, pageTotal < 1 ? "?" : pageTotal, passCount
          );
        }
      }
    } catch( final IOException ex ) {
      clue( ex );
      throw new UncheckedIOException( ex );
    }
  }

  private BufferedReader createReader( final InputStream inputStream ) {
    return new BufferedReader( new InputStreamReader( inputStream ) );
  }
}
