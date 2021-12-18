package com.keenwrite.preferences;

import com.keenwrite.cmdline.Arguments;

import java.io.File;

/**
 * Responsible for maintaining key-value pairs for user-defined setting
 * values. When processing a document, various settings are used to configure
 * the processing behaviour. This interface represents an abstraction that
 * can be used by the processors without having to depend on a specific
 * implementation, such as {@link Arguments} or a {@link Workspace}.
 */
public interface KeyConfiguration {

  /**
   * Returns a {@link String} value associated with the given {@link Key}.
   *
   * @param key The {@link Key} associated with a value.
   * @return The value associated with the given {@link Key}.
   */
  String getString( final Key key );

  /**
   * Returns a {@link Boolean} value associated with the given {@link Key}.
   *
   * @param key The {@link Key} associated with a value.
   * @return The value associated with the given {@link Key}.
   */
  boolean getBoolean( final Key key );

  /**
   * Returns an {@link Integer} value associated with the given {@link Key}.
   *
   * @param key The {@link Key} associated with a value.
   * @return The value associated with the given {@link Key}.
   */
  int getInteger( final Key key );

  /**
   * Returns a {@link Double} value associated with the given {@link Key}.
   *
   * @param key The {@link Key} associated with a value.
   * @return The value associated with the given {@link Key}.
   */
  double getDouble( final Key key );

  /**
   * Returns a {@link File} value associated with the given {@link Key}.
   *
   * @param key The {@link Key} associated with a value.
   * @return The value associated with the given {@link Key}.
   */
  File asFile( final Key key );
}
