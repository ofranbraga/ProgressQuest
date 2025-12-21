package com.progressquest.ui;

import com.progressquest.data.GameData;
import com.progressquest.engine.GameEngine;
import com.progressquest.model.Attributes;
import com.progressquest.model.Character;
import com.progressquest.model.Item;
import com.progressquest.model.Monster;
import com.progressquest.util.RandomNameGenerator;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.util.*;

public class MainApp extends Application {

    private Stage window;
    private Character tempChar;
    private GameEngine idleEngine;

    private Canvas gameCanvas;
    private AnimationTimer gameLoop;
    private Set<KeyCode> activeKeys = new HashSet<>();
    private List<Monster> activeMonsters = new ArrayList<>();
    private List<Projectile> activeProjectiles = new ArrayList<>();
    private long lastAttackTime = 0;
    private Random rand = new Random();

    private ProgressBar pb2DHP, pb2DMP, pb2DXP;
    private Label lbl2DInfo;
    private Label lblNotification;

    private VBox inventoryOverlay;
    private ListView<Item> list2DInvBag;
    private ListView<String> list2DInvEquip;
    private ComboBox<String> mapSelector2D;

    private VBox statusOverlay;
    private VBox statusRowsContainer;
    private Label lblStatusPoints;

    private int creationPoints = 25;
    private final int BASE_STAT = 8;

    private Label lblName, lblRaceClass, lblLevel, lblUnspentPoints;
    private ProgressBar pbHP, pbMP, pbExp, pbAction;
    private Label lblHPText, lblMPText, lblCurrentAction, lblTargetMonster;
    private TextArea combatLog;
    private ComboBox<String> mapSelector;
    private ListView<String> listEquip, listInv, listSpells, listQuests;
    private VBox statsContainer;

    //classe interna para os projeteis (fire ball)
    private class Projectile {
        double x, y, dx, dy;
        boolean active = true;
        public Projectile(double x, double y, double dx, double dy) {
            this.x = x; this.y = y; this.dx = dx; this.dy = dy;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.window = primaryStage;
        window.setTitle("Progress Quest - JavaFX RPG");
        showCreationScreen();
        window.show();
    }

    //tela de criação
    private void showCreationScreen() {
        tempChar = new Character(new Attributes());
        tempChar.getAttributes().setAll(BASE_STAT);
        this.creationPoints = 25;

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
            if (validateCreation(raceGroup, classGroup, tfName)) startIdleGame();
        });

        Button btnStart2D = new Button("Start Adventure Mode (2D)");
        btnStart2D.setMaxWidth(Double.MAX_VALUE);
        btnStart2D.setOnAction(e -> {
            if (validateCreation(raceGroup, classGroup, tfName)) start2DGame();
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
        window.setScene(new Scene(root, 750, 550));
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

    private VBox createSelectionBox(String title, java.util.List<String> options) {
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

    //modo 2d
    private void start2DGame() {
        StackPane root = new StackPane();
        BorderPane gameLayout = new BorderPane();

        gameCanvas = new Canvas(800, 600);
        Pane gamePane = new Pane(gameCanvas);
        gamePane.setStyle("-fx-background-color: #228B22;");
        gameLayout.setCenter(gamePane);

        //hud inferior
        VBox hud = new VBox(5);
        hud.setPadding(new Insets(10));
        hud.setStyle("-fx-background-color: rgba(0,0,0,0.8);");

        HBox infoBox = new HBox(20);
        lbl2DInfo = new Label(tempChar.getName() + " (Lvl " + tempChar.getLevel() + ")");
        lbl2DInfo.setTextFill(Color.WHITE);
        lbl2DInfo.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        Label lblControls = new Label("WASD: Move | SPACE: Attack | CLICK: Fireball | I: Inv | C: Stats | F11: Fullscreen");
        lblControls.setTextFill(Color.YELLOW);
        infoBox.getChildren().addAll(lbl2DInfo, lblControls);

        HBox barsBox = new HBox(10);
        barsBox.setAlignment(Pos.CENTER_LEFT);

        pb2DHP = new ProgressBar(1.0); pb2DHP.setStyle("-fx-accent: red;"); pb2DHP.setPrefWidth(150);
        pb2DMP = new ProgressBar(1.0); pb2DMP.setStyle("-fx-accent: blue;"); pb2DMP.setPrefWidth(150);
        pb2DXP = new ProgressBar(0.0); pb2DXP.setStyle("-fx-accent: green;"); pb2DXP.setPrefWidth(300); pb2DXP.setMaxWidth(Double.MAX_VALUE); HBox.setHgrow(pb2DXP, Priority.ALWAYS);

        barsBox.getChildren().addAll(new Label("HP"), pb2DHP, new Label("MP"), pb2DMP, new Label("XP"), pb2DXP);
        hud.getChildren().addAll(infoBox, barsBox);
        gameLayout.setBottom(hud);

        //notificação flutuante dos drops
        lblNotification = new Label("");
        lblNotification.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        lblNotification.setTextFill(Color.GOLD);
        lblNotification.setStyle("-fx-effect: dropshadow(one-pass-box, black, 2, 0.5, 0, 0);");
        StackPane.setAlignment(lblNotification, Pos.TOP_CENTER);
        StackPane.setMargin(lblNotification, new Insets(50, 0, 0, 0));

        root.getChildren().addAll(gameLayout, lblNotification);

        //inventario e mapa
        createInventoryOverlay();
        root.getChildren().add(inventoryOverlay);

        //status
        createStatusOverlay();
        root.getChildren().add(statusOverlay);

        Scene scene = new Scene(root, 800, 680);

        //inputs
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.I) toggleInventory();
            else if (e.getCode() == KeyCode.C) toggleStatus();
            else if (e.getCode() == KeyCode.F11) window.setFullScreen(!window.isFullScreen());
            else activeKeys.add(e.getCode());
        });
        scene.setOnKeyReleased(e -> activeKeys.remove(e.getCode()));

        //implementação da bola de fogo ao clicar
        scene.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && !inventoryOverlay.isVisible() && !statusOverlay.isVisible()) {
                fireProjectile(e.getX(), e.getY());
            }
        });

        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!inventoryOverlay.isVisible() && !statusOverlay.isVisible()) {
                    update2DGame();
                }
                render2DGame(gameCanvas.getGraphicsContext2D());
                update2DUI();

                //vai limpat notificação antiga
                if (System.currentTimeMillis() % 3000 < 50 && !lblNotification.getText().isEmpty()) {
                    //forma simples de limpar msg depois de um tempo
                    //depois vou colocar um timer dedicado
                }

                if (tempChar.isDead()) handleDeath();
            }
        };
        spawn2DMonsters();
        gameLoop.start();
        window.setScene(scene);
    }

    private void createInventoryOverlay() {
        inventoryOverlay = new VBox(10);
        inventoryOverlay.setAlignment(Pos.CENTER);
        inventoryOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.9); -fx-padding: 20;");
        inventoryOverlay.setMaxSize(500, 550);
        inventoryOverlay.setVisible(false);

        Label lblInvTitle = new Label("INVENTORY & MAP");
        lblInvTitle.setTextFill(Color.WHITE); lblInvTitle.setFont(Font.font(20));

        //seletor de mapa dentro do inventário
        HBox mapBox = new HBox(10);
        mapBox.setAlignment(Pos.CENTER);
        Label lblMap = new Label("Travel to:"); lblMap.setTextFill(Color.WHITE);
        mapSelector2D = new ComboBox<>();
        mapSelector2D.getItems().addAll(GameData.MAPS.keySet());
        mapSelector2D.setValue("Green Fields");
        mapSelector2D.setOnAction(e -> {
            tempChar.setCurrentMap(mapSelector2D.getValue());
            showNotification("Traveled to " + mapSelector2D.getValue());
            activeMonsters.clear(); //limoa os mobs do mapa antigo
            spawn2DMonsters(); //spawna novos mobs
        });
        mapBox.getChildren().addAll(lblMap, mapSelector2D);

        list2DInvEquip = new ListView<>();
        list2DInvEquip.setPrefHeight(150);

        Label lblBag = new Label("Bag (Click to Equip):"); lblBag.setTextFill(Color.WHITE);
        list2DInvBag = new ListView<>();
        VBox.setVgrow(list2DInvBag, Priority.ALWAYS);

        //logica de equipar o item ao cliclar
        list2DInvBag.setOnMouseClicked(e -> {
            Item selected = list2DInvBag.getSelectionModel().getSelectedItem();
            if (selected != null) {
                tempChar.lootItem(selected); //reutiliza lógica de auto-equip
                tempChar.getInventory().remove(selected);
                refresh2DInventory();
            }
        });

        //CellFactory para mostrar nome bonitinho do item
        list2DInvBag.setCellFactory(param -> new ListCell<Item>() {
            @Override
            protected void updateItem(Item item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(item.getName() + " (+" + item.getBonus() + " " + item.getStatAffected() + ") [" + item.getSlot() + "]");
            }
        });

        Button btnClose = new Button("Resume Game");
        btnClose.setOnAction(e -> inventoryOverlay.setVisible(false));

        inventoryOverlay.getChildren().addAll(lblInvTitle, mapBox, new Separator(), new Label("Equipped:"), list2DInvEquip, lblBag, list2DInvBag, btnClose);
    }

    private void createStatusOverlay() {
        statusOverlay = new VBox(10);
        statusOverlay.setAlignment(Pos.CENTER);
        statusOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.9); -fx-padding: 20;");
        statusOverlay.setMaxSize(400, 500);
        statusOverlay.setVisible(false);

        Label lblTitle = new Label("CHARACTER STATUS");
        lblTitle.setTextFill(Color.WHITE); lblTitle.setFont(Font.font(20));

        lblStatusPoints = new Label();
        lblStatusPoints.setTextFill(Color.CYAN);

        statusRowsContainer = new VBox(5);

        Button btnClose = new Button("Resume Game");
        btnClose.setOnAction(e -> statusOverlay.setVisible(false));

        statusOverlay.getChildren().addAll(lblTitle, lblStatusPoints, statusRowsContainer, btnClose);
    }

    private void toggleInventory() {
        boolean opening = !inventoryOverlay.isVisible();
        inventoryOverlay.setVisible(opening);
        if(opening) {
            statusOverlay.setVisible(false);
            refresh2DInventory();
        }
    }

    private void refresh2DInventory() {
        list2DInvEquip.getItems().clear();
        tempChar.getEquipment().forEach((slot, item) ->
                list2DInvEquip.getItems().add(slot + ": " + item.getDisplayString()));

        list2DInvBag.getItems().clear();
        list2DInvBag.getItems().addAll(tempChar.getInventory());
    }

    private void toggleStatus() {
        boolean opening = !statusOverlay.isVisible();
        statusOverlay.setVisible(opening);
        if(opening) {
            inventoryOverlay.setVisible(false);
            refresh2DStatus();
        }
    }

    private void refresh2DStatus() {
        int points = tempChar.getAttributePoints();
        lblStatusPoints.setText("Points Available: " + points);

        statusRowsContainer.getChildren().clear();
        tempChar.getAttributes().getAll().forEach((key, val) -> {
            HBox row = new HBox(10); row.setAlignment(Pos.CENTER);
            Label lbl = new Label(key + ": " + val); lbl.setTextFill(Color.WHITE); lbl.setPrefWidth(80);
            row.getChildren().add(lbl);

            if (points > 0) {
                Button btnPlus = new Button("+");
                btnPlus.setOnAction(e -> {
                    tempChar.spendAttributePoint(key);
                    refresh2DStatus();
                });
                row.getChildren().add(btnPlus);
            }
            statusRowsContainer.getChildren().add(row);
        });
    }

    private void update2DUI() {
        lbl2DInfo.setText(tempChar.getName() + " (Lvl " + tempChar.getLevel() + ")");
        pb2DHP.setProgress((double)tempChar.getHpCurrent() / tempChar.getHpMax());
        pb2DMP.setProgress((double)tempChar.getMpCurrent() / tempChar.getMpMax());
        pb2DXP.setProgress((double)tempChar.getExperience() / tempChar.xpToNextLevel());
    }

    private void showNotification(String msg) {
        lblNotification.setText(msg);
    }

    //logica do jogo em 2d
    private void fireProjectile(double targetX, double targetY) {
        if (tempChar.getMpCurrent() >= 5) {
            tempChar.regenMana(-5); //fire ball gasta 5 mana
            double angle = Math.atan2(targetY - tempChar.getY(), targetX - tempChar.getX());
            double speed = 8.0;
            activeProjectiles.add(new Projectile(tempChar.getX() + 16, tempChar.getY() + 16, Math.cos(angle) * speed, Math.sin(angle) * speed));
        } else {
            showNotification("Not enough Mana!");
        }
    }

    private void spawn2DMonsters() {
        while (activeMonsters.size() < 5) {
            Monster m = new Monster(GameData.getMonsterFromMap(tempChar.getCurrentMap()), tempChar.getLevel());
            m.spawnRandomly(800, 600);
            activeMonsters.add(m);
        }
    }

    //gera o loot
    private Item generateLoot() {
        Item.Slot[] slots = Item.Slot.values();
        Item.Slot slot = slots[rand.nextInt(slots.length)];
        String name = RandomNameGenerator.randomItemName();
        int bonus = 1 + (tempChar.getLevel() / 2) + rand.nextInt(3);
        return new Item(name, slot, bonus, "STR"); //simplificado STR
    }

    private void update2DGame() {
        //movimento do jogador
        double dx = 0, dy = 0;
        if (activeKeys.contains(KeyCode.W)) dy -= 1;
        if (activeKeys.contains(KeyCode.S)) dy += 1;
        if (activeKeys.contains(KeyCode.A)) dx -= 1;
        if (activeKeys.contains(KeyCode.D)) dx += 1;
        if (dx != 0 && dy != 0) { dx *= 0.707; dy *= 0.707; }
        tempChar.move(dx, dy);
        //limitez
        if (tempChar.getX() < 0) tempChar.setPosition(0, tempChar.getY());
        if (tempChar.getY() < 0) tempChar.setPosition(tempChar.getX(), 0);
        if (tempChar.getX() > 768) tempChar.setPosition(768, tempChar.getY());
        if (tempChar.getY() > 568) tempChar.setPosition(tempChar.getX(), 568);

        //projeteis
        Iterator<Projectile> projIt = activeProjectiles.iterator();
        while (projIt.hasNext()) {
            Projectile p = projIt.next();
            p.x += p.dx; p.y += p.dy;
            if (p.x < 0 || p.x > 800 || p.y < 0 || p.y > 600) projIt.remove();
            else {
                for (Monster m : activeMonsters) {
                    if (getDistance(p.x, p.y, m.getX(), m.getY()) < 30) {
                        //dano magico
                        int magicDmg = 5 + tempChar.getAttributes().get("INT");
                        m.takeDamage(magicDmg);
                        projIt.remove();
                        break;
                    }
                }
            }
        }

        //ataque melee
        if (activeKeys.contains(KeyCode.SPACE)) {
            long now = System.currentTimeMillis();
            if (now - lastAttackTime > 500) {
                for (Monster m : activeMonsters) {
                    if (getDistance(tempChar.getX(), tempChar.getY(), m.getX(), m.getY()) < 60) {
                        m.takeDamage(tempChar.calculateDamage());
                    }
                }
                lastAttackTime = now;
            }
        }

        //monstros e drops
        Iterator<Monster> it = activeMonsters.iterator();
        while (it.hasNext()) {
            Monster m = it.next();
            m.moveTowards(tempChar.getX(), tempChar.getY(), 1.0);

            //dano no jogador
            if (getDistance(tempChar.getX(), tempChar.getY(), m.getX(), m.getY()) < 30) {
                tempChar.takeDamage(1);
            }

            if (m.isDead()) {
                tempChar.gainExperience(m.getRewardXP());

                //sistema de drop
                if (rand.nextDouble() < 0.3) {
                    Item item = generateLoot();
                    tempChar.getInventory().add(item); // Adiciona na bag (não equipa auto pra ter graça de equipar)
                    showNotification("Looted: " + item.getName());
                }
                //spell book drop, tornei ele raro
                if (rand.nextDouble() < 0.1) {
                    String spell = GameData.getRandomSpell();
                    tempChar.learnSpell(spell);
                    showNotification("Learned: " + spell);
                }

                it.remove();
            }
        }
        spawn2DMonsters();
    }

    private void render2DGame(GraphicsContext gc) {
        gc.setFill(Color.FORESTGREEN);
        gc.fillRect(0, 0, 800, 600);

        //player
        gc.setFill(Color.BLUE);
        gc.fillRect(tempChar.getX(), tempChar.getY(), 32, 32);
        gc.setStroke(Color.WHITE); gc.strokeRect(tempChar.getX(), tempChar.getY(), 32, 32);

        //implementação de projeteis
        gc.setFill(Color.ORANGE);
        for (Projectile p : activeProjectiles) gc.fillOval(p.x, p.y, 10, 10);

        //monstros
        for (Monster m : activeMonsters) {
            gc.setFill(Color.RED);
            gc.fillOval(m.getX(), m.getY(), 32, 32);
            //vida
            gc.setFill(Color.BLACK); gc.fillRect(m.getX(), m.getY() - 10, 32, 5);
            gc.setFill(Color.LIME);
            double hpPerc = (double) m.getCurrentHp() / m.getMaxHp();
            gc.fillRect(m.getX(), m.getY() - 10, 32 * hpPerc, 5);
            //nome
            gc.setFill(Color.WHITE);
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setFont(Font.font("Arial", 10));
            gc.fillText(m.getName() + " (Lvl " + m.getLevel() + ")", m.getX() + 16, m.getY() - 15);
        }

        //efeito de combate
        if (System.currentTimeMillis() - lastAttackTime < 100) {
            gc.setStroke(Color.YELLOW); gc.setLineWidth(3);
            gc.strokeOval(tempChar.getX() - 20, tempChar.getY() - 20, 72, 72);
        }
    }

    private void handleDeath() {
        gameLoop.stop();
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("GAME OVER");
            alert.setHeaderText("You have died!");
            alert.setContentText("What would you like to do?");
            ButtonType btnRevive = new ButtonType("Revive (Keep XP/Stats)");
            ButtonType btnRestart = new ButtonType("New Character");
            alert.getButtonTypes().setAll(btnRevive, btnRestart);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == btnRevive) {
                tempChar.heal(tempChar.getHpMax());
                tempChar.regenMana(tempChar.getMpMax());
                activeMonsters.clear();
                activeProjectiles.clear();
                activeKeys.clear();
                gameLoop.start();
            } else {
                showCreationScreen();
            }
        });
    }

    private double getDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    //modo de jogo idle
    private void startIdleGame() {
        idleEngine = new GameEngine(tempChar);
        BorderPane root = new BorderPane();

        //coluna 1
        VBox col1 = new VBox(5); col1.setPadding(new Insets(5));
        VBox charSheet = createPanel("Character Sheet");
        lblName = new Label(); lblRaceClass = new Label(); lblLevel = new Label();
        lblUnspentPoints = new Label();
        lblUnspentPoints.setStyle("-fx-text-fill: blue; -fx-font-weight: bold;");
        statsContainer = new VBox(2);
        charSheet.getChildren().addAll(lblName, lblRaceClass, lblLevel, new Separator(), lblUnspentPoints, statsContainer);
        VBox spellBox = createPanel("Spell Book");
        listSpells = new ListView<>();
        spellBox.getChildren().add(listSpells);
        VBox.setVgrow(listSpells, Priority.ALWAYS);
        col1.getChildren().addAll(charSheet, spellBox);
        VBox.setVgrow(spellBox, Priority.ALWAYS);

        //coluna 2
        VBox col2 = new VBox(5); col2.setPadding(new Insets(5));
        VBox vitalBox = createPanel("Vitals");
        pbHP = new ProgressBar(1.0); pbHP.setMaxWidth(Double.MAX_VALUE); pbHP.setStyle("-fx-accent: red;");
        lblHPText = new Label("HP: ?/?");
        pbMP = new ProgressBar(1.0); pbMP.setMaxWidth(Double.MAX_VALUE); pbMP.setStyle("-fx-accent: blue;");
        lblMPText = new Label("MP: ?/?");
        vitalBox.getChildren().addAll(new Label("Health"), pbHP, lblHPText, new Label("Mana"), pbMP, lblMPText);

        VBox combatBox = createPanel("Combat Zone");
        mapSelector = new ComboBox<>();
        mapSelector.getItems().addAll(GameData.MAPS.keySet());
        mapSelector.setValue("Green Fields");
        mapSelector.setMaxWidth(Double.MAX_VALUE);
        mapSelector.setOnAction(e -> tempChar.setCurrentMap(mapSelector.getValue()));
        lblTargetMonster = new Label("Searching...");
        lblTargetMonster.setFont(Font.font("System", FontWeight.BOLD, 14));
        lblTargetMonster.setStyle("-fx-text-fill: darkred;");
        combatBox.getChildren().addAll(new Label("Location:"), mapSelector, new Separator(), lblTargetMonster);

        VBox logBox = createPanel("Adventure Log");
        combatLog = new TextArea();
        combatLog.setEditable(false); combatLog.setWrapText(true);
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
        pbAction = new ProgressBar(0); pbAction.setMaxWidth(Double.MAX_VALUE);
        HBox xpBox = new HBox(5);
        Label lblXp = new Label("XP:");
        pbExp = new ProgressBar(0); pbExp.setMaxWidth(Double.MAX_VALUE); HBox.setHgrow(pbExp, Priority.ALWAYS);
        pbExp.setStyle("-fx-accent: green;");
        xpBox.getChildren().addAll(lblXp, pbExp);
        bottom.getChildren().addAll(lblCurrentAction, pbAction, xpBox);
        root.setBottom(bottom);

        idleEngine.setCallbacks(
                msg -> Platform.runLater(() -> { combatLog.appendText(msg + "\n"); combatLog.setScrollTop(Double.MAX_VALUE); }),
                () -> Platform.runLater(this::updateIdleUI)
        );
        idleEngine.start();
        updateIdleUI();

        window.setScene(new Scene(root, 900, 700));
        window.setOnCloseRequest(e -> { idleEngine.stop(); Platform.exit(); System.exit(0); });
    }

    private VBox createPanel(String title) {
        VBox box = new VBox(2);
        box.setStyle("-fx-border-color: gray; -fx-border-width: 1; -fx-background-color: #f4f4f4;");
        Label lbl = new Label(title);
        lbl.setFont(Font.font("System", FontWeight.BOLD, 12));
        lbl.setStyle("-fx-background-color: #ddd; -fx-padding: 2;");
        lbl.setMaxWidth(Double.MAX_VALUE);
        box.getChildren().add(lbl);
        return box;
    }

    private void updateIdleUI() {
        lblName.setText(tempChar.getName());
        lblRaceClass.setText(tempChar.getRace() + " " + tempChar.getClazz());
        lblLevel.setText("Level " + tempChar.getLevel());

        int points = tempChar.getAttributePoints();
        lblUnspentPoints.setText(points > 0 ? "POINTS AVAILABLE: " + points : "");

        statsContainer.getChildren().clear();
        tempChar.getAttributes().getAll().forEach((key, val) -> {
            HBox row = new HBox(10); row.setAlignment(Pos.CENTER_LEFT);
            row.getChildren().add(new Label(key + ": " + val));
            if (points > 0) {
                Button btnAdd = new Button("+");
                btnAdd.setStyle("-fx-font-size: 10; -fx-padding: 2 6; -fx-base: lightgreen;");
                btnAdd.setOnAction(e -> { tempChar.spendAttributePoint(key); updateIdleUI(); });
                row.getChildren().add(btnAdd);
            }
            statsContainer.getChildren().add(row);
        });

        double hpPerc = (double) tempChar.getHpCurrent() / tempChar.getHpMax();
        pbHP.setProgress(hpPerc);
        lblHPText.setText("HP: " + tempChar.getHpCurrent() + "/" + tempChar.getHpMax());
        double mpPerc = (double) tempChar.getMpCurrent() / tempChar.getMpMax();
        pbMP.setProgress(mpPerc);
        lblMPText.setText("MP: " + tempChar.getMpCurrent() + "/" + tempChar.getMpMax());

        Monster m = idleEngine.getCurrentMonster();
        lblTargetMonster.setText(m != null ? "Fighting: " + m.getName() + " (HP: " + m.getCurrentHp() + "/" + m.getMaxHp() + ")" : "Searching...");

        pbExp.setProgress((double)tempChar.getExperience() / tempChar.xpToNextLevel());
        pbAction.setProgress((double)idleEngine.getActionProgress() / idleEngine.getActionMax());
        lblCurrentAction.setText(idleEngine.getCurrentAction());

        listSpells.getItems().setAll(tempChar.getSpellBook());
        listEquip.getItems().clear();
        tempChar.getEquipment().forEach((slot, item) -> listEquip.getItems().add(slot + ": " + item.getDisplayString()));
        listInv.getItems().clear();
        for(Item i : tempChar.getInventory()) listInv.getItems().add(i.toString());
        listQuests.getItems().clear();
        if(tempChar.getCurrentQuest() != null) {
            listQuests.getItems().add(tempChar.getCurrentQuest().getTitle() + " (" + tempChar.getCurrentQuest().getCurrentKills() + "/" + tempChar.getCurrentQuest().getRequiredKills() + ")");
        }
    }
}