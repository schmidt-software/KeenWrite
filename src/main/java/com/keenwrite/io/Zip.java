/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.io;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Responsible for managing zipped archive files. Does not handle archives
 * within archives.
 */
public final class Zip {
  /**
   * Extracts the contents of the zip archive into its current directory. The
   * contents of the archive must be {@link StandardCharsets#UTF_8}. For
   * example, if the {@link Path} is <code>/tmp/filename.zip</code>, then
   * the contents of the file will be extracted into <code>/tmp</code>.
   *
   * @param zipPath The {@link Path} to the zip file to extract.
   * @throws IOException Could not extract the zip file, zip entries, or find
   *                     the parent directory that contains the path to the
   *                     zip archive.
   */
  public static void extract( final Path zipPath ) throws IOException {
    final var path = zipPath.getParent().normalize();

    iterate( zipPath, ( zipFile, zipEntry ) -> {
      // Determine the directory name where the zip archive resides. Files will
      // be extracted relative to that directory.
      final var zipEntryPath = path.resolve( zipEntry.getName() );

      // Guard against zip slip.
      if( zipEntryPath.normalize().startsWith( path ) ) {
        try {
          extract( zipFile, zipEntry, zipEntryPath );
        } catch( final IOException ex ) {
          throw new UncheckedIOException( ex );
        }
      }
    } );
  }

  /**
   * Returns the first root-level directory found in the zip archive. Only call
   * this function if you know there is exactly one top-level directory in the
   * zip archive. If there are multiple top-level directories, one of the
   * directories will be returned, albeit indeterminately. No files are
   * extracted when calling this function.
   *
   * @param zipPath The path to the zip archive to process.
   * @return The fully qualified root-level directory resolved relatively to
   * the zip archive itself.
   * @throws IOException Could not process the zip archive.
   */
  public static Path root( final Path zipPath ) throws IOException {
    // Directory that contains the zip archive file.
    final var zipParent = zipPath.getParent();

    if( zipParent == null ) {
      throw new IOException( zipPath + " has no parent" );
    }

    final var result = new AtomicReference<>( zipParent );

    iterate( zipPath, ( zipFile, zipEntry ) -> {
      final var zipEntryPath = Path.of( zipEntry.getName() );

      // The first entry without a parent is considered the root-level entry.
      // Return the relative directory path to that entry.
      if( zipEntryPath.getParent() == null ) {
        result.set( zipParent.resolve( zipEntryPath ) );
      }
    } );

    // The zip file doesn't have a sane folder structure, so return the
    // directory where the zip file was found.
    return result.get();
  }

  /**
   * Processes each entry in the zip archive.
   *
   * @param zipPath  The path to the zip file being processed.
   * @param consumer The {@link BiConsumer} that receives each entry in the
   *                 zip archive.
   * @throws IOException Could not extract zip file entries.
   */
  private static void iterate(
    final Path zipPath,
    final BiConsumer<ZipFile, ZipEntry> consumer )
    throws IOException {
    assert zipPath.toFile().isFile();

    try( final var zipFile = new ZipFile( zipPath.toFile() ) ) {
      final var entries = zipFile.entries();

      while( entries.hasMoreElements() ) {
        consumer.accept( zipFile, entries.nextElement() );
      }
    }
  }

  /**
   * Extracts a single entry of a zip file to a given directory. This will
   * create the necessary directory path if it doesn't exist. Empty
   * directories are not re-created.
   *
   * @param zipFile      The zip archive to extract.
   * @param zipEntry     An entry in the zip archive.
   * @param zipEntryPath The file location to write the zip entry.
   * @throws IOException Could not extract the zip file entry.
   */
  private static void extract(
    final ZipFile zipFile,
    final ZipEntry zipEntry,
    final Path zipEntryPath ) throws IOException {
    // Only extract files, skip empty directories.
    if( !zipEntry.isDirectory() ) {
      createDirectories( zipEntryPath.getParent() );

      try( final var in = zipFile.getInputStream( zipEntry ) ) {
        Files.copy( in, zipEntryPath, REPLACE_EXISTING );
      }
    }
  }
}
