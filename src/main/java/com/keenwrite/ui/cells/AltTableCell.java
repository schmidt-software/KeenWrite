package com.keenwrite.ui.cells;

import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;

public class AltTableCell<S, T> extends TextFieldTableCell<S, T> {
  public AltTableCell( final StringConverter<T> converter ) {
    super( converter );

    assert converter != null;

    new CellEditor(
      input -> commitEdit( getConverter().fromString( input ) ),
      graphicProperty()
    );
  }
}
