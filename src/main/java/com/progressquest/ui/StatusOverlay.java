package com.progressquest.ui;

import com.progressquest.model.Character;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class StatusOverlay extends VBox {
    private final Character character;
    private final Runnable onClose;
    private Label lblStatusPoints;
    private VBox statusRowsContainer;
    private Label lblGold;

    public StatusOverlay(Character character, Runnable onClose) {
        this.character = character;
        this.onClose = onClose;
        initUI();
    }

    private void initUI() {
        this.setSpacing(10);
        this.setAlignment(Pos.CENTER);
        this.setStyle("-fx-background-color: rgba(0,0,0,0.9); -fx-padding: 20;");
        this.setMaxSize(400, 500);
        this.setVisible(false);

        Label lblTitle = new Label("CHARACTER STATUS");
        lblTitle.setTextFill(Color.WHITE); lblTitle.setFont(Font.font(20));

        lblStatusPoints = new Label();
        lblStatusPoints.setTextFill(Color.CYAN);

        lblGold = new Label();
        lblGold.setTextFill(Color.GOLD);

        statusRowsContainer = new VBox(5);

        Button btnClose = new Button("Resume Game");
        btnClose.setOnAction(e -> onClose.run());

        this.getChildren().addAll(lblTitle, lblStatusPoints, lblGold, statusRowsContainer, btnClose);
    }

    public void refresh() {
        int points = character.getAttributePoints();
        lblStatusPoints.setText("Points Available: " + points);

        lblGold.setText("Gold: " + character.getGold());

        statusRowsContainer.getChildren().clear();
        character.getAttributes().getAll().forEach((key, val) -> {
            HBox row = new HBox(10); row.setAlignment(Pos.CENTER);
            Label lbl = new Label(key + ": " + val); lbl.setTextFill(Color.WHITE); lbl.setPrefWidth(80);
            row.getChildren().add(lbl);

            if (points > 0) {
                Button btnPlus = new Button("+");
                btnPlus.setOnAction(e -> {
                    character.spendAttributePoint(key);
                    refresh();
                });
                row.getChildren().add(btnPlus);
            }
            statusRowsContainer.getChildren().add(row);
        });
    }
}