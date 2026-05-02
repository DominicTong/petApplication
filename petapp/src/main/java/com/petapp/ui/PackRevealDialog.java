package com.petapp.ui;

import com.petapp.model.Animal;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;

/**
 * Modal dialog that animates the reveal of animals drawn from a pack.
 *
 * Cards start invisible and fade in one by one with a short delay,
 * mimicking the excitement of opening a card pack.  The "Add to Garden"
 * button appears after all cards are revealed.
 */
public class PackRevealDialog {

    private final Stage       stage;
    private final List<Animal> animals;

    public PackRevealDialog(List<Animal> animals) {
        this.animals = animals;
        this.stage   = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Pack Opened! 🎴");
        stage.setResizable(false);
    }

    /** Shows the dialog and blocks until it is closed. */
    public void showAndWait() {
        // ── Root layout ───────────────────────────────────────────────────────
        VBox root = new VBox(28);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40, 44, 36, 44));
        root.setStyle("-fx-background-color: #1a1530;");

        // ── Heading ───────────────────────────────────────────────────────────
        Label heading = new Label("✨  Pack Opened!  ✨");
        heading.setStyle(
                "-fx-font-size: 22px; -fx-font-weight: bold; " +
                "-fx-text-fill: #f2d060; -fx-font-family: 'Segoe UI';");

        // ── Cards row ─────────────────────────────────────────────────────────
        HBox cardsRow = new HBox(20);
        cardsRow.setAlignment(Pos.CENTER);

        for (Animal animal : animals) {
            Node card = buildCard(animal);
            card.setOpacity(0);
            cardsRow.getChildren().add(card);
        }

        // ── Close button ──────────────────────────────────────────────────────
        Button closeBtn = new Button("Add to Garden 🌿");
        closeBtn.setStyle(
                "-fx-background-color: #4caf50; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-font-weight: bold; " +
                "-fx-padding: 11 28; -fx-background-radius: 24; -fx-cursor: hand;");
        closeBtn.setOpacity(0);
        closeBtn.setOnAction(e -> stage.close());

        root.getChildren().addAll(heading, cardsRow, closeBtn);

        Scene scene = new Scene(root, 560, 400);
        stage.setScene(scene);
        stage.show();

        // ── Sequential fade-in animation ──────────────────────────────────────
        SequentialTransition seq = new SequentialTransition();

        // Each card: short pause then fade in
        for (Node card : cardsRow.getChildren()) {
            PauseTransition  pause = new PauseTransition(Duration.millis(350));
            FadeTransition   fade  = new FadeTransition(Duration.millis(350), card);
            fade.setFromValue(0);
            fade.setToValue(1);
            ScaleTransition  scale = new ScaleTransition(Duration.millis(350), card);
            scale.setFromX(0.7);
            scale.setFromY(0.7);
            scale.setToX(1.0);
            scale.setToY(1.0);
            ParallelTransition combo = new ParallelTransition(fade, scale);
            seq.getChildren().addAll(pause, combo);
        }

        // Button appears after all cards
        PauseTransition btnPause = new PauseTransition(Duration.millis(200));
        FadeTransition  btnFade  = new FadeTransition(Duration.millis(300), closeBtn);
        btnFade.setFromValue(0);
        btnFade.setToValue(1);
        seq.getChildren().addAll(btnPause, btnFade);

        seq.play();

        // Block the calling thread until the stage is closed
        stage.showAndWait();
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private Node buildCard(Animal animal) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20, 16, 20, 16));
        card.setPrefWidth(140);
        card.setMinHeight(210);

        // Background and border colour by rarity
        String[] colours = rarityColours(animal.getRarity());
        String bg     = colours[0];
        String border = colours[1];
        String glow   = colours[2];

        card.setStyle(String.format(
                "-fx-background-color: %s; " +
                "-fx-border-color: %s; -fx-border-width: 2.5; " +
                "-fx-border-radius: 14; -fx-background-radius: 14; " +
                "-fx-effect: dropshadow(gaussian, %s, 14, 0.4, 0, 0);",
                bg, border, glow));

        Label emojiLabel = new Label(animal.getEmoji());
        emojiLabel.setStyle("-fx-font-size: 52px;");

        Label nameLabel = new Label(animal.getName());
        nameLabel.setStyle(
                "-fx-font-size: 14px; -fx-font-weight: bold; " +
                "-fx-text-fill: #f0ece4; -fx-font-family: 'Segoe UI';");
        nameLabel.setWrapText(true);

        Label rarityLabel = new Label(animal.getRarityLabel());
        rarityLabel.setStyle(
                "-fx-font-size: 11px; -fx-font-weight: bold; " +
                "-fx-text-fill: " + border + ";");

        Label descLabel = new Label(animal.getDescription());
        descLabel.setStyle(
                "-fx-font-size: 10px; -fx-text-fill: #c0bab0; " +
                "-fx-font-family: 'Segoe UI'; -fx-wrap-text: true;");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(120);

        card.getChildren().addAll(emojiLabel, nameLabel, rarityLabel, descLabel);
        return card;
    }

    /**
     * Returns [backgroundHex, borderHex, glowHex] for a given rarity string.
     */
    private String[] rarityColours(String rarity) {
        return switch (rarity) {
            case "LEGENDARY" -> new String[]{ "#2a1800", "#ffd700", "#ffd700" };
            case "RARE"      -> new String[]{ "#0c1828", "#5599ff", "#4488ee" };
            case "UNCOMMON"  -> new String[]{ "#111f11", "#55cc55", "#44bb44" };
            default          -> new String[]{ "#1c1c1c", "#999999", "#777777" };
        };
    }
}
