/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.io;

import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.Watchable;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Responsible for polling the file system to see whether a file has been
 * updated. This is instantiated when an instance of {@link WatchService}
 * cannot be created using the Java API.
 * <p>
 * This is a skeleton class to avoid {@code null} references. In theory,
 * it should never get instantiated. If the application is run on a system
 * that does not support file system events, this should eliminate NPEs.
 * </p>
 */
public class PollingWatchService implements WatchService {
  private final WatchKey EMPTY_KEY = new WatchKey() {
    private final Watchable WATCHABLE = new Watchable() {
      @Override
      public WatchKey register(
        final WatchService watcher,
        final WatchEvent.Kind<?>[] events,
        final WatchEvent.Modifier... modifiers ) {
        return EMPTY_KEY;
      }

      @Override
      public WatchKey register(
        final WatchService watcher, final WatchEvent.Kind<?>... events ) {
        return EMPTY_KEY;
      }
    };

    @Override
    public boolean isValid() {
      return false;
    }

    @Override
    public List<WatchEvent<?>> pollEvents() {
      return List.of();
    }

    @Override
    public boolean reset() {
      return false;
    }

    @Override
    public void cancel() {
    }

    @Override
    public Watchable watchable() {
      return WATCHABLE;
    }
  };

  @Override
  public void close() {
  }

  @Override
  public WatchKey poll() {
    return EMPTY_KEY;
  }

  @Override
  public WatchKey poll( final long timeout, final TimeUnit unit ) {
    return EMPTY_KEY;
  }

  @Override
  public WatchKey take() {
    return EMPTY_KEY;
  }
}
