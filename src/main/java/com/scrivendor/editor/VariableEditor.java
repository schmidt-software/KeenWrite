/*
 * Copyright 2016 White Magic Software, Ltd.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.scrivendor.editor;

import com.scrivendor.FileEditorPane;
import com.scrivendor.Services;
import com.scrivendor.definition.DefinitionPane;
import static com.scrivendor.definition.Lists.getFirst;
import static com.scrivendor.definition.Lists.getLast;
import com.scrivendor.service.Settings;
import java.util.function.Consumer;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.scene.control.IndexRange;
import javafx.scene.control.TreeItem;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import static javafx.scene.input.KeyCode.DIGIT2;
import static javafx.scene.input.KeyCode.MINUS;
import static javafx.scene.input.KeyCode.PERIOD;
import static javafx.scene.input.KeyCombination.SHIFT_DOWN;
import javafx.scene.input.KeyEvent;
import org.fxmisc.richtext.StyledTextArea;
import org.fxmisc.wellbehaved.event.EventPattern;
import static org.fxmisc.wellbehaved.event.EventPattern.keyPressed;
import static org.fxmisc.wellbehaved.event.EventPattern.keyTyped;
import org.fxmisc.wellbehaved.event.InputMap;
import static org.fxmisc.wellbehaved.event.InputMap.consume;
import static org.fxmisc.wellbehaved.event.InputMap.sequence;

/**
 * Provides the logic for editing variable names within the editor.
 *
 * @author White Magic Software, Ltd.
 */
public class VariableEditor {

  private final Settings settings = Services.load( Settings.class );

  /**
   * Used to capture keyboard events once the user presses @.
   */
  private InputMap<InputEvent> keyboardMap;

  private FileEditorPane fileEditorPane;
  private DefinitionPane definitionPane;

  /**
   * Position of the variable in the text when in variable mode.
   */
  private int initialCaretPosition = 0;

  public VariableEditor(
    final FileEditorPane editorPane,
    final DefinitionPane definitionPane ) {
    setFileEditorPane( editorPane );
    setDefinitionPane( definitionPane );

    initKeyboardEventListeners();
  }

  /**
   * The @ symbol is a short-cut to inserting a YAML variable reference.
   *
   * @param e Superfluous information about the key that was pressed.
   */
  private void atPressed( KeyEvent e ) {
    startEventCapture();
    setInitialCaretPosition();
    advancePath();
  }

  /**
   * Receives key presses until the user completes the variable selection. This
   * allows the arrow keys to be used for selecting variables.
   *
   * @param e The key that was pressed.
   */
  private void vModeKeyPressed( KeyEvent e ) {
    final KeyCode keyCode = e.getCode();

    switch( keyCode ) {
      case BACK_SPACE:
        deleteSelection();

        // Break out of variable mode by back spacing to the original position.
        if( getCurrentCaretColumn() > getInitialCaretPosition() ) {
          break;
        }

      case ESCAPE:
        stopEventCapture();
        break;

      case RIGHT:
      case END:
        advancePath();
        break;

      case ENTER:
        acceptPath();
        break;

      case UP:
        cyclePathPrev();
        break;

      case DOWN:
        cyclePathNext();
        break;

      default:
        if( isVariableNameKey( e ) ) {
          updateEditorText( e.getText() );
          advancePath();
        }

        break;
    }

    e.consume();
  }

  /**
   * Updates the text with the path selected (or typed) by the user.
   */
  private void advancePath() {
    System.out.println( "----------" );
    System.out.println( "advancePath" );

    final String word = getCurrentWord();
    System.out.println( "current word = '" + word + "'" );

    final TreeItem<String> node = findNode( word );
    final String text = node.getValue();

    if( !node.isLeaf() ) {
      expand( node );

      System.out.println( "node = '" + node.getValue() + "'" );

//      final TreeItem<String> child = getFirst( node.getChildren() );
      // TODO: Magically decide how much of the path overlaps the node.
//      final String nodeValue = node.getValue();
//      final int index = nodeValue.indexOf( word );
//
//      if( index == 0 && !nodeValue.equals( word ) ) {
//        typeAhead = nodeValue.substring( word.length() );
//      }
    }

    updateEditorText( text );
  }

  /**
   * Inserts the string at the current caret position, replacing any selected
   * text.
   *
   * @param text The text to insert, never null.
   */
  private void updateEditorText( final String text ) {
    final StyledTextArea t = getEditor();

    final int length = text.length();
    final int posBegan = getInitialCaretPosition();
    final int posEnded = posBegan + length;

    t.replaceSelection( text );

    if( posEnded - posBegan > 0 ) {
      t.selectRange( posEnded, posBegan );
    }
  }

  /**
   * Called when the user presses either End or Enter key.
   */
  private void acceptPath() {
    final IndexRange range = getSelectionRange();
    final StyledTextArea textArea = getEditor();

    if( range != null ) {
      final int rangeEnd = range.getEnd();
      textArea.deselect();
      textArea.moveTo( rangeEnd );
    }
  }

  /**
   * Called when the user presses the Backspace key.
   */
  private void deleteSelection() {
    final StyledTextArea textArea = getEditor();
    textArea.replaceSelection( "" );
    textArea.deletePreviousChar();
  }

  /**
   * Cycles the selected text through the nodes.
   *
   * @param direction true - next; false - previous
   */
  private void cycleSelection( final boolean direction ) {
    final String word = getCurrentWord();
    final TreeItem<String> node = findNode( word );

    System.out.println( "---------------" );
    System.out.println( "cycle selection" );
    System.out.println( "path = '" + word + "'" );

    // Find the sibling for the current selection and replace the current
    // selection with the sibling's value
    TreeItem<String> cycled = direction
      ? node.nextSibling()
      : node.previousSibling();

    // When cycling at the end (or beginning) of the list, jump to the first
    // (or last) sibling depending on the cycle direction.
    if( cycled == null ) {
      cycled = direction ? getFirstSibling( node ) : getLastSibling( node );
    }

    System.out.println( "cycled value = '" + cycled.getValue() + "'" );

    expand( cycled );
    updateEditorText( cycled.getValue() );
  }

  /**
   * Cycles to the next sibling of the currently selected tree node.
   */
  private void cyclePathNext() {
    cycleSelection( true );
  }

  private void cyclePathPrev() {
    cycleSelection( false );
  }

  private <T> ObservableList<TreeItem<T>> getSiblings(
    final TreeItem<T> item ) {
    final TreeItem<T> parent = item.getParent();
    return parent == null ? item.getChildren() : parent.getChildren();
  }

  private <T> TreeItem<T> getFirstSibling( final TreeItem<T> item ) {
    return getFirst( getSiblings( item ), item );
  }

  private <T> TreeItem<T> getLastSibling( final TreeItem<T> item ) {
    return getLast( getSiblings( item ), item );
  }

  /**
   * Returns the caret's offset into the current paragraph.
   *
   * @return
   */
  private int getCurrentCaretColumn() {
    return getEditor().getCaretColumn();
  }

  /**
   * Returns all the characters from the initial caret column to the the first
   * whitespace character. This will return a path that contains zero or more
   * separators.
   *
   * @return A non-null string, possibly empty.
   */
  private String getCurrentWord() {
    final String s = globText();

    int i = 0;

    while( i < s.length() && !Character.isWhitespace( s.charAt( i ) ) ) {
      i++;
    }

    return s.substring( 0, i );
  }

  /**
   * Returns a swath of text from the initial caret position until .
   *
   * @return
   */
  private String globText() {
    final StyledTextArea textArea = getEditor();
    final int textBegan = getInitialCaretPosition();
    final int remaining = textArea.getLength() - textBegan;
    final int textEnded = Math.min( remaining, getMaxVarLength() );

    return textArea.getText( textBegan, textEnded );
  }

  /**
   * Finds the node that most closely matches the given path.
   *
   * @param path The path that represents a node.
   *
   * @return The node for the path, or the root node if the path could not be
   * found, but never null.
   */
  private TreeItem<String> findNode( final String path ) {
    return getDefinitionPane().findNode( path );
  }

  /**
   * Used to ignore typed keys in favour of trapping pressed keys.
   *
   * @param e The key that was typed.
   */
  private void vModeKeyTyped( KeyEvent e ) {
    e.consume();
  }

  /**
   * Used to lazily initialize the keyboard map.
   *
   * @return Mappings for keyTyped and keyPressed.
   */
  protected InputMap<InputEvent> createKeyboardMap() {
    return sequence(
      consume( keyTyped(), this::vModeKeyTyped ),
      consume( keyPressed(), this::vModeKeyPressed )
    );
  }

  private InputMap<InputEvent> getKeyboardMap() {
    if( this.keyboardMap == null ) {
      this.keyboardMap = createKeyboardMap();
    }

    return this.keyboardMap;
  }

  private void expand( final TreeItem<String> node ) {
    final DefinitionPane pane = getDefinitionPane();
    pane.collapse();
    pane.expand( node );
    pane.select( node );
  }

  /**
   * Trap the AT key for inserting YAML variables.
   */
  private void initKeyboardEventListeners() {
    addEventListener( keyPressed( DIGIT2, SHIFT_DOWN ), this::atPressed );
  }

  /**
   * Returns true iff the key code the user typed can be used as part of a YAML
   * variable name.
   *
   * @param keyEvent Keyboard key press event information.
   *
   * @return true The key is a value that can be inserted into the text.
   */
  private boolean isVariableNameKey( final KeyEvent keyEvent ) {
    final KeyCode kc = keyEvent.getCode();

    return (kc.isLetterKey()
      || kc.isDigitKey()
      || kc == PERIOD
      || (keyEvent.isShiftDown() && kc == MINUS))
      && !keyEvent.isControlDown();
  }

  /**
   * Starts to capture user input events.
   */
  private void startEventCapture() {
    addEventListener( getKeyboardMap() );
  }

  /**
   * Restores capturing of user input events to the previous event listener.
   */
  private void stopEventCapture() {
    removeEventListener( getKeyboardMap() );
  }

  /**
   * Delegates to the file editor pane, and, ultimately, to its text area.
   */
  private <T extends Event, U extends T> void addEventListener(
    final EventPattern<? super T, ? extends U> event,
    final Consumer<? super U> consumer ) {
    getFileEditorPane().addEventListener( event, consumer );
  }

  /**
   * Delegates to the file editor pane, and, ultimately, to its text area.
   *
   * @param map The map of methods to events.
   */
  private void addEventListener( final InputMap<InputEvent> map ) {
    getFileEditorPane().addEventListener( map );
  }

  private void removeEventListener( final InputMap<InputEvent> map ) {
    getFileEditorPane().removeEventListener( map );
  }

  /**
   * Returns the position of the caret when variable mode editing was requested.
   *
   * @return The variable mode caret position.
   */
  private int getInitialCaretPosition() {
    return this.initialCaretPosition;
  }

  /**
   * Sets the position of the caret when variable mode editing was requested.
   * Stores the current position because only the text that comes afterwards is
   * a suitable variable reference.
   *
   * @return The variable mode caret position.
   */
  private void setInitialCaretPosition() {
    this.initialCaretPosition = getEditor().getCaretPosition();
  }

  private StyledTextArea getEditor() {
    return getFileEditorPane().getEditor();
  }

  public FileEditorPane getFileEditorPane() {
    return this.fileEditorPane;
  }

  private void setFileEditorPane( final FileEditorPane fileEditorPane ) {
    this.fileEditorPane = fileEditorPane;
  }

  private DefinitionPane getDefinitionPane() {
    return this.definitionPane;
  }

  private void setDefinitionPane( final DefinitionPane definitionPane ) {
    this.definitionPane = definitionPane;
  }

  private IndexRange getSelectionRange() {
    return getEditor().getSelection();
  }

  /**
   * Don't look ahead too far when trying to find the end of a node.
   *
   * @return 512 by default.
   */
  private int getMaxVarLength() {
    return getSettings().getSetting( "editor.variable.maxLength", 512 );
  }

  private Settings getSettings() {
    return this.settings;
  }
}
