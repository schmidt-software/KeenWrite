/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.io;

import org.renjin.repackaged.guava.collect.BiMap;
import org.renjin.repackaged.guava.collect.HashBiMap;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.file.FileSystems.getDefault;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.util.Collections.newSetFromMap;

/**
 * Responsible for watching when a file has been changed.
 */
public class FileWatchService implements Runnable {
  /**
   * Set to {@code false} when {@link #stop()} is called.
   */
  private volatile boolean mRunning;

  /**
   * Contains the listeners to notify when a given file has changed.
   */
  private final Set<FileModifiedListener> mListeners =
    newSetFromMap( new ConcurrentHashMap<>() );
  private final WatchService mWatchService;
  private final BiMap<File, WatchKey> mWatched = HashBiMap.create();

  /**
   * Creates a new file system watch service with the given files to watch.
   *
   * @param files The files to watch for file system events.
   */
  public FileWatchService( final File... files ) {
    mWatchService = createWatchService();

    try {
      for( final var file : files ) {
        register( file );
      }
    } catch( final Exception ex ) {
      throw new RuntimeException( ex );
    }
  }

  /**
   * Runs the event handler until {@link #stop()} is called.
   *
   * @throws RuntimeException There was an error watching for file events.
   */
  @Override
  public void run() {
    mRunning = true;

    while( mRunning ) {
      handleEvents();
    }
  }

  private void handleEvents() {
    try {
      final var watchKey = mWatchService.take();

      for( final var pollEvent : watchKey.pollEvents() ) {
        final var watchable = (Path) watchKey.watchable();
        final var context = (Path) pollEvent.context();
        final var file = watchable.resolve( context ).toFile();

        if( mWatched.containsKey( file ) ) {
          final var fileEvent = new FileEvent( file );

          for( final var listener : mListeners ) {
            listener.accept( fileEvent );
          }
        }
      }

      if( !watchKey.reset() ) {
        unregister( watchKey );
      }
    } catch( final Exception ex ) {
      throw new RuntimeException( ex );
    }
  }

  /**
   * Adds the given {@link File}'s containing directory to the watch list. When
   * the given {@link File} is modified, this service will receive a
   * notification that the containing directory has been modified, which will
   * then be filtered by file name.
   * <p>
   * This method is idempotent.
   * </p>
   *
   * @param file The {@link File} to watch for modification events.
   * @return The {@link File}'s directory watch state.
   * @throws IOException              Could not register the directory.
   * @throws IllegalArgumentException The {@link File} has no parent directory.
   */
  public WatchKey register( final File file ) throws IOException {
    if( mWatched.containsKey( file ) ) {
      return mWatched.get( file );
    }

    final var path = getParentDirectory( file );
    final var watchKey = path.register( mWatchService, ENTRY_MODIFY );

    return mWatched.put( file, watchKey );
  }

  /**
   * Removes the given {@link File}'s containing directory from the watch list.
   * <p>
   * This method is idempotent.
   * </p>
   *
   * @param file The {@link File} to no longer watch.
   * @throws IllegalArgumentException The {@link File} has no parent directory.
   */
  public void unregister( final File file ) {
    mWatched.remove( cancel( file ) );
  }

  /**
   * Cancels watching the given file for file system changes.
   *
   * @param file The {@link File} to watch for file events.
   * @return The given file, always.
   */
  private File cancel( final File file ) {
    final var watchKey = mWatched.get( file );

    if( watchKey != null ) {
      watchKey.cancel();
    }

    return file;
  }

  /**
   * Removes the given {@link WatchKey} from the registration map.
   *
   * @param watchKey The {@link WatchKey} to remove from the map.
   */
  private void unregister( final WatchKey watchKey ) {
    unregister( mWatched.inverse().get( watchKey ) );
  }

  /**
   * Adds a listener to be notified when a file under watch has been modified.
   * Listeners are backed by a set.
   *
   * @param listener The {@link FileModifiedListener} to add to the list.
   * @return {@code true} if this set did not already contain listener.
   */
  public boolean addListener( final FileModifiedListener listener ) {
    return mListeners.add( listener );
  }

  /**
   * Removes a listener from the notify list.
   *
   * @param listener The {@link FileModifiedListener} to remove.
   */
  public void removeListener( final FileModifiedListener listener ) {
    mListeners.remove( listener );
  }

  /**
   * Shuts down the file watch service and clears both watchers and listeners.
   *
   * @throws IOException Could not close the watch service.
   */
  public void stop() throws IOException {
    mRunning = false;

    for( final var file : mWatched.keySet() ) {
      cancel( file );
    }

    mWatched.clear();
    mListeners.clear();
    mWatchService.close();
  }

  /**
   * Returns the directory containing the given {@link File} instance.
   *
   * @param file The {@link File}'s containing directory to watch.
   * @return The {@link Path} to the {@link File}'s directory.
   * @throws IllegalArgumentException The {@link File} has no parent directory.
   */
  private Path getParentDirectory( final File file ) {
    assert file != null;
    assert !file.isDirectory();

    final var directory = file.getParentFile();

    if( directory == null ) {
      throw new IllegalArgumentException( file.getAbsolutePath() );
    }

    return directory.toPath();
  }

  private WatchService createWatchService() {
    try {
      return getDefault().newWatchService();
    } catch( final Exception ex ) {
      // Create a fallback that allows the class to be instantiated and used
      // without without preventing the application from launching.
      return new PollingWatchService();
    }
  }
}
