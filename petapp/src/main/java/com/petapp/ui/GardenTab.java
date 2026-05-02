package com.petapp.ui;

import com.petapp.DatabaseManager;
import com.petapp.model.Animal;
import com.petapp.model.UserData;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;

import java.sql.SQLException;
import java.util.List;

/**
 * Garden tab — the player's animal sanctuary.
 *
 * Displays every species the player has collected as a card on a
 * grassy background, inspired by Neko Atsume.  Cards show:
 *   • The animal's emoji (large)
 *   • Its name
 *   • Its rarity (colour-coded)
 *   • How many copies the player owns (× N)
 *
 * Hovering a card shows the animal's description in a tooltip.
 */
public class GardenTab {

    private final VBox      root;
    private final FlowPane  animalGrid;
    private final Label     coinLabel;
    private final Label     collectionSummary;

    public GardenTab() {
        root = new VBox(16);
        root.setPadding(new Insets(28, 28, 16, 28));
        root.getStyleClass().add("tab-content");

        // ── Header row ────────────────────────────────────────────────────────
        Label title = new Label("🌿  My Garden");
        title.getStyleClass().add("page-title");

        coinLabel = new Label("🪙  0 coins");
        coinLabel.getStyleClass().add("coin-badge");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(12, title, spacer, coinLabel);
        header.setAlignment(Pos.CENTER_LEFT);

        collectionSummary = new Label("No animals yet");
        collectionSummary.getStyleClass().add("subtitle");

        // ── Garden grid (FlowPane tiles) ──────────────────────────────────────
        animalGrid = new FlowPane();
        animalGrid.setHgap(14);
        animalGrid.setVgap(14);
        animalGrid.setPadding(new Insets(20));
        animalGrid.getStyleClass().add("garden-area");
        animalGrid.setPrefWrapLength(800);

        ScrollPane scroll = new ScrollPane(animalGrid);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.getStyleClass().add("garden-scroll");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        root.getChildren().addAll(header, collectionSummary, scroll);
        refresh();
    }

    public Node getContent() { return root; }

    /** Pull fresh data from the database and repopulate the garden. */
    public void refresh() {
        try {
            UserData data = DatabaseManager.getInstance().getUserData();
            coinLabel.setText("🪙  " + data.getCoins() + " coins");

            List<Animal> collection = DatabaseManager.getInstance().getUserCollection();
            animalGrid.getChildren().clear();

            if (collection.isEmpty()) {
                showEmptyState();
                collectionSummary.setText("Your garden is empty — visit the Shop! 🛒");
            } else {
                // Rarity order already handled by the SQL query
                for (Animal animal : collection) {
                    animalGrid.getChildren().add(buildAnimalCard(animal));
                }
                int unique = collection.size();
                int total  = collection.stream().mapToInt(Animal::getCount).sum();
                collectionSummary.setText(
                        unique + " unique animal" + (unique == 1 ? "" : "s") +
                        " · " + total + " total");
            }
        } catch (SQLException e) {
            showError("Failed to load garden", e.getMessage());
        }
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private void showEmptyState() {
        VBox emptyState = new VBox(12);
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setPadding(new Insets(60));
        emptyState.setMaxWidth(Double.MAX_VALUE);

        Label bigEmoji = new Label("🌱");
        bigEmoji.setStyle("-fx-font-size: 72px;");

        Label heading = new Label("Your garden is empty!");
        heading.getStyleClass().add("empty-title");

        Label hint = new Label(
                "Complete tasks to earn coins, then visit the Shop\n" +
                "to open packs and discover animal companions.");
        hint.getStyleClass().add("empty-hint");
        hint.setWrapText(true);
        hint.setTextAlignment(TextAlignment.CENTER);

        emptyState.getChildren().addAll(bigEmoji, heading, hint);
        animalGrid.getChildren().add(emptyState);
    }

    private Node buildAnimalCard(Animal animal) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(16));
        card.setPrefWidth(130);
        card.setMinHeight(160);

        // Apply base style + rarity-specific style
        card.getStyleClass().addAll("animal-card", "rarity-" + animal.getRarity().toLowerCase());

        Label emojiLabel = new Label(animal.getEmoji());
        emojiLabel.setStyle("-fx-font-size: 44px;");

        Label nameLabel = new Label(animal.getName());
        nameLabel.getStyleClass().add("animal-name");
        nameLabel.setWrapText(true);
        nameLabel.setTextAlignment(TextAlignment.CENTER);

        Label rarityLabel = new Label(animal.getRarityLabel());
        rarityLabel.getStyleClass().addAll("rarity-badge",
                "rarity-text-" + animal.getRarity().toLowerCase());

        Label countLabel = new Label("×" + animal.getCount());
        countLabel.getStyleClass().add("animal-count");

        card.getChildren().addAll(emojiLabel, nameLabel, rarityLabel, countLabel);

        // Tooltip on hover
        Tooltip tip = new Tooltip(animal.getDescription());
        tip.setStyle("-fx-font-size: 12px;");
        Tooltip.install(card, tip);

        return card;
    }

    private void showError(String title, String detail) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(detail);
        alert.show();
    }
}
