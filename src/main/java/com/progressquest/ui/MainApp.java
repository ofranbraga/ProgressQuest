package com.progressquest.ui;

import com.progressquest.data.GameData;
import com.progressquest.engine.GameEngine;
import com.progressquest.model.Attributes;
import com.progressquest.model.Character;
import com.progressquest.model.Item;
import com.progressquest.model.Monster;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.Map;

public class MainApp extends Application {

    private Stage window;
    private Character tempChar;
    private GameEngine engine;

    //interface do jogo
    private Label lblName, lblRaceClass, lblLevel;
    private Label lblUnspentPoints; //aviso de pontos
    private VBox statsContainer; //container dos atributos

    //combate
    private ProgressBar pbHP, pbMP, pbExp, pbAction;
    private Label lblHPText, lblMPText, lblCurrentAction;
    private TextArea combatLog;
    private ComboBox<String> mapSelector;
    private Label lblTargetMonster;

    private ListView<String> listEquip, listInv, listSpells, listQuests;

    private int creationPoints = 25; //pontos iniciais
    private final int BASE_STAT = 8; //valor mínimo

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

    private void showCreationScreen() {
        tempChar = new Character(new Attributes());
        tempChar.getAttributes().setAll(BASE_STAT); //inicia tudo em 8
        this.creationPoints = 25; //reinicia saldo

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        //topo
        HBox topBox = new HBox(10);
        TextField tfName = new TextField("Vuckmoot");
        topBox.getChildren().addAll(new Label("Name:"), tfName);
        root.setTop(topBox);

        //centro
        GridPane centerGrid = new GridPane();
        centerGrid.setHgap(10); centerGrid.setVgap(10);
        centerGrid.setPadding(new Insets(10, 0, 10, 0));

        //raças
        VBox raceBox = createSelectionBox("Race", GameData.RACES);
        ToggleGroup raceGroup = (ToggleGroup) raceBox.getProperties().get("group");
        ((RadioButton)raceBox.getChildren().get(1)).setSelected(true);

        //classes
        VBox classBox = createSelectionBox("Class", GameData.CLASSES);
        ToggleGroup classGroup = (ToggleGroup) classBox.getProperties().get("group");
        ((RadioButton)classBox.getChildren().get(1)).setSelected(true);

        //PAINEL DE ATRIBUTOS
        VBox statsBox = new VBox(5);
        statsBox.setStyle("-fx-border-color: lightgray; -fx-padding: 5;");
        statsBox.getChildren().add(new Label("Stats (Point Buy)"));

        Label lblPoints = new Label("Points Left: " + creationPoints);
        lblPoints.setFont(Font.font("System", FontWeight.BOLD, 14));
        lblPoints.setStyle("-fx-text-fill: blue;");
        statsBox.getChildren().add(lblPoints);

        //container das linhas (+/-)
        VBox statsRows = new VBox(5);
        refreshCreationStats(statsRows, lblPoints);
        statsBox.getChildren().add(statsRows);

        //botao de começar o jogo
        Button btnSold = new Button("Start Game!");
        btnSold.setMaxWidth(Double.MAX_VALUE);
        btnSold.setFont(Font.font("System", FontWeight.BOLD, 14));
        btnSold.setOnAction(e -> {
            if (creationPoints > 0) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "You still have points to spend!");
                alert.showAndWait();
            } else {
                RadioButton rbRace = (RadioButton) raceGroup.getSelectedToggle();
                RadioButton rbClass = (RadioButton) classGroup.getSelectedToggle();
                if (rbRace != null && rbClass != null) {
                    tempChar.init(tfName.getText(), rbRace.getText(), rbClass.getText());
                    startGame();
                }
            }
        });

        statsBox.getChildren().addAll(new Separator(), btnSold);

        //layout
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

    //cria os RadioButtons
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

    //logica visual dos botoes de criacao
    private void refreshCreationStats(VBox container, Label lblPoints) {
        container.getChildren().clear();
        Map<String, Integer> stats = tempChar.getAttributes().getAll();

        stats.forEach((key, val) -> {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);

            Label name = new Label(key); name.setPrefWidth(40);

            Button btnMinus = new Button("-");
            btnMinus.setDisable(val <= BASE_STAT); //nao pode descer da base
            btnMinus.setOnAction(e -> {
                tempChar.getAttributes().decrement(key);
                creationPoints++;
                lblPoints.setText("Points Left: " + creationPoints);
                refreshCreationStats(container, lblPoints);
            });

            Label value = new Label(String.valueOf(val));
            value.setPrefWidth(30); value.setAlignment(Pos.CENTER);

            Button btnPlus = new Button("+");
            btnPlus.setDisable(creationPoints <= 0); //trava se acabou pontos
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

    private void startGame() {
        engine = new GameEngine(tempChar);
        BorderPane root = new BorderPane();

        //FICHA
        VBox col1 = new VBox(5); col1.setPadding(new Insets(5));

        VBox charSheet = createPanel("Character Sheet");
        lblName = new Label();
        lblRaceClass = new Label();
        lblLevel = new Label();

        //label de aviso de pontos
        lblUnspentPoints = new Label();
        lblUnspentPoints.setStyle("-fx-text-fill: blue; -fx-font-weight: bold;");

        //container onde os atributos serão desenhados
        statsContainer = new VBox(2);

        charSheet.getChildren().addAll(lblName, lblRaceClass, lblLevel, new Separator(), lblUnspentPoints, statsContainer);

        VBox spellBox = createPanel("Spell Book");
        listSpells = new ListView<>();
        spellBox.getChildren().add(listSpells);
        VBox.setVgrow(listSpells, Priority.ALWAYS);

        col1.getChildren().addAll(charSheet, spellBox);
        VBox.setVgrow(spellBox, Priority.ALWAYS);

        //COMBATE
        VBox col2 = new VBox(5); col2.setPadding(new Insets(5));

        //vitals
        VBox vitalBox = createPanel("Vitals");
        pbHP = new ProgressBar(1.0); pbHP.setMaxWidth(Double.MAX_VALUE); pbHP.setStyle("-fx-accent: red;");
        lblHPText = new Label("HP: ?/?");
        pbMP = new ProgressBar(1.0); pbMP.setMaxWidth(Double.MAX_VALUE); pbMP.setStyle("-fx-accent: blue;");
        lblMPText = new Label("MP: ?/?");
        vitalBox.getChildren().addAll(new Label("Health"), pbHP, lblHPText, new Label("Mana"), pbMP, lblMPText);

        //ZONA DE COMBATE
        VBox combatBox = createPanel("Combat Zone");
        mapSelector = new ComboBox<>();
        mapSelector.getItems().addAll(GameData.MAPS.keySet());
        mapSelector.setValue("Green Fields");
        mapSelector.setMaxWidth(Double.MAX_VALUE);
        mapSelector.setOnAction(e -> {
            tempChar.setCurrentMap(mapSelector.getValue());
            if(combatLog != null) combatLog.appendText("\nTraveling to " + mapSelector.getValue() + "...\n");
        });

        lblTargetMonster = new Label("Searching...");
        lblTargetMonster.setFont(Font.font("System", FontWeight.BOLD, 14));
        lblTargetMonster.setStyle("-fx-text-fill: darkred;");
        combatBox.getChildren().addAll(new Label("Location:"), mapSelector, new Separator(), lblTargetMonster);

        //LOG
        VBox logBox = createPanel("Adventure Log");
        combatLog = new TextArea();
        combatLog.setEditable(false);
        combatLog.setWrapText(true);
        logBox.getChildren().add(combatLog);
        VBox.setVgrow(logBox, Priority.ALWAYS);
        VBox.setVgrow(combatLog, Priority.ALWAYS);

        col2.getChildren().addAll(vitalBox, combatBox, logBox);
        VBox.setVgrow(logBox, Priority.ALWAYS);

        //INVENTÁRIO
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
        VBox.setVgrow(questBox, Priority.ALWAYS);
        VBox.setVgrow(equipBox, Priority.ALWAYS);
        VBox.setVgrow(invBox, Priority.ALWAYS);

        //LAYOUT
        GridPane grid = new GridPane();
        grid.add(col1, 0, 0);
        grid.add(col2, 1, 0);
        grid.add(col3, 2, 0);
        ColumnConstraints c = new ColumnConstraints(); c.setPercentWidth(33.3);
        grid.getColumnConstraints().addAll(c, c, c);
        root.setCenter(grid);

        //BARRA INFERIOR
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

        engine.setCallbacks(
                msg -> Platform.runLater(() -> {
                    combatLog.appendText(msg + "\n");
                    combatLog.setScrollTop(Double.MAX_VALUE);
                }),
                () -> Platform.runLater(this::updateGameUI)
        );

        engine.start();
        updateGameUI();

        window.setScene(new Scene(root, 900, 700));
        window.setOnCloseRequest(e -> { engine.stop(); Platform.exit(); System.exit(0); });
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

    private void updateGameUI() {
        lblName.setText(tempChar.getName());
        lblRaceClass.setText(tempChar.getRace() + " " + tempChar.getClazz());
        lblLevel.setText("Level " + tempChar.getLevel());

        int points = tempChar.getAttributePoints();
        if (points > 0) {
            lblUnspentPoints.setText("POINTS AVAILABLE: " + points);
        } else {
            lblUnspentPoints.setText("");
        }

        statsContainer.getChildren().clear();
        tempChar.getAttributes().getAll().forEach((key, val) -> {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            Label lblStat = new Label(String.format("%s: %d", key, val));
            row.getChildren().add(lblStat);

            //só mostra o botão se tiver pontos
            if (points > 0) {
                Button btnAdd = new Button("+");
                btnAdd.setStyle("-fx-font-size: 10; -fx-padding: 2 6; -fx-base: lightgreen;");
                btnAdd.setOnAction(e -> {
                    boolean success = tempChar.spendAttributePoint(key);
                    if (success) updateGameUI(); // Redesenha imediato
                });
                row.getChildren().add(btnAdd);
            }
            statsContainer.getChildren().add(row);
        });

        //barras
        double hpPerc = (double) tempChar.getHpCurrent() / tempChar.getHpMax();
        pbHP.setProgress(hpPerc);
        lblHPText.setText("HP: " + tempChar.getHpCurrent() + "/" + tempChar.getHpMax());

        double mpPerc = (double) tempChar.getMpCurrent() / tempChar.getMpMax();
        pbMP.setProgress(mpPerc);
        lblMPText.setText("MP: " + tempChar.getMpCurrent() + "/" + tempChar.getMpMax());

        //monstro
        Monster m = engine.getCurrentMonster();
        if (m != null) {
            lblTargetMonster.setText("Fighting: " + m.getName() + " (HP: " + m.getCurrentHp() + "/" + m.getMaxHp() + ")");
        } else {
            lblTargetMonster.setText("Searching...");
        }

        pbExp.setProgress((double)tempChar.getExperience() / tempChar.xpToNextLevel());
        pbAction.setProgress((double)engine.getActionProgress() / engine.getActionMax());
        lblCurrentAction.setText(engine.getCurrentAction());

        listSpells.getItems().setAll(tempChar.getSpellBook());

        listEquip.getItems().clear();
        tempChar.getEquipment().forEach((slot, item) ->
                listEquip.getItems().add(slot.toString() + ": " + item.getDisplayString()));

        listInv.getItems().clear();
        for(Item i : tempChar.getInventory()) listInv.getItems().add(i.toString());

        listQuests.getItems().clear();
        if(tempChar.getCurrentQuest() != null) {
            listQuests.getItems().add(tempChar.getCurrentQuest().getTitle() +
                    " (" + tempChar.getCurrentQuest().getCurrentKills() + "/" + tempChar.getCurrentQuest().getRequiredKills() + ")");
        }
    }
}