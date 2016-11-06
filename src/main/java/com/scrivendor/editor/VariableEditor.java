/*
 * Copyright 2016 White Magic Software, Inc.
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
import com.scrivendor.definition.DefinitionPane;
import static com.scrivendor.definition.Lists.getFirst;
import static com.scrivendor.definition.Lists.getLast;
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

  /**
   * Used to capture keyboard events once the user presses @.
   */
  private InputMap<InputEvent> keyboardMap;

  private FileEditorPane fileEditorPane;
  private DefinitionPane definitionPane;

  /**
   * Position of the variable in the text when in variable mode.
   */
  private int initialCaretColumn = 0;

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
    setInitialCaretColumn();
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
        if( getCurrentCaretColumn() > getInitialCaretColumn() ) {
          break;
        }

      case ESCAPE:
        stopEventCapture();
        break;

      case PERIOD:
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
        }

        break;
    }

    e.consume();
  }

  /**
   * Inserts the string at the current caret position, replacing any selected
   * text.
   *
   * @param text The text to insert, never null.
   */
  private void updateEditorText( final String text ) {
    final StyledTextArea t = getEditor();

    System.out.println( "----------" );
    System.out.println( "updateText" );

    final int length = text.length();
    final int posBegan = t.getCaretPosition();
    final int posEnded = posBegan + length;

    t.replaceSelection( text );

    if( posEnded - posBegan > 0 ) {
      t.selectRange( posBegan, posEnded );
    }

    System.out.println( "Inserted: '" + text + "'" );
    System.out.println( "Selected: '" + t.getSelectedText() + "'" );
  }
  
  /**
   * Updates the text with the path selected (or typed) by the user.
   */
  private void advancePath() {
    final String word = getCurrentWord();
    final TreeItem<String> node = findNearestNode( word );
    String text = node.getValue();

    System.out.println( "----------" );
    System.out.println( "advancePath" );

    if( !node.isLeaf() ) {
      System.out.println( "word = '" + word + "'" );
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
    final String path = getCurrentWord();
    final TreeItem<String> node = findNearestNode( path );

    System.out.println( "---------------" );
    System.out.println( "cycle selection" );
    System.out.println( "path = '" + path + "'" );

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
   * Returns the full text for the paragraph that contains the caret.
   *
   * @return
   */
  private String getCurrentParagraph() {
    final StyledTextArea textArea = getEditor();
    final int paragraph = textArea.getCurrentParagraph();
    return textArea.getText( paragraph );
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
    final String p = getCurrentParagraph();
    final String s = p.substring( getInitialCaretColumn() );

    int i = 0;

    while( i < s.length() && !Character.isWhitespace( s.charAt( i ) ) ) {
      i++;
    }

    final String word = p.substring( getInitialCaretColumn(), i );

    System.out.println( "Current word: '" + word + "'" );

    return word;
  }

  /**
   * Finds the node that most closely matches the given path.
   *
   * @param path The path that represents a node.
   *
   * @return The node for the path, or the root node if the path could not be
   * found, but never null.
   */
  private TreeItem<String> findNearestNode( final String path ) {
    return getDefinitionPane().findNearestNode( path );
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

  private <T> void expand( final TreeItem<T> node ) {
    final DefinitionPane pane = getDefinitionPane();
    pane.collapse();
    pane.expand( node );
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
  private boolean isVariableNameKey( KeyEvent keyEvent ) {
    final KeyCode keyCode = keyEvent.getCode();

    return keyCode.isLetterKey()
      || keyCode.isDigitKey()
      || keyCode == PERIOD
      || (keyEvent.isShiftDown() && keyCode == MINUS);
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
  private int getInitialCaretColumn() {
    return this.initialCaretColumn;
  }

  /**
   * Sets the position of the caret when variable mode editing was requested.
   * Stores the current position because only the text that comes afterwards is
   * a suitable variable reference.
   *
   * @return The variable mode caret position.
   */
  private void setInitialCaretColumn() {
    this.initialCaretColumn = getEditor().getCaretColumn();
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
}
