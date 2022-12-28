package com.keenwrite.typesetting.installer.panes;

import com.keenwrite.io.CommandNotFoundException;
import com.keenwrite.typesetting.containerization.ContainerManager;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import org.apache.commons.lang3.function.FailableConsumer;
import org.controlsfx.dialog.Wizard;

import static com.keenwrite.Messages.get;

/**
 * Responsible for showing the output from running commands against a container
 * manager. There are a few installation steps that run different commands
 * against the installer, which are platform-specific and cannot be merged.
 * Common functionality between them is codified in this class.
 */
public abstract class ManagerOutputPane extends InstallerPane {
  private final String PROP_INITIALIZER = getClass().getCanonicalName();

  private final String mCorrectKey;
  private final String mMissingKey;
  private final FailableConsumer<ContainerManager, CommandNotFoundException> mFc;
  private final ContainerManager mContainer;
  private final TextArea mTextArea;

  public ManagerOutputPane(
    final String correctKey,
    final String missingKey,
    final FailableConsumer<ContainerManager, CommandNotFoundException> fc,
    final int cols
  ) {
    mFc = fc;
    mCorrectKey = correctKey;
    mMissingKey = missingKey;

    mTextArea = textArea( 5, cols );
    final var titledPane = titledPane( "Output", mTextArea );
    final var borderPane = new BorderPane();
    borderPane.setBottom( titledPane );

    mContainer = createContainer( mTextArea );

    setContent( borderPane );
  }

  @Override
  public void onEnteringPage( final Wizard wizard ) {
    disableNext( true );

    try {
      final var properties = wizard.getProperties();
      final var thread = properties.get( PROP_INITIALIZER );

      if( thread instanceof Thread initializer && initializer.isAlive() ) {
        return;
      }

      final var task = createTask( () -> {
        mFc.accept( mContainer );
        properties.remove( thread );
        return null;
      } );

      task.setOnSucceeded( event -> {
        append( mTextArea, get( mCorrectKey ) );
        disableNext( false );
      } );
      task.setOnFailed( event -> append( mTextArea, get( mMissingKey ) ) );
      task.setOnCancelled( event -> append( mTextArea, get( mMissingKey ) ) );

      final var initializer = createThread( task );
      properties.put( PROP_INITIALIZER, initializer );
      initializer.start();
    } catch( final Exception e ) {
      throw new RuntimeException( e );
    }
  }
}
