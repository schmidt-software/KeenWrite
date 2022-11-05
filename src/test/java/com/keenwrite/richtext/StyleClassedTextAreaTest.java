package com.keenwrite.richtext;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.StyleClassedTextArea;

import java.net.URISyntaxException;

/**
 * Scaffolding for creating one-off tests, not run as part of test suite.
 */
public class StyleClassedTextAreaTest extends Application {
  private final StyleClassedTextArea mTextArea =
    new StyleClassedTextArea( false );

  private final VirtualizedScrollPane<StyleClassedTextArea> mScrollPane =
    new VirtualizedScrollPane<>( mTextArea );

  public static void main( final String[] args ) {
    launch( args );
  }

  @Override
  public void start( final Stage stage ) throws URISyntaxException {
    final var pane = new StackPane( mScrollPane );
    final var scene = new Scene( pane, 800, 600 );

    final var stylesheets = scene.getStylesheets();
    stylesheets.clear();
    stylesheets.add( getStylesheet( "skins/scene.css" ) );
    stylesheets.add( getStylesheet( "editor/markdown.css" ) );
    stylesheets.add( getStylesheet( "skins/monokai.css" ) );

    mTextArea.getStyleClass().add( "markdown" );
    mTextArea.insertText( 0, TEXT + TEXT + TEXT + TEXT );
    mTextArea.setStyle( "-fx-font-size: 13pt" );

    mTextArea.requestFollowCaret();
    mTextArea.moveTo( 4375 );

    stage.setScene( scene );
    stage.show();
  }

  private String getStylesheet( final String suffix )
    throws URISyntaxException {
    final var url = getClass().getResource( "/com/keenwrite/" + suffix );
    return url == null ? "" : url.toURI().toString();
  }

  private static final String TEXT = """
    In my younger and more vulnerable years my father gave me some advice
    that I’ve been turning over in my mind ever since.
                                                                                    
    “Whenever you feel like criticizing anyone,” he told me, “just
    remember that all the people in this world haven’t had the advantages
    that you’ve had.”
                                                                                    
    He didn’t say any more, but we’ve always been unusually communicative
    in a reserved way, and I understood that he meant a great deal more
    than that. In consequence, I’m inclined to reserve all judgements, a
    habit that has opened up many curious natures to me and also made me
    the victim of not a few veteran bores. The abnormal mind is quick to
    detect and attach itself to this quality when it appears in a normal
    person, and so it came about that in college I was unjustly accused of
    being a politician, because I was privy to the secret griefs of wild,
    unknown men. Most of the confidences were unsought—frequently I have
    feigned sleep, preoccupation, or a hostile levity when I realized by
    some unmistakable sign that an intimate revelation was quivering on
    the horizon; for the intimate revelations of young men, or at least
    the terms in which they express them, are usually plagiaristic and
    marred by obvious suppressions. Reserving judgements is a matter of
    infinite hope. I am still a little afraid of missing something if I
    forget that, as my father snobbishly suggested, and I snobbishly
    repeat, a sense of the fundamental decencies is parcelled out
    unequally at birth.""";
}
