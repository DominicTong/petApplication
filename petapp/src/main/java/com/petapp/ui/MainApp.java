package com.petapp.ui;

import com.petapp.DatabaseManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

/**
 * JavaFX entry point.
 *
 * Creates a 900×650 window with three tabs:
 *   [0] 📋 Tasks  —  to-do list + coin rewards
 *   [1] 🌿 Garden —  collected animals (default / centre tab)
 *   [2] 🛒 Shop   —  buy Animal Packs
 *
 * Each tab implements a {@code refresh()} method that reloads its data
 * from MySQL whenever the tab is brought into focus, keeping all
 * displays in sync without a shared event bus.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {

        // Initialise the database connection early so any config errors
        // surface immediately rather than mid-session.
        DatabaseManager.getInstance();

        // ── Build tabs ────────────────────────────────────────────────────────
        TasksTab  tasksController  = new TasksTab();
        GardenTab gardenController = new GardenTab();
        ShopTab   shopController   = new ShopTab();

        Tab tasksTab  = new Tab("📋  Tasks",  tasksController.getContent());
        Tab gardenTab = new Tab("🌿  Garden", gardenController.getContent());
        Tab shopTab   = new Tab("🛒  Shop",   shopController.getContent());

        tasksTab.setClosable(false);
        gardenTab.setClosable(false);
        shopTab.setClosable(false);

        // ── TabPane ───────────────────────────────────────────────────────────
        TabPane tabPane = new TabPane(tasksTab, gardenTab, shopTab);
        tabPane.setTabMinWidth(110);

        // Refresh whichever tab is selected
        tabPane.getSelectionModel().selectedItemProperty().addListener(
                (obs, prev, next) -> {
                    if (next == tasksTab)  tasksController.refresh();
                    else if (next == gardenTab) gardenController.refresh();
                    else if (next == shopTab)   shopController.refresh();
                });

        // Default to the Garden (centre) tab
        tabPane.getSelectionModel().select(gardenTab);

        // ── Scene & Stage ─────────────────────────────────────────────────────
        Scene scene = new Scene(tabPane, 900, 660);
        scene.getStylesheets().add(
                getClass().getResource("/styles.css").toExternalForm());

        primaryStage.setTitle("Pet Garden 🌸");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(820);
        primaryStage.setMinHeight(580);
        primaryStage.show();
    }
}
