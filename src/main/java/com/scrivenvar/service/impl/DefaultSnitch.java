/*
 * Copyright 2020 White Magic Software, Ltd.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.scrivenvar.service.impl;

import com.scrivenvar.service.Snitch;

import java.io.IOException;
import java.nio.file.*;
import java.util.Collections;
import java.util.Map;
import java.util.Observable;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.scrivenvar.Constants.APP_WATCHDOG_TIMEOUT;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * Listens for file changes. Other classes can register paths to be monitored
 * and listen for changes to those paths.
 */
public class DefaultSnitch extends Observable implements Snitch {

  /**
   * Service for listening to directories for modifications.
   */
  private WatchService watchService;

  /**
   * Directories being monitored for changes.
   */
  private Map<WatchKey, Path> keys;

  /**
   * Files that will kick off notification events if modified.
   */
  private Set<Path> eavesdropped;

  /**
   * Set to true when running; set to false to stop listening.
   */
  private volatile boolean listening;

  public DefaultSnitch() {
  }

  @Override
  public void stop() {
    setListening( false );
  }

  /**
   * Adds a listener to the list of files to watch for changes. If the file is
   * already in the monitored list, this will return immediately.
   *
   * @param file Path to a file to watch for changes.
   * @throws IOException The file could not be monitored.
   */
  @Override
  public void listen( final Path file ) throws IOException {
    if( file != null && getEavesdropped().add( file ) ) {
      final Path dir = toDirectory( file );
      final WatchKey key = dir.register( getWatchService(), ENTRY_MODIFY );

      getWatchMap().put( key, dir );
    }
  }

  /**
   * Returns the given path to a file (or directory) as a directory. If the
   * given path is already a directory, it is returned. Otherwise, this returns
   * the directory that contains the file. This will fail if the file is stored
   * in the root folder.
   *
   * @param path The file to return as a directory, which should always be the
   *             case.
   * @return The given path as a directory, if a file, otherwise the path
   * itself.
   */
  private Path toDirectory( final Path path ) {
    return Files.isDirectory( path )
        ? path
        : path.toFile().getParentFile().toPath();
  }

  /**
   * Stop listening to the given file for change events. This fails silently.
   *
   * @param file The file to no longer monitor for changes.
   */
  @Override
  public void ignore( final Path file ) {
    if( file != null ) {
      final Path directory = toDirectory( file );

      // Remove all occurrences (there should be only one).
      getWatchMap().values().removeAll( Collections.singleton( directory ) );

      // Remove all occurrences (there can be only one).
      getEavesdropped().remove( file );
    }
  }

  /**
   * Loops until stop is called, or the application is terminated.
   */
  @Override
  @SuppressWarnings("BusyWait")
  public void run() {
    setListening( true );

    while( isListening() ) {
      try {
        final WatchKey key = getWatchService().take();
        final Path path = get( key );

        // Prevent receiving two separate ENTRY_MODIFY events: file modified
        // and timestamp updated. Instead, receive one ENTRY_MODIFY event
        // with two counts.
        Thread.sleep( APP_WATCHDOG_TIMEOUT );

        for( final WatchEvent<?> event : key.pollEvents() ) {
          final Path changed = path.resolve( (Path) event.context() );

          if( event.kind() == ENTRY_MODIFY && isListening( changed ) ) {
            setChanged();
            notifyObservers( changed );
          }
        }

        if( !key.reset() ) {
          ignore( path );
        }
      } catch( final IOException | InterruptedException ex ) {
        // Stop eavesdropping.
        setListening( false );
      }
    }
  }

  /**
   * Returns true if the list of files being listened to for changes contains
   * the given file.
   *
   * @param file Path to a system file.
   * @return true The given file is being monitored for changes.
   */
  private boolean isListening( final Path file ) {
    return getEavesdropped().contains( file );
  }

  /**
   * Returns a path for a given watch key.
   *
   * @param key The key to lookup its corresponding path.
   * @return The path for the given key.
   */
  private Path get( final WatchKey key ) {
    return getWatchMap().get( key );
  }

  private synchronized Map<WatchKey, Path> getWatchMap() {
    if( this.keys == null ) {
      this.keys = createWatchKeys();
    }

    return this.keys;
  }

  protected Map<WatchKey, Path> createWatchKeys() {
    return new ConcurrentHashMap<>();
  }

  /**
   * Returns a list of files that, when changed, will kick off a notification.
   *
   * @return A non-null, possibly empty, list of files.
   */
  private synchronized Set<Path> getEavesdropped() {
    if( this.eavesdropped == null ) {
      this.eavesdropped = createEavesdropped();
    }

    return this.eavesdropped;
  }

  protected Set<Path> createEavesdropped() {
    return ConcurrentHashMap.newKeySet();
  }

  /**
   * The existing watch service, or a new instance if null.
   *
   * @return A valid WatchService instance, never null.
   * @throws IOException Could not create a new watch service.
   */
  private synchronized WatchService getWatchService() throws IOException {
    if( this.watchService == null ) {
      this.watchService = createWatchService();
    }

    return this.watchService;
  }

  protected WatchService createWatchService() throws IOException {
    final FileSystem fileSystem = FileSystems.getDefault();
    return fileSystem.newWatchService();
  }

  /**
   * Answers whether the loop should continue executing.
   *
   * @return true The internal listening loop should continue listening for file
   * modification events.
   */
  protected boolean isListening() {
    return this.listening;
  }

  /**
   * Requests the snitch to stop eavesdropping on file changes.
   *
   * @param listening Use true to indicate the service should stop running.
   */
  private void setListening( final boolean listening ) {
    this.listening = listening;
  }
}
