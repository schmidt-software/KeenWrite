/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.editors;

import com.keenwrite.io.MediaType;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.scene.Node;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Path;

import static com.keenwrite.constants.Constants.DEFAULT_CHARSET;
import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.io.SysFile.toFile;
import static java.nio.charset.Charset.forName;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.write;
import static java.util.Arrays.asList;
import static java.util.Locale.ENGLISH;

/**
 * A text resource can be persisted and retrieved from its persisted location.
 */
public interface TextResource {
  /**
   * Sets the text string that to be changed through some graphical user
   * interface. For example, a YAML document must be parsed from the given
   * text string into a tree view with which the user may interact.
   *
   * @param text The new content for the resource.
   */
  void setText( String text );

  /**
   * Returns the text string that may have been modified by the user through
   * some graphical user interface.
   *
   * @return The text value, based on the value set from
   * {@link #setText(String)}, but possibly mutated.
   */
  String getText();

  /**
   * Return the character encoding for this file.
   *
   * @return A non-null character set, primarily detected from file contents.
   */
  Charset getEncoding();

  /**
   * Renames the current file to the given fully qualified file name.
   *
   * @param file The new file name.
   */
  void rename( final File file );

  /**
   * Returns the file name, without any directory components, for this instance.
   * Useful for showing as a tab title.
   *
   * @return The file name value returned from {@link #getFile()}.
   */
  default String getFilename() {
    final var filename = getFile().toPath().getFileName();
    return filename == null ? "" : filename.toString();
  }

  /**
   * Returns the fully qualified {@link File} to the editable text resource.
   * Useful for showing as a tab tooltip, saving the file, or reading it.
   *
   * @return A non-null {@link File} instance.
   */
  File getFile();

  /**
   * Returns the {@link MediaType} associated with the file being edited.
   *
   * @return The {@link MediaType} for the editor's file.
   */
  default MediaType getMediaType() {
    return MediaType.fromFilename( getFile() );
  }

  /**
   * Answers whether this instance is an editor for at least one of the given
   * {@link MediaType} references.
   *
   * @param mediaTypes The {@link MediaType} references to compare against.
   * @return {@code true} if the given list of media types contains the
   * {@link MediaType} for this editor.
   */
  default boolean isMediaType( final MediaType... mediaTypes ) {
    return asList( mediaTypes ).contains( getMediaType() );
  }

  /**
   * Returns the fully qualified {@link Path} to the editable text resource.
   * This delegates to {@link #getFile()}.
   *
   * @return A non-null {@link Path} instance.
   */
  default Path getPath() {
    return getFile().toPath();
  }

  /**
   * Read the file contents and update the text accordingly. If the file
   * cannot be read then no changes will happen to the text. Fails silently.
   *
   * @param path The fully qualified {@link Path}, including a file name, to
   *             fully read into the editor.
   * @return The character encoding for the file at the given {@link Path}.
   */
  default Charset open( final Path path ) {
    final var file = toFile( path );
    Charset encoding = DEFAULT_CHARSET;

    try {
      if( file.exists() ) {
        if( file.canWrite() && file.canRead() ) {
          final var bytes = readAllBytes( path );
          encoding = detectEncoding( bytes );

          setText( asString( bytes, encoding ) );
        }
        else {
          clue( "TextResource.load.error.permissions", file.toString() );
        }
      }
      else {
        clue( "TextResource.load.error.unsaved", file.toString() );
      }
    } catch( final Exception ex ) {
      clue( ex );
    }

    return encoding;
  }

  /**
   * Read the file contents and update the text accordingly. If the file
   * cannot be read then no changes will happen to the text. This delegates
   * to {@link #open(Path)}.
   *
   * @param file The {@link File} to fully read into the editor.
   * @return The file's character encoding.
   */
  default Charset open( final File file ) {
    return open( file.toPath() );
  }

  /**
   * Save the file contents and clear the modified flag. If the file cannot
   * be saved, the exception is swallowed and this method returns {@code false}.
   *
   * @return {@code true} the file was saved; {@code false} if upon exception.
   */
  default boolean save() {
    try {
      write( getPath(), asBytes( getText() ) );
      clearModifiedProperty();
      return true;
    } catch( final Exception ex ) {
      clue( ex );
    }

    return false;
  }

  /**
   * Returns the node associated with this {@link TextResource}.
   *
   * @return The view component for the {@link TextResource}.
   */
  Node getNode();

  /**
   * Answers whether the resource has been modified.
   *
   * @return {@code true} the resource has changed; {@code false} means that
   * no changes to the resource have been made.
   */
  default boolean isModified() {
    return modifiedProperty().get();
  }

  /**
   * Returns a property that answers whether this text resource has been
   * changed from the original text that was opened.
   *
   * @return A property representing the modified state of this
   * {@link TextResource}.
   */
  ReadOnlyBooleanProperty modifiedProperty();

  /**
   * Lowers the modified flag such that listeners to the modified property
   * will be informed that the text that's being edited no longer differs
   * from what's persisted.
   */
  void clearModifiedProperty();

  private String asString( final byte[] text, final Charset encoding ) {
    return new String( text, encoding );
  }

  /**
   * Converts the given string to an array of bytes using the encoding that was
   * originally detected (if any) and associated with this file.
   *
   * @param text The text to convert into the original file encoding.
   * @return A series of bytes ready for writing to a file.
   */
  private byte[] asBytes( final String text ) {
    return text.getBytes( getEncoding() );
  }

  private Charset detectEncoding( final byte[] bytes ) {
    final var detector = new UniversalDetector( null );
    detector.handleData( bytes, 0, bytes.length );
    detector.dataEnd();

    final var charset = detector.getDetectedCharset();

    return charset == null
      ? DEFAULT_CHARSET
      : forName( charset.toUpperCase( ENGLISH ) );
  }

  /**
   * Answers whether the given resource are of the same conceptual type. This
   * method is intended to be overridden by subclasses.
   *
   * @param mediaType The type to compare.
   * @return {@code true} if the {@link TextResource} is compatible with the
   * given {@link MediaType}.
   */
  default boolean supports( final MediaType mediaType ) {
    return isMediaType( mediaType );
  }
}
