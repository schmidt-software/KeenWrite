/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.typesetting.installer.panes;

import com.keenwrite.io.CommandNotFoundException;
import com.keenwrite.typesetting.containerization.ContainerManager;
import com.keenwrite.typesetting.containerization.StreamProcessor;
import com.keenwrite.util.FailableBiConsumer;
import javafx.collections.ObservableMap;
import javafx.concurrent.Task;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import org.controlsfx.dialog.Wizard;

import static com.keenwrite.Messages.get;
import static com.keenwrite.io.StreamGobbler.gobble;

/**
 * Responsible for showing the output from running commands against a container
 * manager. There are a few installation steps that run different commands
 * against the installer, which are platform-specific and cannot be merged.
 * Common functionality between them is codified in this class.
 */
public abstract class ManagerOutputPane extends InstallerPane {
  private final static String PROP_EXECUTOR =
    ManagerOutputPane.class.getCanonicalName();

  private final String mCorrectKey;
  private final String mMissingKey;
  private final FailableBiConsumer
    <ContainerManager, StreamProcessor, CommandNotFoundException> mFc;
  private final ContainerManager mContainer;
  private final TextArea mTextArea;

  public ManagerOutputPane(
    final String correctKey,
    final String missingKey,
    final FailableBiConsumer
      <ContainerManager, StreamProcessor, CommandNotFoundException> fc,
    final int cols
  ) {
    mFc = fc;
    mCorrectKey = correctKey;
    mMissingKey = missingKey;
    mTextArea = textArea( 5, cols );
    mContainer = createContainer();

    final var borderPane = new BorderPane();
    final var titledPane = titledPane( "Output", mTextArea );

    borderPane.setBottom( titledPane );
    setContent( borderPane );
  }

  @Override
  public void onEnteringPage( final Wizard wizard ) {
    disableNext( true );

    try {
      final var properties = wizard.getProperties();
      final var thread = properties.get( PROP_EXECUTOR );

      if( thread instanceof Thread executor && executor.isAlive() ) {
        return;
      }

      final Task<Void> task = createTask( properties, thread );
      final var executor = createThread( task );

      properties.put( PROP_EXECUTOR, executor );
      executor.start();
    } catch( final Exception e ) {
      throw new RuntimeException( e );
    }
  }

  private Task<Void> createTask(
    final ObservableMap<Object, Object> properties,
    final Object thread ) {
    final Task<Void> task = createTask( () -> {
      mFc.accept(
        mContainer,
        input -> gobble( input, line -> append( mTextArea, line ) )
      );
      properties.remove( thread );
      return null;
    } );

    task.setOnSucceeded( _ -> {
      append( mTextArea, get( mCorrectKey ) );
      properties.remove( thread );
      disableNext( false );
    } );
    task.setOnFailed( _ -> append( mTextArea, get( mMissingKey ) ) );
    task.setOnCancelled( _ -> append( mTextArea, get( mMissingKey ) ) );
    return task;
  }
}
