package com.progressquest.ui;

import com.progressquest.data.GameData;
import com.progressquest.model.Character;
import com.progressquest.model.Item;
import com.progressquest.model.Monster;
import com.progressquest.model.Projectile;
import com.progressquest.util.RandomNameGenerator;
import javafx.animation.AnimationTimer;
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

public class AdventureMode {

    private final Stage stage;
    private final Character character;
    private final Runnable onExit;

    private Canvas gameCanvas;
    private AnimationTimer gameLoop;
    private Set<KeyCode> activeKeys = new HashSet<>();
    private List<Monster> activeMonsters = new ArrayList<>();
    private List<Projectile> activeProjectiles = new ArrayList<>();
    private long lastAttackTime = 0;
    private Random rand = new Random();

    //UI components
    private ProgressBar pb2DHP, pb2DMP, pb2DXP;
    private Label lbl2DInfo;
    private Label lblNotification;
    private InventoryOverlay inventoryOverlay;
    private StatusOverlay statusOverlay;

    public AdventureMode(Stage stage, Character character, Runnable onExit) {
        this.stage = stage;
        this.character = character;
        this.onExit = onExit;
    }

    public void start() {
        StackPane root = new StackPane();
        BorderPane gameLayout = new BorderPane();

        gameCanvas = new Canvas(800, 600);
        Pane gamePane = new Pane(gameCanvas);
        gamePane.setStyle("-fx-background-color: #228B22;");
        gameLayout.setCenter(gamePane);

        // HUD Inferior
        VBox hud = new VBox(5);
        hud.setPadding(new Insets(10));
        hud.setStyle("-fx-background-color: rgba(0,0,0,0.8);");

        HBox infoBox = new HBox(20);
        lbl2DInfo = new Label(character.getName() + " (Lvl " + character.getLevel() + ")");
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

        //notificação
        lblNotification = new Label("");
        lblNotification.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        lblNotification.setTextFill(Color.GOLD);
        lblNotification.setStyle("-fx-effect: dropshadow(one-pass-box, black, 2, 0.5, 0, 0);");
        StackPane.setAlignment(lblNotification, Pos.TOP_CENTER);
        StackPane.setMargin(lblNotification, new Insets(50, 0, 0, 0));

        root.getChildren().addAll(gameLayout, lblNotification);

        // Overlays
        createOverlays(root);

        Scene scene = new Scene(root, 800, 680);

        // Inputs
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.I) toggleInventory();
            else if (e.getCode() == KeyCode.C) toggleStatus();
            else if (e.getCode() == KeyCode.F11) stage.setFullScreen(!stage.isFullScreen());
            else activeKeys.add(e.getCode());
        });
        scene.setOnKeyReleased(e -> activeKeys.remove(e.getCode()));

        scene.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && !inventoryOverlay.isVisible() && !statusOverlay.isVisible()) {
                fireProjectile(e.getX(), e.getY());
            }
        });

        // Game Loop
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!inventoryOverlay.isVisible() && !statusOverlay.isVisible()) {
                    update2DGame();
                }
                render2DGame(gameCanvas.getGraphicsContext2D());
                update2DUI();

                if (character.isDead()) handleDeath();
            }
        };
        spawn2DMonsters();
        gameLoop.start();
        stage.setScene(scene);
    }

    private void createOverlays(StackPane root) {
        // Inventory Overlay
        inventoryOverlay = new InventoryOverlay(
                character,
                this::showNotification,
                () -> { activeMonsters.clear(); spawn2DMonsters(); },
                () -> inventoryOverlay.setVisible(false)
        );
        root.getChildren().add(inventoryOverlay);

        // Status Overlay
        statusOverlay = new StatusOverlay(
                character,
                () -> statusOverlay.setVisible(false)
        );
        root.getChildren().add(statusOverlay);
    }

    private void toggleInventory() {
        boolean opening = !inventoryOverlay.isVisible();
        inventoryOverlay.setVisible(opening);
        if(opening) {
            statusOverlay.setVisible(false);
            inventoryOverlay.refresh();
        }
    }

    private void toggleStatus() {
        boolean opening = !statusOverlay.isVisible();
        statusOverlay.setVisible(opening);
        if(opening) {
            inventoryOverlay.setVisible(false);
            statusOverlay.refresh();
        }
    }

    private void update2DUI() {
        lbl2DInfo.setText(character.getName() + " (Lvl " + character.getLevel() + ")");
        pb2DHP.setProgress((double)character.getHpCurrent() / character.getHpMax());
        pb2DMP.setProgress((double)character.getMpCurrent() / character.getMpMax());
        pb2DXP.setProgress((double)character.getExperience() / character.xpToNextLevel());
    }

    private void showNotification(String msg) {
        lblNotification.setText(msg);
    }

    private void fireProjectile(double targetX, double targetY) {
        if (character.getMpCurrent() >= 5) {
            character.regenMana(-5);
            double angle = Math.atan2(targetY - character.getY(), targetX - character.getX());
            double speed = 8.0;
            activeProjectiles.add(new Projectile(character.getX() + 16, character.getY() + 16, Math.cos(angle) * speed, Math.sin(angle) * speed));
        } else {
            showNotification("Not enough Mana!");
        }
    }

    private void spawn2DMonsters() {
        while (activeMonsters.size() < 5) {
            Monster m = new Monster(GameData.getMonsterFromMap(character.getCurrentMap()), character.getLevel());
            m.spawnRandomly(800, 600);
            activeMonsters.add(m);
        }
    }

    private Item generateLoot() {
        Item.Slot[] slots = Item.Slot.values();
        Item.Slot slot = slots[rand.nextInt(slots.length)];
        String name = RandomNameGenerator.randomItemName();
        int bonus = 1 + (character.getLevel() / 2) + rand.nextInt(3);
        return new Item(name, slot, bonus, "STR");
    }

    private void update2DGame() {
        //movimento
        double dx = 0, dy = 0;
        if (activeKeys.contains(KeyCode.W)) dy -= 1;
        if (activeKeys.contains(KeyCode.S)) dy += 1;
        if (activeKeys.contains(KeyCode.A)) dx -= 1;
        if (activeKeys.contains(KeyCode.D)) dx += 1;
        if (dx != 0 && dy != 0) { dx *= 0.707; dy *= 0.707; }
        character.move(dx, dy);

        //limites
        if (character.getX() < 0) character.setPosition(0, character.getY());
        if (character.getY() < 0) character.setPosition(character.getX(), 0);
        if (character.getX() > 768) character.setPosition(768, character.getY());
        if (character.getY() > 568) character.setPosition(character.getX(), 568);

        //projeteis
        Iterator<Projectile> projIt = activeProjectiles.iterator();
        while (projIt.hasNext()) {
            Projectile p = projIt.next();
            p.x += p.dx; p.y += p.dy;
            if (p.x < 0 || p.x > 800 || p.y < 0 || p.y > 600) projIt.remove();
            else {
                for (Monster m : activeMonsters) {
                    if (getDistance(p.x, p.y, m.getX(), m.getY()) < 30) {
                        int magicDmg = 5 + character.getAttributes().get("INT");
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
                    if (getDistance(character.getX(), character.getY(), m.getX(), m.getY()) < 60) {
                        m.takeDamage(character.calculateDamage());
                    }
                }
                lastAttackTime = now;
            }
        }

        //monstros e drops
        Iterator<Monster> it = activeMonsters.iterator();
        while (it.hasNext()) {
            Monster m = it.next();
            m.moveTowards(character.getX(), character.getY(), 1.0);

            if (getDistance(character.getX(), character.getY(), m.getX(), m.getY()) < 30) {
                character.takeDamage(1);
            }

            if (m.isDead()) {
                character.gainExperience(m.getRewardXP());
                if (rand.nextDouble() < 0.3) {
                    Item item = generateLoot();
                    character.getInventory().add(item);
                    showNotification("Looted: " + item.getName());
                }
                if (rand.nextDouble() < 0.1) {
                    String spell = GameData.getRandomSpell();
                    character.learnSpell(spell);
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
        gc.fillRect(character.getX(), character.getY(), 32, 32);
        gc.setStroke(Color.WHITE); gc.strokeRect(character.getX(), character.getY(), 32, 32);

        //projeteis
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

        //efeito combate
        if (System.currentTimeMillis() - lastAttackTime < 100) {
            gc.setStroke(Color.YELLOW); gc.setLineWidth(3);
            gc.strokeOval(character.getX() - 20, character.getY() - 20, 72, 72);
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
                character.heal(character.getHpMax());
                character.regenMana(character.getMpMax());
                activeMonsters.clear();
                activeProjectiles.clear();
                activeKeys.clear();
                gameLoop.start();
            } else {
                onExit.run();
            }
        });
    }

    private double getDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }
}