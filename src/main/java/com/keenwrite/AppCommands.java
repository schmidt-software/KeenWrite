package com.keenwrite;

import com.keenwrite.cmdline.Arguments;
import com.keenwrite.util.AlphanumComparator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import static com.keenwrite.Launcher.terminate;
import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.processors.ProcessorFactory.createProcessors;
import static com.keenwrite.util.FileWalker.walk;
import static java.lang.System.lineSeparator;
import static java.nio.file.Files.readString;
import static java.nio.file.Files.writeString;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.apache.commons.io.FilenameUtils.getExtension;

/**
 * Responsible for executing common commands. These commands are shared by
 * both the graphical and the command-line interfaces.
 */
public class AppCommands {
  private static final ExecutorService sExecutor = newFixedThreadPool( 1 );

  /**
   * Sci-fi genres, which are can be longer than other genres, typically fall
   * below 150,000 words at 6 chars per word. This reduces re-allocations of
   * memory when concatenating files together when exporting novels.
   */
  private static final int DOCUMENT_LENGTH = 150_000 * 6;

  private AppCommands() {
  }

  public static void run( final Arguments args ) {
    final var exitCode = new AtomicInteger();

    final var future = new CompletableFuture<Path>() {
      @Override
      public boolean complete( final Path path ) {
        return super.complete( path );
      }

      @Override
      public boolean completeExceptionally( final Throwable ex ) {
        clue( ex );
        exitCode.set( 1 );

        return super.completeExceptionally( ex );
      }
    };

    file_export( args, future );
    sExecutor.shutdown();
    future.join();
    terminate( exitCode.get() );
  }

  /**
   * Converts one or more files into the given file format. If {@code dir}
   * is set to true, this will first append all files in the same directory
   * as the actively edited file.
   *
   * @param future Indicates whether the export succeeded or failed.
   */
  private static void file_export(
    final Arguments args, final CompletableFuture<Path> future ) {
    assert args != null;

    final Callable<Path> callableTask = () -> {
      try {
        final var context = args.createProcessorContext();
        final var concat = context.getConcatenate();
        final var inputPath = context.getInputPath();
        final var outputPath = context.getOutputPath();
        final var chain = createProcessors( context );
        final var inputDoc = read( inputPath, concat );
        final var outputDoc = chain.apply( inputDoc );

        // Processors can export binary files. In such cases, processors will
        // return null to prevent further processing.
        final var result =
          outputDoc == null ? null : writeString( outputPath, outputDoc );

        future.complete( outputPath );
        return result;
      } catch( final Throwable ex ) {
        future.completeExceptionally( ex );
        return null;
      }
    };

    // Prevent the application from blocking while the processor executes.
    sExecutor.submit( callableTask );
  }

  /**
   * @param concat {@code true} means to export all files in the active file
   *               editor's directory; {@code false} means to export only the
   *               actively edited file.
   *
  private void file_export_pdf( final Path theme, final boolean concat ) {
  if( Typesetter.canRun() ) {
  // If the typesetter is installed, allow the user to select a theme. If
  // the themes aren't installed, a status message will appear.
  if( ThemePicker.choose( themes, theme ) ) {
  file_export( APPLICATION_PDF, concat );
  }
  }
  else {
  fireExportFailedEvent();
  }
  }

  public void file_export_pdf() {
  file_export_pdf( false );
  }

  public void file_export_pdf_dir() {
  file_export_pdf( true );
  }

  public void file_export_html_svg() {
  file_export( HTML_TEX_SVG );
  }

  public void file_export_html_tex() {
  file_export( HTML_TEX_DELIMITED );
  }

  public void file_export_xhtml_tex() {
  file_export( XHTML_TEX );
  }

  public void file_export_markdown() {
  file_export( MARKDOWN_PLAIN );
  }
   */

  /**
   * Concatenates all the files in the same directory as the given file into
   * a string. The extension is determined by the given file name pattern; the
   * order files are concatenated is based on their numeric sort order (this
   * avoids lexicographic sorting).
   * <p>
   * If the parent path to the file being edited in the text editor cannot
   * be found then this will return the editor's text, without iterating through
   * the parent directory. (Should never happen, but who knows?)
   * </p>
   * <p>
   * New lines are automatically appended to separate each file.
   * </p>
   *
   * @param inputPath The path to the source file to read.
   * @param concat    {@code true} to concatenate all files with the same
   *                  extension as the source path.
   * @return All files in the same directory as the file being edited
   * concatenated into a single string.
   */
  private static String read( final Path inputPath, final boolean concat )
    throws IOException {
    final var parent = inputPath.getParent();
    final var filename = inputPath.getFileName().toString();
    final var extension = getExtension( filename );

    // Short-circuit because: only one file was requested; there is no parent
    // directory to scan for files; or there's no extension for globbing.
    if( !concat || parent == null || extension.isBlank() ) {
      return readString( inputPath );
    }

    final var glob = "**/*." + extension;
    final var files = new ArrayList<Path>();
    walk( parent, glob, files::add );
    files.sort( new AlphanumComparator<>() );

    final var text = new StringBuilder( DOCUMENT_LENGTH );
    final var eol = lineSeparator();

    for( final var file : files ) {
      text.append( readString( file ) );
      text.append( eol );
    }

    return text.toString();
  }
}
