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
 * Shop tab — buy Animal Packs with earned coins.
 *
 * The single item for sale is the "Animal Pack":
 *  • Costs {@link DatabaseManager#PACK_COST} coins
 *  • Contains {@link DatabaseManager#ANIMALS_PER_PACK} randomly drawn animals
 *  • Rarity weighted: Common → Uncommon → Rare → Legendary
 *
 * Purchasing opens the {@link PackRevealDialog} to animate the reveal.
 */
public class ShopTab {

    private final VBox  root;
    private final Label coinLabel;
    private final Button buyBtn;

    public ShopTab() {
        root = new VBox(28);
        root.setPadding(new Insets(36, 28, 28, 28));
        root.setAlignment(Pos.TOP_CENTER);
        root.getStyleClass().add("tab-content");

        // ── Header ────────────────────────────────────────────────────────────
        Label title = new Label("🛒  Shop");
        title.getStyleClass().add("page-title");

        coinLabel = new Label("🪙  0 coins");
        coinLabel.getStyleClass().add("coin-badge");

        VBox headerBox = new VBox(10, title, coinLabel);
        headerBox.setAlignment(Pos.CENTER);

        // ── Pack card ─────────────────────────────────────────────────────────
        VBox packCard = new VBox(14);
        packCard.setAlignment(Pos.CENTER);
        packCard.getStyleClass().add("pack-card");
        packCard.setPadding(new Insets(36));
        packCard.setMaxWidth(360);

        Label packEmoji = new Label("🎴");
        packEmoji.setStyle("-fx-font-size: 72px;");

        Label packName = new Label("Animal Pack");
        packName.getStyleClass().add("pack-title");

        Label packDesc = new Label(
                "Unwrap " + DatabaseManager.ANIMALS_PER_PACK +
                " mystery animal companions!\n" +
                "From humble hamsters to legendary dragons…");
        packDesc.getStyleClass().add("pack-description");
        packDesc.setWrapText(true);
        packDesc.setTextAlignment(TextAlignment.CENTER);

        Label rarityLine = new Label(
                "🐾 Common  ·  🌟 Uncommon  ·  ✨ Rare  ·  👑 Legendary");
        rarityLine.getStyleClass().add("rarity-hint");

        Separator sep = new Separator();
        sep.setMaxWidth(260);

        Label costLabel = new Label("🪙  " + DatabaseManager.PACK_COST + " coins per pack");
        costLabel.getStyleClass().add("pack-cost");

        buyBtn = new Button("Open a Pack!");
        buyBtn.getStyleClass().add("btn-buy");
        buyBtn.setPrefWidth(210);
        buyBtn.setOnAction(e -> handleBuyPack());

        packCard.getChildren().addAll(
                packEmoji, packName, packDesc, rarityLine, sep, costLabel, buyBtn);

        // ── Footer hint ───────────────────────────────────────────────────────
        Label hint = new Label(
                "Earn coins by completing tasks in the Tasks tab. 📋");
        hint.getStyleClass().add("subtitle");
        hint.setTextAlignment(TextAlignment.CENTER);

        root.getChildren().addAll(headerBox, packCard, hint);
        refresh();
    }

    public Node getContent() { return root; }

    /** Refresh coin balance and update button state. */
    public void refresh() {
        try {
            UserData data = DatabaseManager.getInstance().getUserData();
            coinLabel.setText("🪙  " + data.getCoins() + " coins");
            // Disable buy button if player can't afford
            buyBtn.setDisable(data.getCoins() < DatabaseManager.PACK_COST);
        } catch (SQLException e) {
            showError("Failed to load shop", e.getMessage());
        }
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private void handleBuyPack() {
        try {
            List<Animal> drawn = DatabaseManager.getInstance().buyPack();

            if (drawn == null) {
                // Shouldn't normally reach here (button is disabled), but guard anyway
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Not Enough Coins");
                alert.setHeaderText(null);
                alert.setContentText(
                        "You need 🪙 " + DatabaseManager.PACK_COST + " coins to open a pack.\n" +
                        "Complete more tasks to earn coins!");
                alert.showAndWait();
                return;
            }

            // Show animated reveal dialog
            new PackRevealDialog(drawn).showAndWait();

            // Refresh balance after purchase
            refresh();

        } catch (SQLException e) {
            showError("Could not open pack", e.getMessage());
        }
    }

    private void showError(String title, String detail) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(detail);
        alert.show();
    }
}
