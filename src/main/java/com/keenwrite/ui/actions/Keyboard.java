package com.keenwrite.ui.actions;

import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;

import static javafx.scene.input.KeyCode.C;
import static javafx.scene.input.KeyCode.INSERT;
import static javafx.scene.input.KeyCombination.CONTROL_ANY;

public class Keyboard {
  public static final KeyCodeCombination CTRL_C =
    new KeyCodeCombination( C, CONTROL_ANY );
  public static final KeyCodeCombination CTRL_INSERT =
    new KeyCodeCombination( INSERT, CONTROL_ANY );

  /**
   * Answers whether the user issued a copy request via the keyboard.
   *
   * @param event The keyboard event to examine.
   * @return {@code true} if the user pressed Ctrl+C or Ctrl+Insert.
   */
  public static boolean isCopy( final KeyEvent event ) {
    return CTRL_C.match( event ) || CTRL_INSERT.match( event );
  }
}
