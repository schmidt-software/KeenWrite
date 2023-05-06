/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.events.workspace;

import com.keenwrite.preferences.Workspace;

/**
 * Indicates that the {@link Workspace} has been loaded.
 */
public class WorkspaceLoadedEvent extends WorkspaceEvent {
  private final Workspace mWorkspace;

  private WorkspaceLoadedEvent( final Workspace workspace ) {
    assert workspace != null;

    mWorkspace = workspace;
  }

  /**
   * Publishes an event that indicates a new {@link Workspace} has been loaded.
   */
  public static void fire( final Workspace workspace ) {
    new WorkspaceLoadedEvent( workspace ).publish();
  }

  /**
   * Returns a reference to the {@link Workspace} that was loaded.
   *
   * @return The {@link Workspace} that has loaded user preferences.
   */
  @SuppressWarnings( "unused" )
  private Workspace getWorkspace() {
    return mWorkspace;
  }
}
