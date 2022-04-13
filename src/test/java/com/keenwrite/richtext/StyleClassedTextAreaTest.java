package com.keenwrite.richtext;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Scaffolding for creating one-off tests, not run as part of test suite.
 */
public class StyleClassedTextAreaTest extends Application {
  private final org.fxmisc.richtext.StyleClassedTextArea mTextArea =
    new org.fxmisc.richtext.StyleClassedTextArea( false );

  public static void main( final String[] args ) {
    launch( args );
  }

  @Override
  public void start( final Stage stage ) {
    final var pane = new StackPane( mTextArea );
    final var scene = new Scene( pane, 600, 400 );

    stage.setScene( scene );
    stage.show();
  }
}
