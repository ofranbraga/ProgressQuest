package com.progressquest.ui;

import com.progressquest.engine.GameEngine;
import com.progressquest.model.Attributes;
import com.progressquest.model.Item;
import com.progressquest.model.Monster;
import com.progressquest.model.Quest;
import com.progressquest.model.Character;
import com.progressquest.util.RandomNameGenerator;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainApp extends Application {
    private final Random rnd = new Random();
    private ScheduledExecutorService scheduler;
    private Character hero;
    private Label lblStatus;
    private ListView<String> lvInventory;
    private Button btnStartStop;
    private boolean running = false;

    @Override
    public void start(Stage primaryStage) {
        // Criar herói (pode ser substituído carregando arquivo)
        Attributes attrs = new Attributes(5, 5, 5, 5, 5);
        hero = new Character("Herói", "Humano", "Aventureiro", attrs);
        hero.setCurrentQuest(new Quest("Primeira Missão", "Mate 3 monstros", 3, 150, null));

        lblStatus = new Label();
        lvInventory = new ListView<>();
        btnStartStop = new Button("Iniciar");

        btnStartStop.setOnAction(e -> {
            if (!running) startSimulation(); else stopSimulation();
        });

        Button btnKillNow = new Button("Forçar Monstro");
        btnKillNow.setOnAction(e -> {
            doEncounter();
            updateUI();
        });

        HBox controls = new HBox(8, btnStartStop, btnKillNow);
        VBox root = new VBox(10, lblStatus, lvInventory, controls);
        root.setPadding(new Insets(12));
        VBox.setVgrow(lvInventory, Priority.ALWAYS);

        updateUI();

        Scene scene = new Scene(root, 640, 420);
        primaryStage.setTitle("Progress Quest - UI (JavaFX)");
        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.setOnCloseRequest(e -> {
            stopSimulation();
            Platform.exit();
            System.exit(0);
        });
    }

    private void startSimulation() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::tick, 0, 2, TimeUnit.SECONDS);
        running = true;
        Platform.runLater(() -> btnStartStop.setText("Parar"));
    }

    private void stopSimulation() {
        if (scheduler != null) scheduler.shutdownNow();
        running = false;
        Platform.runLater(() -> btnStartStop.setText("Iniciar"));
    }

    private void tick() {
        try {
            if (hero.getCurrentQuest() != null && !hero.getCurrentQuest().isCompleted()) {
                if (rnd.nextDouble() < 0.7) {
                    doEncounter();
                } else {
                    doFindItem();
                }
            } else {
                if (rnd.nextDouble() < 0.5) {
                    hero.setCurrentQuest(new Quest("Missão Gerada", "Mate 4 monstros", 4, 220, null));
                }
            }
            Platform.runLater(this::updateUI);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void doEncounter() {
        Monster m = spawnMonster();
        long xp = m.getRewardXP();
        hero.gainExperience(xp);
        if (hero.getCurrentQuest() != null) hero.getCurrentQuest().registerKill();
        if (hero.getCurrentQuest() != null && hero.getCurrentQuest().isCompleted()) {
            hero.gainExperience(hero.getCurrentQuest().getRewardXP());
            hero.setCurrentQuest(new Quest("Nova Missão", "Mate 5 monstros", 5, 350, null));
        }
    }

    private void doFindItem() {
        Item it = generateRandomItem();
        hero.addItem(it);
    }

    private Monster spawnMonster() {
        String name = RandomNameGenerator.randomMonsterName();
        int level = Math.max(1, hero.getLevel() + (int) (rnd.nextGaussian() * 2));
        long rewardXP = 15 + level * 12;
        return new Monster(name, Math.max(1, level), rewardXP, 0.15);
    }

    private Item generateRandomItem() {
        String name = RandomNameGenerator.randomItemName();
        Item.Type type = rnd.nextBoolean() ? Item.Type.WEAPON : Item.Type.ARMOR;
        Attributes bonus = new Attributes(rnd.nextInt(3), rnd.nextInt(3), rnd.nextInt(3), 0, 0);
        return new Item(name, type, bonus);
    }

    private void updateUI() {
        lblStatus.setText(hero.status());
        lvInventory.getItems().clear();
        for (Item it : hero.getInventory()) {
            String s = it.getName() + " (" + it.getType() + ") ";
            lvInventory.getItems().add(s);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
