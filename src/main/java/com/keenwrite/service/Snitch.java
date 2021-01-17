/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.service;

import com.keenwrite.io.FileModifiedListener;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Observer;

/**
 * Listens for changes to file system files and directories.
 *
 * @deprecated Use {@link FileModifiedListener} and {@link FileWatchService}.
 */
@Deprecated
public interface Snitch extends Service, Runnable {

  /**
   * Adds an observer to the set of observers for this object, provided that it
   * is not the same as some observer already in the set. The order in which
   * notifications will be delivered to multiple observers is not specified.
   *
   * @param o The object to receive changed events for when monitored files
   *          are changed.
   */
  void addObserver( Observer o );

  /**
   * Listens for changes to the path. If the path specifies a file, then only
   * notifications pertaining to that file are sent. Otherwise, change events
   * for the directory that contains the file are sent. This method must allow
   * for multiple calls to the same file without incurring additional listeners
   * or events.
   *
   * @param file Send notifications when this file changes, can be null.
   * @throws IOException Couldn't create a watcher for the given file.
   */
  void listen( Path file ) throws IOException;

  /**
   * Removes the given file from the notifications list.
   *
   * @param file The file to stop monitoring for any changes, can be null.
   */
  void ignore( Path file );

  /**
   * Start listening for events on a new thread.
   */
  void start();

  /**
   * Stop listening for events.
   */
  void stop();
}
