package com.keenwrite;

import com.panemu.tiwulfx.control.dock.DetachableTabPane;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.Scene;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public class DefinitionTabSceneFactory {

  private final Consumer<Tab> mTabSelectionConsumer;

  public DefinitionTabSceneFactory( final Consumer<Tab> tabSelectionConsumer ) {
    mTabSelectionConsumer = tabSelectionConsumer;
  }

  public Scene create( final DetachableTabPane tabPane ) {
    final var container = new DefinitionTabContainer(
        tabPane, mTabSelectionConsumer );
    final var scene = new Scene( container, 900, 500 );

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

  private static class DefinitionTabContainer extends VBox {

    private final DetachableTabPane mTabPane;

    public DefinitionTabContainer(
        final DetachableTabPane tabPane, final Consumer<Tab> tabConsumer ) {
      mTabPane = tabPane;
      setVgrow( tabPane, Priority.ALWAYS );
      getChildren().add( tabPane );

      selectedItemProperty().addListener(
          ( c, o, n ) -> tabConsumer.accept( n )
      );
    }

    private SingleSelectionModel<Tab> getSelectionModel() {
      return mTabPane.getSelectionModel();
    }

    private ReadOnlyObjectProperty<Tab> selectedItemProperty() {
      return getSelectionModel().selectedItemProperty();
    }

    public Tab getSelectedTab() {
      return getSelectionModel().getSelectedItem();
    }
  }
}
