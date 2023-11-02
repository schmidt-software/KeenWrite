/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite;

import com.keenwrite.cmdline.Arguments;
import com.keenwrite.commands.ConcatenateCommand;
import com.keenwrite.io.SysFile;
import com.keenwrite.processors.Processor;
import com.keenwrite.processors.ProcessorContext;
import com.keenwrite.processors.RBootstrapProcessor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static com.keenwrite.Launcher.terminate;
import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.io.MediaType.TEXT_R_MARKDOWN;
import static com.keenwrite.processors.ProcessorFactory.createProcessors;
import static java.nio.charset.StandardCharsets.UTF_8;
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
   * @return The path to the exported file as a {@link Future}.
   */
  @SuppressWarnings( "UnusedReturnValue" )
  private static Future<Path> file_export(
    final Arguments args, final CompletableFuture<Path> future ) {
    assert args != null;
    assert future != null;

    final Callable<Path> callableTask = () -> {
      try {
        final var context = args.createProcessorContext();
        final var outputPath = context.getTargetPath();
        final var chain = createProcessors( context );
        final var processor = createBootstrapProcessor( chain, context );
        final var inputDoc = read( context );
        final var outputDoc = processor.apply( inputDoc );

        // Processors can export binary files. In such cases, processors will
        // return null to prevent further processing.
        final var result = outputDoc == null
          ? null
          : writeString( outputPath, outputDoc, UTF_8 );

        future.complete( outputPath );
        return result;
      } catch( final Throwable ex ) {
        future.completeExceptionally( ex );
        return null;
      }
    };

    // Prevent the application from blocking while the processor executes.
    return sExecutor.submit( callableTask );
  }

  private static Processor<String> createBootstrapProcessor(
    final Processor<String> chain, final ProcessorContext context ) {

    return context.getSourceType() == TEXT_R_MARKDOWN
      ? new RBootstrapProcessor( chain, context )
      : chain;
  }

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
   * @param context The {@link ProcessorContext} containing input path,
   *                and other command-line parameters.
   * @return All files in the same directory as the file being edited
   * concatenated into a single string.
   */
  private static String read( final ProcessorContext context )
    throws IOException {
    final var concat = context.getConcatenate();
    final var inputPath = context.getSourcePath();
    final var parent = inputPath.getParent();
    final var filename = SysFile.getFileName( inputPath );
    final var extension = getExtension( filename );

    // Short-circuit because: only one file was requested; there is no parent
    // directory to scan for files; or there's no extension for globbing.
    if( !concat || parent == null || extension.isBlank() ) {
      return readString( inputPath, UTF_8 );
    }

    final var command = new ConcatenateCommand(
      parent, extension, context.getChapters() );
    return command.call();
  }
}
