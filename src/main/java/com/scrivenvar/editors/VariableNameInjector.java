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
package com.scrivenvar.editors;

import com.scrivenvar.FileEditorTab;
import com.scrivenvar.decorators.VariableDecorator;
import com.scrivenvar.definition.DefinitionPane;
import com.scrivenvar.definition.FindMode;
import com.scrivenvar.definition.VariableTreeItem;
import javafx.event.Event;
import javafx.scene.control.TreeItem;
import javafx.scene.input.KeyEvent;
import org.fxmisc.richtext.StyledTextArea;
import org.fxmisc.wellbehaved.event.EventPattern;

import java.nio.file.Path;
import java.text.BreakIterator;
import java.util.function.Consumer;

import static com.scrivenvar.definition.FindMode.*;
import static java.lang.Character.isWhitespace;
import static javafx.scene.input.KeyCode.SPACE;
import static javafx.scene.input.KeyCombination.CONTROL_DOWN;
import static org.fxmisc.wellbehaved.event.EventPattern.keyPressed;

/**
 * Provides the logic for injecting variable names within the editor.
 *
 * @author White Magic Software, Ltd.
 */
public final class VariableNameInjector {

  public static final int DEFAULT_MAX_VAR_LENGTH = 64;

  /**
   * TODO: Move this into settings.
   */
  private static final String PUNCTUATION = "\"#$%&'()*+,-/:;<=>?@[]^_`{|}~";

  /**
   * Recipient of name injections.
   */
  private FileEditorTab tab;

  /**
   * Initiates double-click events.
   */
  private DefinitionPane definitionPane;

  /**
   * Initializes the variable name injector against the given pane.
   *
   * @param tab  The tab to inject variable names into.
   * @param pane The definition panel to listen to for double-click events.
   */
  public VariableNameInjector(
      final FileEditorTab tab, final DefinitionPane pane ) {
    setFileEditorTab( tab );
    setDefinitionPane( pane );
    initKeyboardEventListeners();
  }

  /**
   * Trap control+space and the @ key.
   *
   * @param tab The file editor that sends keyboard events for variable name
   *            injection.
   */
  public void initKeyboardEventListeners( final FileEditorTab tab ) {
    setFileEditorTab( tab );
    initKeyboardEventListeners();
  }

  /**
   * Traps keys for performing various short-cut tasks, such as @-mode variable
   * insertion and control+space for variable autocomplete.
   *
   * @ key is pressed, a new keyboard map is inserted in place of the current
   * map -- this class goes into "variable edit mode" (a.k.a. vMode).
   */
  private void initKeyboardEventListeners() {
    // Control and space are pressed.
    addKeyboardListener( keyPressed( SPACE, CONTROL_DOWN ),
                         this::autocomplete );
  }

  /**
   * Pressing control+space will find a node that matches the current word and
   * substitute the YAML variable reference. This is called when the user is not
   * editing in vMode.
   *
   * @param e Ignored -- it can only be Ctrl+Space.
   */
  private void autocomplete( final KeyEvent e ) {
    final String paragraph = getCaretParagraph();
    final int[] boundaries = getWordBoundariesAtCaret();
    final String word = paragraph.substring( boundaries[ 0 ], boundaries[ 1 ] );

    VariableTreeItem<String> leaf = findLeafStartsWith( word );

    if( leaf == null ) {
      // If a leaf doesn't match using "starts with", then try using "contains".
      leaf = findLeafContains( word );
    }

    if( leaf == null ) {
      leaf = findLeafLevenshtein( word );
    }

    if( leaf != null ) {
      replaceText( boundaries[ 0 ], boundaries[ 1 ], leaf.toPath() );
      decorate();
      expand( leaf );
    }
  }

  private int[] getWordBoundariesAtCaret() {
    final String paragraph = getCaretParagraph();
    final int column = getCurrentCaretColumn();
    final int offset = column - (column == paragraph.length() ? 1 : 0);

    final BreakIterator wordBreaks = BreakIterator.getWordInstance();
    wordBreaks.setText( paragraph );

    final int[] boundaries = new int[ 2 ];
    boundaries[ 1 ] = wordBreaks.following( offset );
    boundaries[ 0 ] = wordBreaks.previous();

    return boundaries;
  }

  /**
   * Called when autocomplete finishes on a valid leaf or when the user presses
   * Enter to finish manual autocomplete.
   */
  private void decorate() {
    // A little bit of duplication...
    final String paragraph = getCaretParagraph();
    final int[] boundaries = getWordBoundaries( paragraph );
    final String old = paragraph.substring( boundaries[ 0 ], boundaries[ 1 ] );

    final String newVariable = decorate( old );

    final int posEnded = getCurrentCaretPosition();
    final int posBegan = posEnded - old.length();

    getEditor().replaceText( posBegan, posEnded, newVariable );
  }

  /**
   * Called when user double-clicks on a tree view item.
   *
   * @param variable The variable to decorate.
   */
  private String decorate( final String variable ) {
    return getVariableDecorator().decorate( variable );
  }

  /**
   * Updates the text at the given position within the current paragraph.
   *
   * @param posBegan The starting index in the paragraph text to replace.
   * @param posEnded The ending index in the paragraph text to replace.
   * @param text     Overwrite the paragraph substring with this text.
   */
  private void replaceText(
      final int posBegan, final int posEnded, final String text ) {
    final int p = getCurrentParagraph();

    getEditor().replaceText( p, posBegan, p, posEnded, text );
  }

  /**
   * Returns the caret's current paragraph position.
   *
   * @return A number greater than or equal to 0.
   */
  private int getCurrentParagraph() {
    return getEditor().getCurrentParagraph();
  }

  /**
   * Returns current word boundary indexes into the current paragraph, excluding
   * punctuation.
   *
   * @param p      The paragraph wherein to hunt word boundaries.
   * @param offset The offset into the paragraph to begin scanning left and
   *               right.
   * @return The starting and ending index of the word closest to the caret.
   */
  private int[] getWordBoundaries( final String p, final int offset ) {
    // Remove dashes, but retain hyphens. Retain same number of characters
    // to preserve relative indexes.
    final String paragraph = p.replace( "---", "   " ).replace( "--", "  " );

    return getWordAt( paragraph, offset );
  }

  /**
   * Helper method to get the word boundaries for the current paragraph.
   *
   * @param paragraph The paragraph to search for word boundaries.
   * @return The word boundary indexes into the paragraph.
   */
  private int[] getWordBoundaries( final String paragraph ) {
    return getWordBoundaries( paragraph, getCurrentCaretColumn() );
  }

  /**
   * Given an arbitrary offset into a string, this returns the word at that
   * index. The inputs and outputs include:
   *
   * <ul>
   * <li>surrounded by space: <code>hello | world!</code> ("");</li>
   * <li>end of word: <code>hello| world!</code> ("hello");</li>
   * <li>start of a word: <code>hello |world!</code> ("world");</li>
   * <li>within a word: <code>hello wo|rld!</code> ("world");</li>
   * <li>end of a paragraph: <code>hello world!|</code> ("world");</li>
   * <li>start of a paragraph: <code>|hello world!</code> ("hello"); or</li>
   * <li>after punctuation: <code>hello world!|</code> ("world").</li>
   * </ul>
   *
   * @param p      The string to scan for a word.
   * @param offset The offset within s to begin searching for the nearest word
   *               boundary, must not be out of bounds of s.
   * @return The word in s at the offset.
   */
  private int[] getWordAt( final String p, final int offset ) {
    return new int[]{getWordBegan( p, offset ), getWordEnded( p, offset )};
  }

  /**
   * Returns the index into s where a word begins.
   *
   * @param s      Never null.
   * @param offset Index into s to begin searching backwards for a word
   *               boundary.
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
   * @param s      Never null.
   * @param offset Index into s to begin searching forwards for a word boundary.
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
   * @return false The character is a space character.
   */
  private boolean isBoundary( final char c ) {
    return !isWhitespace( c ) && !isPunctuation( c );
  }

  /**
   * Returns true if the given character is part of the set of Latin (English)
   * punctuation marks.
   *
   * @param c The character to determine whether it is punctuation.
   * @return {@code true} when the given character is in the set of
   * {@link #PUNCTUATION}.
   */
  private static boolean isPunctuation( final char c ) {
    return PUNCTUATION.indexOf( c ) != -1;
  }

  /**
   * Returns the text for the paragraph that contains the caret.
   *
   * @return A non-null string, possibly empty.
   */
  private String getCaretParagraph() {
    return getEditor().getText( getCurrentParagraph() );
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

  private VariableTreeItem<String> findLeafContains( final String text ) {
    return findLeaf( text, CONTAINS );
  }

  private VariableTreeItem<String> findLeafStartsWith( final String text ) {
    return findLeaf( text, STARTS_WITH );
  }

  private VariableTreeItem<String> findLeafLevenshtein( final String text ) {
    return findLeaf( text, LEVENSHTEIN );
  }

  /**
   * Finds the first leaf having a value that starts with the given text, or
   * contains the text if contains is true.
   *
   * @param text     The text to find in the definition tree.
   * @param findMode Dictates what search criteria to use for matching words.
   * @return The leaf that starts with the given text, or null if not found.
   */
  private VariableTreeItem<String> findLeaf(
      final String text, final FindMode findMode ) {
    return getDefinitionPane().findLeaf( text, findMode );
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
   * @return A variable decorator that corresponds to the given file type.
   */
  private VariableDecorator getVariableDecorator() {
    return VariableNameDecoratorFactory.newInstance( getFilename() );
  }

  private Path getFilename() {
    return getFileEditorTab().getPath();
  }

  private EditorPane getEditorPane() {
    return getFileEditorTab().getEditorPane();
  }

  /**
   * Delegates to the file editor pane, and, ultimately, to its text area.
   */
  private <T extends Event, U extends T> void addKeyboardListener(
      final EventPattern<? super T, ? extends U> event,
      final Consumer<? super U> consumer ) {
    getEditorPane().addKeyboardListener( event, consumer );
  }

  private StyledTextArea<?, ?> getEditor() {
    return getEditorPane().getEditor();
  }

  public FileEditorTab getFileEditorTab() {
    return this.tab;
  }

  public void setFileEditorTab( final FileEditorTab editorTab ) {
    this.tab = editorTab;
  }

  private DefinitionPane getDefinitionPane() {
    return this.definitionPane;
  }

  private void setDefinitionPane( final DefinitionPane definitionPane ) {
    this.definitionPane = definitionPane;
  }

}
