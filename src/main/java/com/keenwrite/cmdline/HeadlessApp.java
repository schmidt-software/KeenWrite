package com.keenwrite.cmdline;

import com.keenwrite.AppCommands;
import com.keenwrite.events.StatusEvent;
import org.greenrobot.eventbus.Subscribe;

import static com.keenwrite.events.Bus.register;
import static java.lang.String.format;

/**
 * Responsible for running the application in headless mode.
 */
public class HeadlessApp {

  /**
   * Contains directives that control text file processing.
   */
  private final Arguments mArgs;

  /**
   * Creates a new command-line version of the application.
   *
   * @param args The post-processed command-line arguments.
   */
  public HeadlessApp( final Arguments args ) {
    assert args != null;

    mArgs = args;

    register( this );
    AppCommands.run( mArgs );
  }

  /**
   * When a status message is shown, write it to the console, if not in
   * quiet mode.
   *
   * @param event The event published when the status changes.
   */
  @Subscribe
  public void handle( final StatusEvent event ) {
    assert event != null;

    if( !mArgs.quiet() ) {
      final var stacktrace = event.getProblem();
      final var problem = stacktrace.isBlank()
        ? ""
        : format( "%n%s", stacktrace );
      final var msg = format( "%s%s", event, problem );

      System.out.println( msg );
    }
  }

  /**
   * Entry point for running the application in headless mode.
   *
   * @param args The parsed command-line arguments.
   */
  @SuppressWarnings( "ConfusingMainMethod" )
  public static void main( final Arguments args ) {
    new HeadlessApp( args );
  }
}
