package com.keenwrite.typesetting.installer.panes;

import com.keenwrite.io.CommandNotFoundException;
import com.keenwrite.typesetting.container.api.Container;
import com.keenwrite.typesetting.container.impl.Podman;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import org.apache.commons.lang3.function.FailableConsumer;
import org.controlsfx.dialog.Wizard;

import static com.keenwrite.Messages.get;
import static java.lang.System.lineSeparator;
import static javafx.application.Platform.runLater;

public abstract class ContainerOutputPane extends InstallerPane {
  private final String PROP_INITIALIZER = getClass().getCanonicalName();

  private final String mCorrectKey;
  private final String mMissingKey;
  private final FailableConsumer<Container, CommandNotFoundException> mFc;
  private final Container mContainer;
  private final TextArea mTextArea;

  public ContainerOutputPane(
    final String headerKey,
    final String correctKey,
    final String missingKey,
    final FailableConsumer<Container, CommandNotFoundException> fc,
    final int cols
  ) {
    super( headerKey );

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

  /**
   * Creates a container that can have its standard output read as an input
   * stream that's piped directly to a {@link TextArea}.
   *
   * @param textarea The {@link TextArea} to receive text.
   * @return An object that can perform tasks against a container.
   */
  public static Container createContainer( final TextArea textarea ) {
    return new Podman( text -> append( textarea, text ) );
  }

  public static void append( final TextArea node, final String text ) {
    runLater( () -> {
      node.appendText( text );
      node.appendText( lineSeparator() );
    } );
  }
}
