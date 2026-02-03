package com.progressquest.ui;

import com.progressquest.data.GameData;
import com.progressquest.engine.GameEngine;
import com.progressquest.model.Character;
import com.progressquest.model.Item;
import com.progressquest.model.Monster;
import com.progressquest.model.damage.DamageStrategy;
import com.progressquest.model.Potion;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class IdleMode {

    private final Stage stage;
    private final Character character;
    private final Runnable onExit;
    private GameEngine idleEngine;

    //componentes UI
    private Label lblName, lblRaceClass, lblLevel, lblUnspentPoints;
    private Label lblGold;
    private ProgressBar pbHP, pbMP, pbExp, pbAction;
    private Label lblHPText, lblMPText, lblCurrentAction, lblTargetMonster;
    private TextArea combatLog;
    private ComboBox<String> mapSelector;
    private Button btnShop;
    private ListView<String> listEquip, listInv, listSpells, listQuests;
    private VBox statsContainer;

    public IdleMode(Stage stage, Character character, Runnable onExit) {
        this.stage = stage;
        this.character = character;
        this.onExit = onExit;
    }

    public void start() {
        idleEngine = new GameEngine(character);
        BorderPane root = new BorderPane();

        //coluna 1
        VBox col1 = new VBox(5); col1.setPadding(new Insets(5));
        VBox charSheet = createPanel("Character Sheet");
        lblName = new Label(); lblRaceClass = new Label(); lblLevel = new Label();
        lblUnspentPoints = new Label();
        lblUnspentPoints.setStyle("-fx-text-fill: blue; -fx-font-weight: bold;");
        lblGold = new Label();
        lblGold.setStyle("-fx-text-fill: goldenrod; -fx-font-weight: bold;");
        statsContainer = new VBox(2);
        charSheet.getChildren().addAll(lblName, lblRaceClass, lblLevel, lblGold, new Separator(), lblUnspentPoints, statsContainer);
        VBox spellBox = createPanel("Spell Book");
        listSpells = new ListView<>();
        spellBox.getChildren().add(listSpells);
        VBox.setVgrow(listSpells, Priority.ALWAYS);
        col1.getChildren().addAll(charSheet, spellBox);
        VBox.setVgrow(spellBox, Priority.ALWAYS);

        //coluna 2
        VBox col2 = new VBox(5); col2.setPadding(new Insets(5));
        VBox vitalBox = createPanel("Vitals");
        pbHP = new ProgressBar(1.0); pbHP.setMaxWidth(Double.MAX_VALUE); pbHP.getStyleClass().addAll("flask-bar", "hp-bar");
        lblHPText = new Label("HP: ?/?");
        pbMP = new ProgressBar(1.0); pbMP.setMaxWidth(Double.MAX_VALUE); pbMP.getStyleClass().addAll("flask-bar", "mp-bar");
        lblMPText = new Label("MP: ?/?");
        vitalBox.getChildren().addAll(new Label("Health"), pbHP, lblHPText, new Label("Mana"), pbMP, lblMPText);

        VBox combatBox = createPanel("Combat Zone");
        mapSelector = new ComboBox<>();
        mapSelector.getItems().addAll(GameData.MAPS.keySet());
        mapSelector.setValue("Green Fields");
        mapSelector.setMaxWidth(Double.MAX_VALUE);
        mapSelector.setOnAction(e -> character.setCurrentMap(mapSelector.getValue()));

        // Utility buttons (combat consumables / safe-zone shop)
        HBox utilityRow = new HBox(5);
        Button btnHpPotion = new Button("Use HP Potion");
        Button btnMpPotion = new Button("Use MP Potion");
        btnShop = new Button("Open Shop");

        btnHpPotion.setOnAction(e -> idleEngine.requestPotionUse(Potion.Kind.HEALTH));
        btnMpPotion.setOnAction(e -> idleEngine.requestPotionUse(Potion.Kind.MANA));
        btnShop.setOnAction(e -> openShopWindow());
        btnShop.setDisable(true);

        utilityRow.getChildren().addAll(btnHpPotion, btnMpPotion, btnShop);
        utilityRow.setAlignment(Pos.CENTER_LEFT);
        lblTargetMonster = new Label("Searching...");
        lblTargetMonster.setFont(Font.font("System", FontWeight.BOLD, 14));
        lblTargetMonster.setStyle("-fx-text-fill: darkred;");
        combatBox.getChildren().addAll(new Label("Location:"), mapSelector, utilityRow, new Separator(), lblTargetMonster);

        VBox logBox = createPanel("Adventure Log");
        combatLog = new TextArea();
        combatLog.setEditable(false); combatLog.setWrapText(true); combatLog.getStyleClass().add("log-area");
        logBox.getChildren().add(combatLog);
        VBox.setVgrow(logBox, Priority.ALWAYS); VBox.setVgrow(combatLog, Priority.ALWAYS);
        col2.getChildren().addAll(vitalBox, combatBox, logBox);
        VBox.setVgrow(logBox, Priority.ALWAYS);

        //coluna 3
        VBox col3 = new VBox(5); col3.setPadding(new Insets(5));
        VBox equipBox = createPanel("Equipment");
        listEquip = new ListView<>();
        equipBox.getChildren().add(listEquip);
        VBox.setVgrow(listEquip, Priority.ALWAYS);
        VBox invBox = createPanel("Inventory");
        listInv = new ListView<>();
        invBox.getChildren().add(listInv);
        VBox.setVgrow(listInv, Priority.ALWAYS);
        VBox questBox = createPanel("Quests");
        listQuests = new ListView<>();
        questBox.getChildren().add(listQuests);
        VBox.setVgrow(listQuests, Priority.ALWAYS);
        col3.getChildren().addAll(equipBox, invBox, questBox);
        VBox.setVgrow(questBox, Priority.ALWAYS); VBox.setVgrow(equipBox, Priority.ALWAYS); VBox.setVgrow(invBox, Priority.ALWAYS);

        GridPane grid = new GridPane();
        grid.add(col1, 0, 0); grid.add(col2, 1, 0); grid.add(col3, 2, 0);
        ColumnConstraints c = new ColumnConstraints(); c.setPercentWidth(33.3);
        grid.getColumnConstraints().addAll(c, c, c);
        root.setCenter(grid);

        VBox bottom = new VBox(2); bottom.setPadding(new Insets(5));
        lblCurrentAction = new Label("Starting...");
        pbAction = new ProgressBar(0); pbAction.setMaxWidth(Double.MAX_VALUE); pbAction.getStyleClass().addAll("flask-bar", "action-bar");
        HBox xpBox = new HBox(5);
        Label lblXp = new Label("XP:");
        pbExp = new ProgressBar(0); pbExp.setMaxWidth(Double.MAX_VALUE); HBox.setHgrow(pbExp, Priority.ALWAYS);
        pbExp.getStyleClass().addAll("flask-bar", "exp-bar");
        xpBox.getChildren().addAll(lblXp, pbExp);
        bottom.getChildren().addAll(lblCurrentAction, pbAction, xpBox);
        root.setBottom(bottom);

        idleEngine.setCallbacks(
                msg -> Platform.runLater(() -> { combatLog.appendText(msg + "\n"); combatLog.setScrollTop(Double.MAX_VALUE); }),
                () -> Platform.runLater(this::updateIdleUI)
        );
        idleEngine.start();
        updateIdleUI();

        Scene scene = new Scene(root, 900, 700);
        scene.getStylesheets().add(getClass().getResource("/styles/medieval.css").toExternalForm());

        stage.setOnCloseRequest(e -> { idleEngine.stop(); Platform.exit(); System.exit(0); });
        stage.setScene(scene);
    }

    private VBox createPanel(String title) {
        VBox box = new VBox(2);
        box.getStyleClass().add("game-panel");
        Label lbl = new Label(title);
        lbl.getStyleClass().add("panel-header");
        lbl.setMaxWidth(Double.MAX_VALUE);
        box.getChildren().add(lbl);
        return box;
    }

    private void updateIdleUI() {
        lblName.setText(character.getName());
        lblRaceClass.setText(character.getRace() + " " + character.getClazz());
        lblLevel.setText("Level " + character.getLevel());
        lblGold.setText("Gold: " + character.getGold());

        // Safe-zone shop button only works in town.
        if (btnShop != null) {
            btnShop.setDisable(!GameData.SAFE_ZONE.equals(character.getCurrentMap()));
        }

        int points = character.getAttributePoints();
        lblUnspentPoints.setText(points > 0 ? "POINTS AVAILABLE: " + points : "");

        statsContainer.getChildren().clear();
        character.getAttributes().getAll().forEach((key, val) -> {
            HBox row = new HBox(10); row.setAlignment(Pos.CENTER_LEFT);
            row.getChildren().add(new Label(key + ": " + val));
            if (points > 0) {
                Button btnAdd = new Button("+");
                btnAdd.setStyle("-fx-font-size: 10; -fx-padding: 2 6; -fx-base: lightgreen;");
                btnAdd.setOnAction(e -> { character.spendAttributePoint(key); updateIdleUI(); });
                row.getChildren().add(btnAdd);
            }
            statsContainer.getChildren().add(row);
        });

        // Derived combat info (character sheet)
        DamageStrategy strat = character.getDamageStrategy();
        String dmgType = (strat != null) ? strat.typeLabel() : "Physical";
        int manaCost = (strat != null) ? strat.manaCost(character) : 0;
        int dmgVal = character.calculateDamage();
        Label lblDmg = new Label("Damage (" + dmgType + "): " + dmgVal + (manaCost > 0 ? "  |  Cost: " + manaCost + " MP" : ""));
        lblDmg.setStyle("-fx-font-weight: bold;");
        statsContainer.getChildren().add(new Separator());
        statsContainer.getChildren().add(lblDmg);

        double hpPerc = (double) character.getHpCurrent() / character.getHpMax();
        pbHP.setProgress(hpPerc);
        lblHPText.setText("HP: " + character.getHpCurrent() + "/" + character.getHpMax());
        double mpPerc = (double) character.getMpCurrent() / character.getMpMax();
        pbMP.setProgress(mpPerc);
        lblMPText.setText("MP: " + character.getMpCurrent() + "/" + character.getMpMax());

        Monster m = idleEngine.getCurrentMonster();
        lblTargetMonster.setText(m != null ? "Fighting: " + m.getName() + " (HP: " + m.getCurrentHp() + "/" + m.getMaxHp() + ")" : "Searching...");

        pbExp.setProgress((double)character.getExperience() / character.xpToNextLevel());
        pbAction.setProgress((double)idleEngine.getActionProgress() / idleEngine.getActionMax());
        lblCurrentAction.setText(idleEngine.getCurrentAction());

        listSpells.getItems().setAll(character.getSpellBook());
        listEquip.getItems().clear();
        character.getEquipment().forEach((slot, item) -> listEquip.getItems().add(slot + ": " + item.getDisplayString()));
        listInv.getItems().clear();
        for(Item i : character.getInventory()) listInv.getItems().add(i.toString());
        listQuests.getItems().clear();
        if(character.getCurrentQuest() != null) {
            listQuests.getItems().add(character.getCurrentQuest().getTitle() + " (" + character.getCurrentQuest().getCurrentKills() + "/" + character.getCurrentQuest().getRequiredKills() + ")");
        }
    }

    /** Simple shop UI available only in the safe-zone map. */
    private void openShopWindow() {
        if (!GameData.SAFE_ZONE.equals(character.getCurrentMap())) {
            return;
        }

        Stage shopStage = new Stage();
        shopStage.setTitle("Shop - " + GameData.SAFE_ZONE);

        VBox root = new VBox(8);
        root.setPadding(new Insets(10));

        Label lblWallet = new Label();
        lblWallet.setStyle("-fx-font-weight: bold; -fx-text-fill: goldenrod;");

        ListView<ShopEntry> list = new ListView<>();
        list.getItems().addAll(
                // Potions
                new ShopEntry("Health Potion (+20% HP)", 30, () -> new Potion("Health Potion", Potion.Kind.HEALTH, 0.20, 30)),
                new ShopEntry("Mana Potion (+20% MP)", 25, () -> new Potion("Mana Potion", Potion.Kind.MANA, 0.20, 25)),
                // Basic equipment
                new ShopEntry("Iron Sword (+2)", 60, () -> new Item("Iron Sword", Item.Slot.WEAPON, 2, "STR")),
                new ShopEntry("Kite Shield (+2)", 55, () -> new Item("Kite Shield", Item.Slot.SHIELD, 2, "CON")),
                new ShopEntry("Leather Hauberk (+2)", 70, () -> new Item("Leather Hauberk", Item.Slot.HAUBERK, 2, "CON")),
                new ShopEntry("Steel Helm (+1)", 40, () -> new Item("Steel Helm", Item.Slot.HELM, 1, "CON"))
        );

        list.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ShopEntry item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.label + " - " + item.price + " gold");
                }
            }
        });

        Button btnBuy = new Button("Buy");
        btnBuy.setMaxWidth(Double.MAX_VALUE);
        btnBuy.setOnAction(e -> {
            ShopEntry sel = list.getSelectionModel().getSelectedItem();
            if (sel == null) return;

            if (!character.spendGold(sel.price)) {
                combatLog.appendText("Not enough gold!\n");
                return;
            }

            Item item = sel.factory.get();
            if (item instanceof Potion) {
                character.getInventory().add(item);
            } else {
                character.lootItem(item);
            }
            combatLog.appendText("Purchased: " + item.toString() + "\n");
            updateIdleUI();
        });

        Runnable refreshWallet = () -> lblWallet.setText("Your Gold: " + character.getGold());
        refreshWallet.run();

        root.getChildren().addAll(new Label("Welcome to town!"), lblWallet, new Separator(), list, btnBuy);
        VBox.setVgrow(list, Priority.ALWAYS);

        Scene scene = new Scene(root, 360, 420);
        scene.getStylesheets().add(getClass().getResource("/styles/medieval.css").toExternalForm());
        shopStage.setScene(scene);
        shopStage.initOwner(stage);
        shopStage.show();
    }

    /** Tiny helper to represent a shop item. */
    private static class ShopEntry {
        final String label;
        final long price;
        final java.util.function.Supplier<Item> factory;
        ShopEntry(String label, long price, java.util.function.Supplier<Item> factory) {
            this.label = label;
            this.price = price;
            this.factory = factory;
        }
        @Override public String toString() { return label; }
    }
}