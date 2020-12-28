package com.keenwrite.editors.markdown;

import com.keenwrite.preferences.Workspace;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;

import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith( ApplicationExtension.class )
public class MarkdownEditorTest {
  private static final String[] WORDS = new String[]{
    "Italicize",
    "English's",
    "foreign",
    "words",
    "based",
    "on",
    "popularity,",
    "like",
    "_bête_",
    "_noire_",
    "and",
    "_Weltanschauung_",
    "but",
    "not",
    "résumé.",
    "Don't",
    "omit",
    "accented",
    "characters!",
    "Cœlacanthe",
    "L'Haÿ-les-Roses",
    "Mühlfeldstraße",
    "Da̱nx̱a̱laga̱litła̱n",
  };

  private static final String TEXT = String.join( " ", WORDS );

  private static final Pattern REGEX = compile(
    "[^\\p{Mn}\\p{Me}\\p{L}\\p{N}'-]+" );

  /**
   * Test that the {@link MarkdownEditor} can retrieve a word at the caret
   * position, regardless of whether the caret is at the beginning, middle, or
   * end of the word.
   */
  @Test
  public void test_CaretWord_GetISO88591Word_WordSelected() {
    final var editor = createMarkdownEditor();

    for( int i = 0; i < WORDS.length; i++ ) {
      final var word = WORDS[ i ];
      final var len = word.length();
      final var expected = REGEX.matcher( word ).replaceAll( "" );

      for( int j = 0; j < len; j++ ) {
        editor.moveTo( offset( i ) + j );
        final var actual = editor.getCaretWordText();
        assertEquals( expected, actual );
      }
    }
  }

  /**
   * Test that the {@link MarkdownEditor} can make a word bold.
   */
  @Test
  public void test_CaretWord_SetWordBold_WordIsBold() {
    final var index = 20;
    final var editor = createMarkdownEditor();

    editor.moveTo( offset( index ) );
    editor.bold();
    assertTrue( editor.getText().contains( "**" + WORDS[ index ] + "**" ) );
  }

  /**
   * Returns the document offset for a string at the given index.
   */
  private static int offset( final int index ) {
    assert 0 <= index && index < WORDS.length;
    int offset = 0;

    for( int i = 0; i < index; i++ ) {
      offset += WORDS[ i ].length();
    }

    // Add the index to compensate for one space between words.
    return offset + index;
  }

  /**
   * Returns an instance of {@link MarkdownEditor} pre-populated with
   * {@link #TEXT}.
   *
   * @return A new {@link MarkdownEditor} instance, ready for unit tests.
   */
  private MarkdownEditor createMarkdownEditor() {
    final var workspace = new Workspace();
    final var editor = new MarkdownEditor( workspace );
    editor.setText( TEXT );
    return editor;
  }
}
