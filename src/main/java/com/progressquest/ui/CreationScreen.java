package com.progressquest.ui;

import com.progressquest.data.GameData;
import com.progressquest.model.Attributes;
import com.progressquest.model.Character;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class CreationScreen {

    private final Stage stage;
    private final BiConsumer<Character, Boolean> onGameStart; // Callback para voltar ao MainApp (Char, is2D)

    private Character tempChar;
    private int creationPoints = 25;
    private final int BASE_STAT = 8;

    public CreationScreen(Stage stage, BiConsumer<Character, Boolean> onGameStart) {
        this.stage = stage;
        this.onGameStart = onGameStart;
        this.tempChar = new Character(new Attributes());
        this.tempChar.getAttributes().setAll(BASE_STAT);
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        HBox topBox = new HBox(10);
        TextField tfName = new TextField("Hero");
        topBox.getChildren().addAll(new Label("Name:"), tfName);
        root.setTop(topBox);

        GridPane centerGrid = new GridPane();
        centerGrid.setHgap(10); centerGrid.setVgap(10);
        centerGrid.setPadding(new Insets(10, 0, 10, 0));

        VBox raceBox = createSelectionBox("Race", GameData.RACES);
        ToggleGroup raceGroup = (ToggleGroup) raceBox.getProperties().get("group");
        ((RadioButton)raceBox.getChildren().get(1)).setSelected(true);

        VBox classBox = createSelectionBox("Class", GameData.CLASSES);
        ToggleGroup classGroup = (ToggleGroup) classBox.getProperties().get("group");
        ((RadioButton)classBox.getChildren().get(1)).setSelected(true);

        VBox statsBox = new VBox(5);
        statsBox.setStyle("-fx-border-color: lightgray; -fx-padding: 5;");
        statsBox.getChildren().add(new Label("Stats (Point Buy)"));

        Label lblPoints = new Label("Points Left: " + creationPoints);
        lblPoints.setFont(Font.font("System", FontWeight.BOLD, 14));
        lblPoints.setStyle("-fx-text-fill: blue;");
        statsBox.getChildren().add(lblPoints);

        VBox statsRows = new VBox(5);
        refreshCreationStats(statsRows, lblPoints);
        statsBox.getChildren().add(statsRows);

        VBox startButtonsBox = new VBox(10);
        startButtonsBox.setAlignment(Pos.CENTER);

        Button btnStartIdle = new Button("Start Classic Mode (Idle)");
        btnStartIdle.setMaxWidth(Double.MAX_VALUE);
        btnStartIdle.setOnAction(e -> {
            if (validateCreation(raceGroup, classGroup, tfName)) {
                onGameStart.accept(tempChar, false); // Inicia Idle
            }
        });

        Button btnStart2D = new Button("Start Adventure Mode (2D)");
        btnStart2D.setMaxWidth(Double.MAX_VALUE);
        btnStart2D.setOnAction(e -> {
            if (validateCreation(raceGroup, classGroup, tfName)) {
                onGameStart.accept(tempChar, true); // Inicia 2D
            }
        });

        statsBox.getChildren().addAll(new Separator(), startButtonsBox);
        startButtonsBox.getChildren().addAll(btnStartIdle, btnStart2D);

        ScrollPane scrollRace = new ScrollPane(raceBox); scrollRace.setPrefHeight(300);
        ScrollPane scrollClass = new ScrollPane(classBox); scrollClass.setPrefHeight(300);

        centerGrid.add(scrollRace, 0, 0);
        centerGrid.add(scrollClass, 1, 0);
        centerGrid.add(statsBox, 2, 0);

        ColumnConstraints col = new ColumnConstraints(); col.setPercentWidth(33);
        centerGrid.getColumnConstraints().addAll(col, col, col);

        root.setCenter(centerGrid);
        stage.setScene(new Scene(root, 750, 550));
    }

    private boolean validateCreation(ToggleGroup raceGroup, ToggleGroup classGroup, TextField tfName) {
        if (creationPoints > 0) {
            new Alert(Alert.AlertType.WARNING, "You still have points to spend!").showAndWait();
            return false;
        }
        RadioButton rbRace = (RadioButton) raceGroup.getSelectedToggle();
        RadioButton rbClass = (RadioButton) classGroup.getSelectedToggle();
        if (rbRace != null && rbClass != null) {
            tempChar.init(tfName.getText(), rbRace.getText(), rbClass.getText());
            return true;
        }
        return false;
    }

    private VBox createSelectionBox(String title, List<String> options) {
        VBox box = new VBox(5);
        box.setStyle("-fx-border-color: lightgray; -fx-padding: 5;");
        box.getChildren().add(new Label(title));
        ToggleGroup group = new ToggleGroup();
        box.getProperties().put("group", group);
        for (String opt : options) {
            RadioButton rb = new RadioButton(opt);
            rb.setToggleGroup(group);
            box.getChildren().add(rb);
        }
        return box;
    }

    private void refreshCreationStats(VBox container, Label lblPoints) {
        container.getChildren().clear();
        Map<String, Integer> stats = tempChar.getAttributes().getAll();
        stats.forEach((key, val) -> {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            Label name = new Label(key); name.setPrefWidth(40);
            Button btnMinus = new Button("-");
            btnMinus.setDisable(val <= BASE_STAT);
            btnMinus.setOnAction(e -> {
                tempChar.getAttributes().decrement(key);
                creationPoints++;
                lblPoints.setText("Points Left: " + creationPoints);
                refreshCreationStats(container, lblPoints);
            });
            Label value = new Label(String.valueOf(val));
            value.setPrefWidth(30); value.setAlignment(Pos.CENTER);
            Button btnPlus = new Button("+");
            btnPlus.setDisable(creationPoints <= 0);
            btnPlus.setOnAction(e -> {
                if (creationPoints > 0) {
                    tempChar.getAttributes().increment(key);
                    creationPoints--;
                    lblPoints.setText("Points Left: " + creationPoints);
                    refreshCreationStats(container, lblPoints);
                }
            });
            row.getChildren().addAll(name, btnMinus, value, btnPlus);
            container.getChildren().add(row);
        });
    }
}