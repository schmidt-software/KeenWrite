/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite;

import javafx.embed.swing.JFXPanel;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testfx.osgi.service.TestFx;

import java.util.concurrent.Semaphore;

import static javafx.application.Platform.runLater;
import static javax.swing.SwingUtilities.invokeLater;

/**
 * Blocks all unit tests until JavaFX is ready.
 */
public class AwaitFxExtension implements BeforeAllCallback {
  /**
   * Prevent {@link RuntimeException} for internal graphics not initialized yet.
   *
   * @param context Provided by the {@link TestFx} framework.
   * @throws InterruptedException Could not acquire semaphore.
   */
  @Override
  public void beforeAll( final ExtensionContext context )
    throws InterruptedException {
    final var semaphore = new Semaphore( 0 );

    invokeLater( () -> {
      // Prepare JavaFX toolkit and environment.
      new JFXPanel();
      runLater( semaphore::release );
    } );

    semaphore.acquire();
  }
}
