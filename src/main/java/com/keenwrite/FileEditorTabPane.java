/*
 * Copyright 2020 Karl Tauber and White Magic Software, Ltd.
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
package com.keenwrite;

import com.panemu.tiwulfx.control.dock.DetachableTabPane;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Tab;

/**
 * Tab pane for file editors.
 */
public final class FileEditorTabPane extends DetachableTabPane {

  private final ReadOnlyObjectWrapper<FileEditorController> mActiveFileEditor =
      new ReadOnlyObjectWrapper<>();

  public FileEditorTabPane( ) {
  }

  /**
   * Allows observers to be notified when the current file editor tab changes.
   *
   * @param listener The listener to notify of tab change events.
   */
  public void addTabSelectionListener( final ChangeListener<Tab> listener ) {
    // Observe the tab so that when a new tab is opened or selected,
    // a notification is kicked off.
    getSelectionModel().selectedItemProperty().addListener( listener );
  }

  /**
   * Returns the tab that has keyboard focus.
   *
   * @return A non-null instance.
   */
  public FileEditorController getActiveFileEditor() {
    return mActiveFileEditor.get();
  }

  /**
   * Returns the property corresponding to the tab that has focus.
   *
   * @return A non-null instance.
   */
  public ReadOnlyObjectProperty<FileEditorController> activeFileEditorProperty() {
    return mActiveFileEditor.getReadOnlyProperty();
  }

}
