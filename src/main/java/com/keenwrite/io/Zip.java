/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.io;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Responsible for managing zipped archive files.
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
    assert !zipPath.toFile().isDirectory();

    try( final var zipFile = new ZipFile( zipPath.toFile() ) ) {
      iterate( zipFile );
    }
  }

  /**
   * Extracts each entry in the zip archive file.
   *
   * @param zipFile The archive to extract.
   * @throws IOException Could not extract the zip file entry.
   */
  private static void iterate( final ZipFile zipFile )
    throws IOException {
    // Determine the directory name where the zip archive resides. Files will
    // be extracted relative to that directory.
    final var path = getDirectory( zipFile );
    final var entries = zipFile.entries();

    while( entries.hasMoreElements() ) {
      final var zipEntry = entries.nextElement();
      final var zipEntryPath = path.resolve( zipEntry.getName() );

      // Guard against zip slip.
      if( zipEntryPath.normalize().startsWith( path ) ) {
        extract( zipFile, zipEntry, zipEntryPath );
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
    // Only attempt to extract files, skipping empty directories.
    if( !zipEntry.isDirectory() ) {
      createDirectories( zipEntryPath.getParent() );

      try( final var in = zipFile.getInputStream( zipEntry ) ) {
        Files.copy( in, zipEntryPath, REPLACE_EXISTING );
      }
    }
  }

  /**
   * Helper method to return the normalized directory where the given archive
   * resides.
   *
   * @param zipFile The {@link ZipFile} having a path to normalize.
   * @return The directory containing the given {@link ZipFile}.
   * @throws IOException The zip file has no parent directory.
   */
  private static Path getDirectory( final ZipFile zipFile ) throws IOException {
    final var zipPath = Path.of( zipFile.getName() );
    final var parent = zipPath.getParent();

    if( parent == null ) {
      throw new IOException( zipFile.getName() + " has no parent directory." );
    }

    return parent.normalize();
  }
}
