/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.events;

import com.keenwrite.editors.TextEditor;

public class TextEditorFocusEvent extends FocusEvent<TextEditor> {
  protected TextEditorFocusEvent( final TextEditor editor ) {
    super( editor );
  }

  /**
   * When the {@link TextEditor} has focus, fire an event so that subscribers
   * may perform an action---such as parsing and rendering the contents.
   *
   * @param editor The instance of editor that has gained input focus.
   */
  public static void fireTextEditorFocus( final TextEditor editor ) {
    new TextEditorFocusEvent( editor ).publish();
  }
}
