package com.keenwrite.ui.listeners;

import com.keenwrite.Caret;
import com.keenwrite.editors.TextEditor;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import static javafx.geometry.Pos.BASELINE_CENTER;

/**
 * Responsible for updating the UI whenever the caret changes position.
 * Only one instance of {@link CaretListener} is allowed, which prevents
 * duplicate adds to the observable property.
 */
public class CaretListener extends VBox implements ChangeListener<Integer> {

  /**
   * Use an instance of {@link Label} for its built-in CSS style class.
   */
  private final Label mLineNumberText = new Label();
  private volatile Caret mCaret;

  public CaretListener( final ReadOnlyObjectProperty<TextEditor> editor ) {
    assert editor != null;

    setAlignment( BASELINE_CENTER );
    getChildren().add( mLineNumberText );

    editor.addListener( ( c, o, n ) -> {
      if( n != null ) {
        updateListener( n.getCaret() );
      }
    } );

    updateListener( editor.get().getCaret() );
  }

  /**
   * Called whenever the caret position changes.
   *
   * @param c The caret position property.
   * @param o The old caret position offset.
   * @param n The new caret position offset.
   */
  @Override
  public void changed(
      final ObservableValue<? extends Integer> c,
      final Integer o, final Integer n ) {
    updateLineNumber();
  }

  private void updateListener( final Caret caret ) {
    assert caret != null;

    final var property = caret.textOffsetProperty();

    property.removeListener( this );
    mCaret = caret;
    property.addListener( this );
    updateLineNumber();
  }

  private void updateLineNumber() {
    mLineNumberText.setText( mCaret.toString() );
  }
}
