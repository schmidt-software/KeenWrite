package com.keenwrite;

import com.panemu.tiwulfx.control.dock.DetachableTabPane;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

public class DefinitionTabSceneFactory {

	private List<DefinitionTabContainer> lstTabContainer = new ArrayList<>();
	private Consumer<Tab> onTabSelected;

	public void setOnTabSelected(Consumer<Tab> onTabSelected) {
		this.onTabSelected = onTabSelected;
	}

	public Scene create(DetachableTabPane tabpane) {
		DefinitionTabContainer con = new DefinitionTabContainer(tabpane, onTabSelected);
		lstTabContainer.add(con);
		Scene scene = new Scene(con, 900, 500);

		scene.windowProperty().addListener((ObservableValue<? extends Window> ov, Window t, final Window newWindow) -> {
			if (newWindow != null) {
				newWindow.setOnHidden((WindowEvent t1) -> {
					lstTabContainer.remove(con);
				});
				newWindow.focusedProperty().addListener((o) -> {
					if (con.getSelectedTab() != null) {
						onTabSelected.accept(con.getSelectedTab());
					}
				});
			}
		});

		return scene;
	}

	private static class DefinitionTabContainer extends VBox {

		private DetachableTabPane containerTabPane;

		public DefinitionTabContainer(DetachableTabPane containerTabPane, Consumer<Tab> onTabSelected) {
			this.containerTabPane = containerTabPane;
			VBox.setVgrow(containerTabPane, Priority.ALWAYS);
			getChildren().add(containerTabPane);
			containerTabPane.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Tab> ov, Tab t, Tab t1) -> {
				onTabSelected.accept(t1);
			});
		}

		public Tab getSelectedTab() {
			return containerTabPane.getSelectionModel().getSelectedItem();
		}
	}

}
