package com.progressquest.ui;

import com.progressquest.data.GameData;
import com.progressquest.model.Character;
import com.progressquest.model.Item;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.function.Consumer;

public class InventoryOverlay extends VBox {
    private final Character character;
    private final Consumer<String> onNotification;
    private final Runnable onMapChange;
    private final Runnable onClose;

    private ListView<Item> listInvBag;
    private ListView<String> listInvEquip;
    private ComboBox<String> mapSelector;

    public InventoryOverlay(Character character, Consumer<String> onNotification, Runnable onMapChange, Runnable onClose) {
        this.character = character;
        this.onNotification = onNotification;
        this.onMapChange = onMapChange;
        this.onClose = onClose;
        initUI();
    }

    private void initUI() {
        this.setSpacing(10);
        this.setAlignment(Pos.CENTER);
        this.setStyle("-fx-background-color: rgba(0,0,0,0.9); -fx-padding: 20;");
        this.setMaxSize(500, 550);
        this.setVisible(false);

        Label lblInvTitle = new Label("INVENTORY & MAP");
        lblInvTitle.setTextFill(Color.WHITE); lblInvTitle.setFont(Font.font(20));

        HBox mapBox = new HBox(10);
        mapBox.setAlignment(Pos.CENTER);
        Label lblMap = new Label("Travel to:"); lblMap.setTextFill(Color.WHITE);
        mapSelector = new ComboBox<>();
        mapSelector.getItems().addAll(GameData.MAPS.keySet());
        mapSelector.setValue("Green Fields");
        mapSelector.setOnAction(e -> {
            character.setCurrentMap(mapSelector.getValue());
            onNotification.accept("Traveled to " + mapSelector.getValue());
            onMapChange.run();
        });
        mapBox.getChildren().addAll(lblMap, mapSelector);

        listInvEquip = new ListView<>();
        listInvEquip.setPrefHeight(150);

        Label lblBag = new Label("Bag (Click to Equip):"); lblBag.setTextFill(Color.WHITE);
        listInvBag = new ListView<>();
        VBox.setVgrow(listInvBag, Priority.ALWAYS);

        listInvBag.setOnMouseClicked(e -> {
            Item selected = listInvBag.getSelectionModel().getSelectedItem();
            if (selected != null) {
                character.lootItem(selected);
                character.getInventory().remove(selected);
                refresh();
            }
        });

        listInvBag.setCellFactory(param -> new ListCell<Item>() {
            @Override
            protected void updateItem(Item item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(item.getName() + " (+" + item.getBonus() + " " + item.getStatAffected() + ") [" + item.getSlot() + "]");
            }
        });

        Button btnClose = new Button("Resume Game");
        btnClose.setOnAction(e -> onClose.run());

        this.getChildren().addAll(lblInvTitle, mapBox, new Separator(), new Label("Equipped:"), listInvEquip, lblBag, listInvBag, btnClose);
    }

    public void refresh() {
        listInvEquip.getItems().clear();
        character.getEquipment().forEach((slot, item) ->
                listInvEquip.getItems().add(slot + ": " + item.getDisplayString()));
        listInvBag.getItems().clear();
        listInvBag.getItems().addAll(character.getInventory());
    }
}