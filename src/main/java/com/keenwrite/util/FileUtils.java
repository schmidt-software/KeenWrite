package com.keenwrite.util;

import com.keenwrite.io.MediaType;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static com.keenwrite.Bootstrap.APP_TITLE_LOWERCASE;
import static com.keenwrite.io.MediaTypeExtension.valueFrom;
import static java.io.File.createTempFile;
import static java.io.File.pathSeparator;
import static java.lang.System.getenv;
import static java.nio.file.Files.isExecutable;
import static java.util.regex.Pattern.quote;

/**
 * Place for common file-related functionality.
 */
public class FileUtils {
  /**
   * For finding executable programs.
   */
  private static final String[] EXTENSIONS = new String[]
    {"", ".com", ".exe", ".bat", ".cmd"};

  private FileUtils() {}

  /**
   * Creates a temporary file for the given {@link MediaType}, which will be
   * deleted when the application exits.
   *
   * @param media The type of file to create (i.e., its extension).
   * @return The fully qualified path to a file.
   * @throws IOException Could not create the temporary file.
   */
  public static Path createTemporaryFile( final MediaType media )
    throws IOException {
    final var file = createTempFile(
      APP_TITLE_LOWERCASE, '.' + valueFrom( media ).getExtension() );
    file.deleteOnExit();
    return file.toPath();
  }

  /**
   * Given the name of an executable (without an extension) file, this will
   * attempt to determine whether the executable is found in the PATH
   * environment variable.
   *
   * @param exe The executable file name to find.
   * @return {@code true} when the given file name references an executable
   * file located in the PATH environment variable.
   */
  public static boolean canExecute( final String exe ) {
    final var paths = getenv( "PATH" ).split( quote( pathSeparator ) );
    return Stream.of( paths ).map( Paths::get ).anyMatch(
      path -> {
        final var p = path.resolve( exe );

        for( final var extension : EXTENSIONS ) {
          if( isExecutable( Path.of( p.toString() + extension ) ) ) {
            return true;
          }
        }

        return false;
      }
    );
  }
}
