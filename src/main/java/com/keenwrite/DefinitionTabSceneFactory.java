/* Copyright 2020 White Magic Software, Ltd.
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
import javafx.scene.Scene;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

import static javafx.scene.layout.Priority.ALWAYS;

/**
 * Responsible for delegating tab selection events to a consumer. This is
 * required so that when a tab is detached from the main view into its own
 * window (scene), any tab changes in that scene can have an effect on the
 * main view.
 *
 * @author Amrullah Syadzili
 * @author White Magic Software, Ltd.
 */
public class DefinitionTabSceneFactory {

  private final Consumer<Tab> mTabSelectionConsumer;

  public DefinitionTabSceneFactory( final Consumer<Tab> tabSelectionConsumer ) {
    mTabSelectionConsumer = tabSelectionConsumer;
  }

  public Scene create( final DetachableTabPane tabPane ) {
    final var container = new TabContainer( tabPane );
    final var scene = new Scene( container, 300, 900 );

    scene.windowProperty().addListener( ( c, o, n ) -> {
      if( n != null ) {
        n.focusedProperty().addListener( ( __ ) -> {
          final var tab = container.getSelectedTab();

          if( tab != null ) {
            mTabSelectionConsumer.accept( tab );
          }
        } );
      }
    } );

    return scene;
  }

  private final class TabContainer extends VBox {
    private final DetachableTabPane mTabPane;

    public TabContainer( final DetachableTabPane tabPane ) {
      mTabPane = tabPane;
      setVgrow( tabPane, ALWAYS );
      getChildren().add( tabPane );

      selectedItemProperty().addListener(
          ( c, o, n ) -> {
            if( n != null ) {
              mTabSelectionConsumer.accept( n );
            }
          }
      );
    }

    private SingleSelectionModel<Tab> getSelectionModel() {
      return mTabPane.getSelectionModel();
    }

    private ReadOnlyObjectProperty<Tab> selectedItemProperty() {
      return getSelectionModel().selectedItemProperty();
    }

    private Tab getSelectedTab() {
      return getSelectionModel().getSelectedItem();
    }
  }
}
