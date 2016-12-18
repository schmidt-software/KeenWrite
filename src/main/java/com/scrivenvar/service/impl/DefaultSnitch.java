/*
 * Copyright 2016 White Magic Software, Ltd.
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
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Listens for file changes.
 *
 * @author White Magic Software, Ltd.
 */
public class DefaultSnitch implements Snitch, Runnable {

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
  private boolean listening;

  public DefaultSnitch() {
  }

  @Override
  public void stop() {
    setListening( false );
  }

  /**
   * Adds a listener to the list of files to watch for changes.
   *
   * @param file Path to a file to watch for changes.
   *
   * @throws IOException The file could not be monitored.
   */
  @Override
  public void listen( final Path file ) throws IOException {
    // This will fail if the file is stored in the root folder.
    final Path path = Files.isDirectory( file ) ? file : file.getParent();
    final WatchKey key = path.register( getWatchService(), ENTRY_MODIFY );

    getWatchMap().put( key, path );
    getEavesdropped().add( file );
  }

  /**
   * Stop listening to the given file for change events. This fails silently.
   *
   * @param file The file to no longer monitor for changes.
   */
  @Override
  public void ignore( final Path file ) {
    // Remove all occurrences.
    getWatchMap().values().removeAll( Collections.singleton( file ) );
  }

  /**
   * Loops until isRunning is set to false.
   */
  @Override
  public void run() {
    setListening( true );

    while( isListening() ) {
      try {
        final WatchKey key = getWatchService().take();
        final Path path = get( key );

        for( WatchEvent<?> event : key.pollEvents() ) {
          final Path changed = (Path)event.context();

          for( final Path file : getEavesdropped() ) {
            System.out.println( "Changed: " + changed );
            System.out.println( "Monitored: " + file );
          }
        }

        if( !key.reset() ) {
          ignore( path );
        }
      } catch( IOException | InterruptedException ex ) {
        // Stop eavesdropping.
        setListening( false );
      }
    }
  }

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
    return new HashMap<>();
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
    return new HashSet<>();
  }

  /**
   * The existing watch service, or a new instance if null.
   *
   * @return A valid WatchService instance, never null.
   *
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
