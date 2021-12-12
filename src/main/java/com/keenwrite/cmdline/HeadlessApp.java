package com.keenwrite.cmdline;

import com.keenwrite.AppCommands;

/**
 * Responsible for running the application in headless mode.
 */
public class HeadlessApp {

  /**
   * Entry point for running the application in headless mode.
   *
   * @param args The parsed command-line arguments.
   */
  public static void main( final Arguments args ) {
    AppCommands.run( args );
  }
}
