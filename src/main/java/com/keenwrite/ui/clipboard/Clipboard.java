/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.ui.clipboard;

import javafx.scene.input.ClipboardContent;

import static javafx.scene.input.Clipboard.getSystemClipboard;

/**
 * Responsible for pasting into the computer's clipboard.
 */
public class Clipboard {
  /**
   * Pastes the given text into the clipboard, overwriting all data.
   *
   * @param text The text to insert into the clipboard.
   */
  public static void write( final String text ) {
    final var contents = new ClipboardContent();
    contents.putString( text );
    getSystemClipboard().setContent( contents );
  }

  /**
   * Delegates to {@link #write(String)}.
   *
   * @see #write(String)
   */
  public static void write( final StringBuilder text ) {
    write( text.toString() );
  }
}
