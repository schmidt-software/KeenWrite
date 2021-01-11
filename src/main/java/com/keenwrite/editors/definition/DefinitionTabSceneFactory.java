/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.editors.definition;

import com.panemu.tiwulfx.control.dock.DetachableTabPane;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.Scene;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
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
public final class DefinitionTabSceneFactory {

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
