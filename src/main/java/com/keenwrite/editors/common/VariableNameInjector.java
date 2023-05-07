/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.editors.common;

import com.keenwrite.editors.TextDefinition;
import com.keenwrite.editors.TextEditor;
import com.keenwrite.editors.definition.DefinitionTreeItem;
import com.keenwrite.io.MediaType;
import com.keenwrite.preferences.Key;
import com.keenwrite.preferences.Workspace;
import com.keenwrite.processors.r.RInlineEvaluator;
import com.keenwrite.sigils.PropertyKeyOperator;
import com.keenwrite.sigils.RKeyOperator;

import java.util.function.UnaryOperator;

import static com.keenwrite.constants.Constants.*;
import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.preferences.AppKeys.*;

/**
 * Provides the logic for injecting variable names within the editor.
 */
public final class VariableNameInjector {
  private final Workspace mWorkspace;

  public VariableNameInjector( final Workspace workspace ) {
    assert workspace != null;

    mWorkspace = workspace;
  }

  /**
   * Find a node that matches the current word and substitute the definition
   * reference. After calling this method the document being edited will have
   * the word under the caret replaced with a corresponding variable name
   * bracketed by sigils according to the document's media type.
   *
   * @param editor      The editor having a caret and a word under that caret.
   * @param definitions The list of variable definitions to search for a value
   *                    that matches the word under the caret.
   */
  public void autoinsert(
    final TextEditor editor,
    final TextDefinition definitions ) {
    assert editor != null;
    assert definitions != null;

    try {
      if( definitions.isEmpty() ) {
        clue( STATUS_DEFINITION_EMPTY );
      }
      else {
        final var indexes = editor.getCaretWord();
        final var word = editor.getText( indexes );

        if( word.isBlank() ) {
          clue( STATUS_DEFINITION_BLANK );
        }
        else {
          final var leaf = findLeaf( definitions, word );

          if( leaf == null ) {
            clue( STATUS_DEFINITION_MISSING, word );
          }
          else {
            insert( editor, leaf );
            definitions.expand( leaf );
          }
        }
      }
    } catch( final Exception ex ) {
      clue( STATUS_DEFINITION_BLANK, ex );
    }
  }

  public void insert(
    final TextEditor editor,
    final DefinitionTreeItem<String> leaf ) {
    assert editor != null;
    assert leaf != null;

    final var mediaType = editor.getMediaType();
    final var operator = createOperator( mediaType );
    final var indexes = editor.getCaretWord();

    editor.replaceText( indexes, operator.apply( leaf.toPath() ) );
  }

  /**
   * Creates an instance of {@link UnaryOperator} that can wrap a value with
   * sigils.
   *
   * @param mediaType The type of document with variables to sigilize.
   * @return An operator that produces sigilized variable names.
   */
  private UnaryOperator<String> createOperator( final MediaType mediaType ) {
    final String began;
    final String ended;
    final UnaryOperator<String> operator;

    switch( mediaType ) {
      case TEXT_MARKDOWN -> {
        began = getString( KEY_DEF_DELIM_BEGAN );
        ended = getString( KEY_DEF_DELIM_ENDED );
        operator = s -> s;
      }
      case TEXT_R_MARKDOWN -> {
        began = RInlineEvaluator.PREFIX + getString( KEY_R_DELIM_BEGAN );
        ended = getString( KEY_R_DELIM_ENDED ) + RInlineEvaluator.SUFFIX;
        operator = new RKeyOperator();
      }
      case TEXT_PROPERTIES -> {
        began = PropertyKeyOperator.BEGAN;
        ended = PropertyKeyOperator.ENDED;
        operator = s -> s;
      }
      default -> {
        began = "";
        ended = "";
        operator = s -> s;
      }
    }

    return s -> began + operator.apply( s ) + ended;
  }

  private String getString( final Key key ) {
    assert key != null;

    return mWorkspace.getString( key );
  }

  /**
   * Looks for the given word, matching first by exact, next by a starts-with
   * condition with diacritics replaced, then by containment.
   *
   * @param word Match the word by: exact, beginning, containment, or other.
   */
  @SuppressWarnings( "ConstantConditions" )
  private static DefinitionTreeItem<String> findLeaf(
    final TextDefinition definition, final String word ) {
    assert definition != null;
    assert word != null;

    DefinitionTreeItem<String> leaf;

    leaf = definition.findLeafExact( word );
    leaf = leaf == null ? definition.findLeafStartsWith( word ) : leaf;
    leaf = leaf == null ? definition.findLeafContains( word ) : leaf;
    leaf = leaf == null ? definition.findLeafContainsNoCase( word ) : leaf;

    return leaf;
  }
}
