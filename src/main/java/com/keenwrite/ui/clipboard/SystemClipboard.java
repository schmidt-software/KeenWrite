/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.ui.clipboard;

import javafx.scene.control.TableView;
import javafx.scene.input.ClipboardContent;

import java.util.TreeSet;

import static javafx.scene.input.Clipboard.getSystemClipboard;

/**
 * Responsible for pasting into the computer's clipboard.
 */
public class SystemClipboard {
  /**
   * Copies the given text into the clipboard, overwriting all data.
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

  /**
   * Copies the contents of the selected rows into the clipboard; code is from
   * <a href="https://stackoverflow.com/a/48126059/59087">StackOverflow</a>.
   *
   * @param table The {@link TableView} having selected rows to copy.
   */
  public static <T> void write( final TableView<T> table ) {
    final var sb = new StringBuilder( 2048 );
    final var rows = new TreeSet<Integer>();
    final var cols = table.getColumns();

    for( final var position : table.getSelectionModel().getSelectedCells() ) {
      rows.add( position.getRow() );
    }

    String rSep = "";

    for( final var row : rows ) {
      sb.append( rSep );

      String cSep = "";

      for( final var column : cols ) {
        sb.append( cSep );

        final var data = column.getCellData( row );
        sb.append( data == null ? "" : data.toString() );

        cSep = "\t";
      }

      rSep = "\n";
    }

    write( sb );
  }
}
