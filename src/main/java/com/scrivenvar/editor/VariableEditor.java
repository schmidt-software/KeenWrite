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
package com.scrivenvar.editor;

import com.scrivenvar.FileEditorPane;
import com.scrivenvar.Services;
import com.scrivenvar.definition.DefinitionPane;
import static com.scrivenvar.definition.DefinitionPane.SEPARATOR;
import static com.scrivenvar.definition.Lists.getFirst;
import static com.scrivenvar.definition.Lists.getLast;
import com.scrivenvar.service.Settings;
import static java.lang.Character.isSpaceChar;
import static java.lang.Character.isWhitespace;
import static java.lang.Math.min;
import java.util.Stack;
import java.util.function.Consumer;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.scene.control.IndexRange;
import javafx.scene.control.TreeItem;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import static javafx.scene.input.KeyCode.AT;
import static javafx.scene.input.KeyCode.DIGIT2;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.MINUS;
import static javafx.scene.input.KeyCode.SPACE;
import static javafx.scene.input.KeyCombination.CONTROL_DOWN;
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
  
  private static final int NO_DIFFERENCE = -1;
  
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
   * Traps keys for performing various short-cut tasks, such as @-mode variable
   * insertion and control+space for variable autocomplete.
   *
   * @ key is pressed, a new keyboard map is inserted in place of the current
   * map -- this class goes into "variable edit mode" (a.k.a. vMode).
   *
   * @see createKeyboardMap()
   */
  private void initKeyboardEventListeners() {
    addEventListener( keyPressed( SPACE, CONTROL_DOWN ), this::autocomplete );

    // @ key in Linux?
    addEventListener( keyPressed( DIGIT2, SHIFT_DOWN ), this::vMode );
    // @ key in Windows.
    addEventListener( keyPressed( AT ), this::vMode );
  }

  /**
   * The @ symbol is a short-cut to inserting a YAML variable reference.
   *
   * @param e Superfluous information about the key that was pressed.
   */
  private void vMode( KeyEvent e ) {
    setInitialCaretPosition();
    vModeStart();
    vModeAutocomplete();
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
        vModeBackspace();
        break;
      
      case ESCAPE:
        vModeStop();
        break;
      
      case ENTER:
      case PERIOD:
      case RIGHT:
      case END:
        // Stop at a leaf node, ENTER means accept.
        if( vModeConditionalComplete() && keyCode == ENTER ) {
          vModeStop();
        }
        break;
      
      case UP:
        cyclePathPrev();
        break;
      
      case DOWN:
        cyclePathNext();
        break;
      
      default:
        vModeFilterKeyPressed( e );
        break;
    }
    
    e.consume();
  }
  
  private void vModeBackspace() {
    deleteSelection();

    // Break out of variable mode by back spacing to the original position.
    if( getCurrentCaretPosition() > getInitialCaretPosition() ) {
      vModeAutocomplete();
    } else {
      vModeStop();
    }
  }

  /**
   * Updates the text with the path selected (or typed) by the user.
   */
  private void vModeAutocomplete() {
    final TreeItem<String> node = getCurrentNode();
    
    if( !node.isLeaf() ) {
      final String word = getLastPathWord();
      final String label = node.getValue();
      final int delta = difference( label, word );
      final String remainder = delta == NO_DIFFERENCE
        ? label
        : label.substring( delta );
      
      final StyledTextArea textArea = getEditor();
      final int posBegan = getCurrentCaretPosition();
      final int posEnded = posBegan + remainder.length();
      
      textArea.replaceSelection( remainder );
      
      if( posEnded - posBegan > 0 ) {
        textArea.selectRange( posEnded, posBegan );
      }
      
      expand( node );
    }
  }

  /**
   * Only variable name keys can pass through the filter. This is called when
   * the user presses a key.
   *
   * @param e The key that was pressed.
   */
  private void vModeFilterKeyPressed( final KeyEvent e ) {
    if( isVariableNameKey( e ) ) {
      typed( e.getText() );
    }
  }

  /**
   * Performs an autocomplete depending on whether the user has finished typing
   * in a word. If there is a selected range, then this will complete the most
   * recent word and jump to the next child.
   *
   * @return true The auto-completed node was a terminal node.
   */
  private boolean vModeConditionalComplete() {
    acceptPath();
    
    final TreeItem<String> node = getCurrentNode();
    final boolean terminal = isTerminal( node );
    
    if( !terminal ) {
      typed( SEPARATOR );
    }
    
    return terminal;
  }

  /**
   * Pressing control+space will find a node that matches the current word and
   * substitute the YAML variable reference. This is called when the user is not
   * editing in vMode.
   *
   * @param e Ignored -- it can only be Ctrl+Space.
   */
  private void autocomplete( KeyEvent e ) {
    final int caretPos = getCurrentCaretColumn();
    final String paragraph = getCaretParagraph();
    
    final int[] boundaries = getWordBoundaries( paragraph, caretPos );
    final String word = paragraph.substring( boundaries[ 0 ], boundaries[ 1 ] );
    
    final TreeItem<String> leaf = findLeaf( word );
    
    if( leaf != null ) {
      replaceText( boundaries[ 0 ], boundaries[ 1 ], toPath( leaf ) );
      expand( leaf );
    }
  }

  /**
   * Updates the text at the given position within the current paragraph.
   *
   * @param posBegan The starting index of the paragraph text to replace.
   * @param posEnded The ending index of the paragraph text to replace.
   * @param text Overwrite the paragraph substring with this text.
   */
  private void replaceText(
    final int posBegan, final int posEnded, final String text ) {
    final StyledTextArea textArea = getEditor();
    final int p = textArea.getCurrentParagraph();
    textArea.replaceText( p, posBegan, p, posEnded, text );
  }

  /**
   * Returns the path for a node, with nodes made distinct using the separator
   * character. This is the antithesis of the findExactNode method. This uses
   * two loops: one for pushing nodes onto a stack and one for popping them
   * off to create the path in desired order.
   *
   * @param node The tree item to path into a string, must not be null.
   *
   * @return A non-null string, possibly empty.
   */
  public String toPath( TreeItem<String> node ) {
    final Stack<TreeItem<String>> stack = new Stack<>();
    
    while( node.getParent() != null ) {
      stack.push( node );
      node = node.getParent();
    }
    
    final StringBuilder sb = new StringBuilder( getMaxVarLength() );
    
    while( !stack.isEmpty() ) {
      node = stack.pop();
      
      if( !node.isLeaf() ) {
        sb.append( node.getValue() );
        
        // This will add a superfluous separator, but instead of peeking at
        // the stack all the time, the last separator will be removed outside
        // the loop.
        sb.append( SEPARATOR );
      }
    }

    // Remove the trailing SEPARATOR.
    if( sb.length() > 0 ) {
      sb.setLength( sb.length() - 1 );
    }
    
    return sb.toString();
  }

  /**
   * Returns current word boundary indexes into the current paragraph, including
   * punctuation.
   *
   * @param paragraph The paragraph
   *
   * @return The starting and ending index of the word closest to the caret.
   */
  private int[] getWordBoundaries( String paragraph, final int offset ) {
    // Remove dashes, but retain hyphens. Retain same number of characters
    // to preserve relative indexes.
    paragraph = paragraph.replace( "---", "   " ).replace( "--", "  " );
    
    final int posBegan = getWordBegan( paragraph, offset );
    final int posEnded = getWordEnded( paragraph, offset );
    
    return new int[]{ posBegan, posEnded };
  }

  /**
   * Given an arbitrary offset into a string, this returns the word at that
   * index. The inputs and outputs include:
   *
   * <ul>
   * <li>surrounded by space: <code>hello | world!</code> ("");</li>
   * <li>end of word: <code>hello| world!</code> ("hello");</li>
   * <li>start of a word: <code>hello |world!</code> ("world!");</li>
   * <li>within a word: <code>hello wo|rld!</code> ("world!");</li>
   * <li>end of a paragraph: <code>hello world!|</code> ("world!");</li>
   * <li>start of a paragraph: <code>|hello world!</code> ("hello!"); or</li>
   * <li>after punctuation: <code>hello world!|</code> ("world!").</li>
   * </ul>
   *
   * @param s The string to scan for a word.
   * @param offset The offset within s to begin searching for the nearest word
   * boundary, must not be out of bounds of s.
   *
   * @return The word in s at the offset.
   *
   * @see getWordBegan( String, int )
   * @see getWordEnded( String, int )
   */
  private String getWordAt( final String s, final int offset ) {
    final int posBegan = getWordBegan( s, offset );
    final int posEnded = getWordEnded( s, offset );
    
    return s.substring( posBegan, posEnded );
  }

  /**
   * Returns the index into s where a word begins.
   *
   * @param s Never null.
   * @param offset Index into s to begin searching backwards for a word
   * boundary.
   *
   * @return The index where a word begins.
   */
  private int getWordBegan( final String s, int offset ) {
    while( offset > 0 && isBoundary( s.charAt( offset - 1 ) ) ) {
      offset--;
    }
    
    return offset;
  }

  /**
   * Returns the index into s where a word ends.
   *
   * @param s Never null.
   * @param offset Index into s to begin searching forwards for a word boundary.
   *
   * @return The index where a word ends.
   */
  private int getWordEnded( final String s, int offset ) {
    final int length = s.length();
    
    while( offset < length && isBoundary( s.charAt( offset ) ) ) {
      offset++;
    }
    
    return offset;
  }

  /**
   * Returns true if the given character can be reasonably expected to be part
   * of a word, including punctuation marks.
   *
   * @param c The character to compare.
   *
   * @return false The character is a space character.
   */
  private boolean isBoundary( final char c ) {
    return !isSpaceChar( c );
  }

  /**
   * Returns the text for the paragraph that contains the caret.
   *
   * @return A non-null string, possibly empty.
   */
  private String getCaretParagraph() {
    final StyledTextArea textArea = getEditor();
    return textArea.getText( textArea.getCurrentParagraph() );
  }

  /**
   * Returns true if the node has children that can be selected (i.e., any
   * non-leaves).
   *
   * @param <T> The type that the TreeItem contains.
   * @param node The node to test for terminality.
   *
   * @return true The node has one branch and its a leaf.
   */
  private <T> boolean isTerminal( final TreeItem<T> node ) {
    final ObservableList<TreeItem<T>> branches = node.getChildren();
    
    return branches.size() == 1 && branches.get( 0 ).isLeaf();
  }

  /**
   * Inserts text that the user typed at the current caret position, then
   * performs an autocomplete for the variable name.
   *
   * @param text The text to insert, never null.
   */
  private void typed( final String text ) {
    getEditor().replaceSelection( text );
    vModeAutocomplete();
  }

  /**
   * Called when the user presses either End or Enter key.
   */
  private void acceptPath() {
    final IndexRange range = getSelectionRange();
    
    if( range != null ) {
      final int rangeEnd = range.getEnd();
      final StyledTextArea textArea = getEditor();
      textArea.deselect();
      textArea.moveTo( rangeEnd );
    }
  }

  /**
   * Replaces the entirety of the existing path (from the initial caret
   * position) with the given path.
   *
   * @param oldPath The path to replace.
   * @param newPath The replacement path.
   */
  private void replacePath( final String oldPath, final String newPath ) {
    final StyledTextArea textArea = getEditor();
    final int posBegan = getInitialCaretPosition();
    final int posEnded = posBegan + oldPath.length();
    
    textArea.deselect();
    textArea.replaceText( posBegan, posEnded, newPath );
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
    final TreeItem<String> node = getCurrentNode();

    // Find the sibling for the current selection and replace the current
    // selection with the sibling's value
    TreeItem< String> cycled = direction
      ? node.nextSibling()
      : node.previousSibling();

    // When cycling at the end (or beginning) of the list, jump to the first
    // (or last) sibling depending on the cycle direction.
    if( cycled == null ) {
      cycled = direction ? getFirstSibling( node ) : getLastSibling( node );
    }
    
    final String path = getCurrentPath();
    final String cycledWord = cycled.getValue();
    final String word = getLastPathWord();
    final int index = path.indexOf( word );
    final String cycledPath = path.substring( 0, index ) + cycledWord;
    
    expand( cycled );
    replacePath( path, cycledPath );
  }

  /**
   * Cycles to the next sibling of the currently selected tree node.
   */
  private void cyclePathNext() {
    cycleSelection( true );
  }

  /**
   * Cycles to the previous sibling of the currently selected tree node.
   */
  private void cyclePathPrev() {
    cycleSelection( false );
  }

  /**
   * Returns all the characters from the initial caret column to the the first
   * whitespace character. This will return a path that contains zero or more
   * separators.
   *
   * @return A non-null string, possibly empty.
   */
  private String getCurrentPath() {
    final String s = extractTextChunk();
    final int length = s.length();
    
    int i = 0;
    
    while( i < length && !isWhitespace( s.charAt( i ) ) ) {
      i++;
    }
    
    return s.substring( 0, i );
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
   * Returns the caret position as an offset into the text.
   *
   * @return A value from 0 to the length of the text (minus one).
   */
  private int getCurrentCaretPosition() {
    return getEditor().getCaretPosition();
  }

  /**
   * Returns the caret position within the current paragraph.
   *
   * @return A value from 0 to the length of the current paragraph.
   */
  private int getCurrentCaretColumn() {
    return getEditor().getCaretColumn();
  }

  /**
   * Returns the last word from the path.
   *
   * @return The last token.
   */
  private String getLastPathWord() {
    String path = getCurrentPath();
    
    int i = path.indexOf( SEPARATOR );
    
    while( i > 0 ) {
      path = path.substring( i + 1 );
      i = path.indexOf( SEPARATOR );
    }
    
    return path;
  }

  /**
   * Returns text from the initial caret position until some arbitrarily long
   * number of characters. The number of characters extracted will be
   * getMaxVarLength, or fewer, depending on how many characters remain to be
   * extracted. The result from this method is trimmed to the first whitespace
   * character.
   *
   * @return A chunk of text that includes all the words representing a path,
   * and then some.
   */
  private String extractTextChunk() {
    final StyledTextArea textArea = getEditor();
    final int textBegan = getInitialCaretPosition();
    final int remaining = textArea.getLength() - textBegan;
    final int textEnded = min( remaining, getMaxVarLength() );
    
    return textArea.getText( textBegan, textEnded );
  }

  /**
   * Returns the node for the current path.
   */
  private TreeItem<String> getCurrentNode() {
    return findNode( getCurrentPath() );
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
   * Finds the first leaf having a value that starts with the given text.
   *
   * @param text The text to find in the definition tree.
   *
   * @return The leaf that starts with the given text, or null if not found.
   */
  private TreeItem<String> findLeaf( final String text ) {
    return getDefinitionPane().findLeaf( text );
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

  /**
   * Collapses the tree then expands and selects the given node.
   *
   * @param node The node to expand.
   */
  private void expand( final TreeItem<String> node ) {
    final DefinitionPane pane = getDefinitionPane();
    pane.collapse();
    pane.expand( node );
    pane.select( node );
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
      || (keyEvent.isShiftDown() && kc == MINUS))
      && !keyEvent.isControlDown();
  }

  /**
   * Starts to capture user input events.
   */
  private void vModeStart() {
    addEventListener( getKeyboardMap() );
  }

  /**
   * Restores capturing of user input events to the previous event listener.
   */
  private void vModeStop() {
    removeEventListener( getKeyboardMap() );
  }

  /**
   * Returns the index where the two strings diverge.
   *
   * @param s1 The string that could be a substring of s2, null allowed.
   * @param s2 The string that could be a substring of s1, null allowed.
   *
   * @return NO_DIFFERENCE if the strings are the same, otherwise the index
   * where they differ.
   */
  @SuppressWarnings( "StringEquality" )
  private int difference( final CharSequence s1, final CharSequence s2 ) {
    if( s1 == s2 ) {
      return NO_DIFFERENCE;
    }
    
    if( s1 == null || s2 == null ) {
      return 0;
    }
    
    int i = 0;
    final int limit = min( s1.length(), s2.length() );
    
    while( i < limit && s1.charAt( i ) == s2.charAt( i ) ) {
      i++;
    }

    // If one string was shorter than the other, that's where they differ.
    return i;
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
