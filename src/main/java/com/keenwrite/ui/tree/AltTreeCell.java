/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.ui.tree;

import com.keenwrite.ui.common.CellEditor;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.util.StringConverter;

/**
 * Responsible for enhancing the existing cell behaviour with fairly common
 * functionality, including commit on focus loss and Enter to commit.
 *
 * @param <T> The type of data stored by the tree.
 */
public class AltTreeCell<T> extends TextFieldTreeCell<T> {
  public AltTreeCell( final StringConverter<T> converter ) {
    super( converter );

    assert converter != null;

    new CellEditor(
      input -> commitEdit( getConverter().fromString( input ) ),
      graphicProperty()
    );
  }
}
