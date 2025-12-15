package com.progressquest.ui;

import com.progressquest.data.GameData;
import com.progressquest.engine.GameEngine;
import com.progressquest.model.Attributes;
import com.progressquest.model.Character;
import com.progressquest.model.Item;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.Map;

public class MainApp extends Application {

    private Stage window;
    private Character tempChar; //personagem sendo criado
    private GameEngine engine;

    private Label lblName, lblRaceClass, lblLevel;
    private Label lblStats, lblHP, lblMP;
    private ProgressBar pbExp, pbAction;
    private Label lblCurrentAction;
    private ListView<String> listEquip, listInv, listSpells, listQuests;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.window = primaryStage;
        window.setTitle("Progress Quest");

        //inicia na tela de criação
        showCreationScreen();
        window.show();
    }

    //TELA DE CRIAÇÃO
    private void showCreationScreen() {
        tempChar = new Character(new Attributes());
        tempChar.getAttributes().roll();

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        //nome
        HBox topBox = new HBox(10);
        TextField tfName = new TextField("Vuckmoot");
        topBox.getChildren().addAll(new Label("Name:"), tfName);
        root.setTop(topBox);

        //Raça, Classe, Stats
        GridPane centerGrid = new GridPane();
        centerGrid.setHgap(10);
        centerGrid.setVgap(10);
        centerGrid.setPadding(new Insets(10, 0, 10, 0));

        //Raças
        VBox raceBox = new VBox(5);
        raceBox.setStyle("-fx-border-color: lightgray; -fx-padding: 5;");
        raceBox.getChildren().add(new Label("Race"));
        ToggleGroup raceGroup = new ToggleGroup();
        for (String r : GameData.RACES) {
            RadioButton rb = new RadioButton(r);
            rb.setToggleGroup(raceGroup);
            if(r.equals("Double Hobbit")) rb.setSelected(true);
            raceBox.getChildren().add(rb);
        }

        ScrollPane scrollRace = new ScrollPane(raceBox);
        scrollRace.setPrefHeight(300);

        VBox classBox = new VBox(5);
        classBox.setStyle("-fx-border-color: lightgray; -fx-padding: 5;");
        classBox.getChildren().add(new Label("Class"));
        ToggleGroup classGroup = new ToggleGroup();
        for (String c : GameData.CLASSES) {
            RadioButton rb = new RadioButton(c);
            rb.setToggleGroup(classGroup);
            if(c.equals("Fighter/Organist")) rb.setSelected(true);
            classBox.getChildren().add(rb);
        }
        ScrollPane scrollClass = new ScrollPane(classBox);
        scrollClass.setPrefHeight(300);

        VBox statsBox = new VBox(5);
        statsBox.setStyle("-fx-border-color: lightgray; -fx-padding: 5;");
        statsBox.getChildren().add(new Label("Stats"));
        Label lblStatsDisplay = new Label();
        updateCreationStats(lblStatsDisplay);
        statsBox.getChildren().add(lblStatsDisplay);

        Button btnRoll = new Button("Roll");
        btnRoll.setMaxWidth(Double.MAX_VALUE);
        btnRoll.setOnAction(e -> {
            tempChar.getAttributes().roll();
            updateCreationStats(lblStatsDisplay);
        });

        Button btnSold = new Button("Sold!");
        btnSold.setMaxWidth(Double.MAX_VALUE);
        btnSold.setFont(Font.font("System", FontWeight.BOLD, 14));
        btnSold.setOnAction(e -> {
            //pega a raça e classe selecionadas
            RadioButton rbRace = (RadioButton) raceGroup.getSelectedToggle();
            RadioButton rbClass = (RadioButton) classGroup.getSelectedToggle();

            if (rbRace != null && rbClass != null) {
                tempChar.init(tfName.getText(), rbRace.getText(), rbClass.getText());
                startGame();
            }
        });

        statsBox.getChildren().addAll(new Separator(), btnRoll, new Region(), btnSold);

        centerGrid.add(scrollRace, 0, 0);
        centerGrid.add(scrollClass, 1, 0);
        centerGrid.add(statsBox, 2, 0);

        ColumnConstraints col1 = new ColumnConstraints(); col1.setPercentWidth(33);
        ColumnConstraints col2 = new ColumnConstraints(); col2.setPercentWidth(33);
        ColumnConstraints col3 = new ColumnConstraints(); col3.setPercentWidth(33);
        centerGrid.getColumnConstraints().addAll(col1, col2, col3);

        root.setCenter(centerGrid);
        window.setScene(new Scene(root, 600, 450));
    }

    private void updateCreationStats(Label lbl) {
        StringBuilder sb = new StringBuilder();
        Map<String, Integer> s = tempChar.getAttributes().getAll();
        s.forEach((k, v) -> sb.append(String.format("%s: %d\n", k, v)));
        sb.append("\nTotal: ").append(tempChar.getAttributes().getTotal());
        lbl.setText(sb.toString());
    }

    //TELA DO JOGO
    private void startGame() {
        engine = new GameEngine(tempChar);

        BorderPane root = new BorderPane();

        //FICHA E SPELLS
        VBox col1 = new VBox(5);
        col1.setPadding(new Insets(5));

        VBox charSheet = createPanel("Character Sheet");
        lblName = new Label();
        lblRaceClass = new Label();
        lblLevel = new Label();

        //Inicializando lblStats e outros labels
        lblStats = new Label();
        lblHP = new Label();
        lblMP = new Label();

        charSheet.getChildren().addAll(lblName, lblRaceClass, lblLevel, new Separator(), lblStats, new Separator(), lblHP, lblMP);

        VBox expBox = new VBox();
        expBox.getChildren().add(new Label("Experience"));
        pbExp = new ProgressBar(0);
        pbExp.setMaxWidth(Double.MAX_VALUE);
        expBox.getChildren().add(pbExp);

        VBox spellBox = createPanel("Spell Book");
        listSpells = new ListView<>();
        VBox.setVgrow(listSpells, Priority.ALWAYS);
        spellBox.getChildren().add(listSpells);

        col1.getChildren().addAll(charSheet, expBox, spellBox);
        VBox.setVgrow(spellBox, Priority.ALWAYS);

        //EQUIPAMENTO E INVENTARIO
        VBox col2 = new VBox(5);
        col2.setPadding(new Insets(5));

        VBox equipBox = createPanel("Equipment");
        listEquip = new ListView<>();
        VBox.setVgrow(listEquip, Priority.ALWAYS);
        equipBox.getChildren().add(listEquip);

        VBox invBox = createPanel("Inventory");
        listInv = new ListView<>();
        VBox.setVgrow(listInv, Priority.ALWAYS);
        invBox.getChildren().add(listInv);

        col2.getChildren().addAll(equipBox, invBox);
        VBox.setVgrow(equipBox, Priority.ALWAYS);
        VBox.setVgrow(invBox, Priority.ALWAYS);

        //PLOT E QUESTS
        VBox col3 = new VBox(5);
        col3.setPadding(new Insets(5));

        VBox plotBox = createPanel("Plot Development");
        Label lblPlot = new Label("Prologue");
        plotBox.getChildren().add(lblPlot);

        VBox questBox = createPanel("Quests");
        listQuests = new ListView<>();
        VBox.setVgrow(listQuests, Priority.ALWAYS);
        questBox.getChildren().add(listQuests);

        col3.getChildren().addAll(plotBox, questBox);
        VBox.setVgrow(questBox, Priority.ALWAYS);

        //LAYOUT PRINCIPAL
        GridPane grid = new GridPane();
        grid.add(col1, 0, 0);
        grid.add(col2, 1, 0);
        grid.add(col3, 2, 0);

        ColumnConstraints c = new ColumnConstraints(); c.setPercentWidth(33.3);
        grid.getColumnConstraints().addAll(c, c, c);

        root.setCenter(grid);

        //BARRA DE AÇÃO
        VBox bottom = new VBox(2);
        bottom.setPadding(new Insets(5));
        lblCurrentAction = new Label("Executing...");
        pbAction = new ProgressBar(0);
        pbAction.setMaxWidth(Double.MAX_VALUE);
        bottom.getChildren().addAll(lblCurrentAction, pbAction);
        root.setBottom(bottom);

        engine.setCallbacks(
                msg -> Platform.runLater(() -> {}),
                () -> Platform.runLater(this::updateGameUI)
        );

        engine.start();
        updateGameUI(); //primeira atualização

        window.setScene(new Scene(root, 800, 600));
        window.setOnCloseRequest(e -> {
            engine.stop();
            Platform.exit();
            System.exit(0);
        });
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
        //atualiza labels
        lblName.setText(tempChar.getName());
        lblRaceClass.setText(tempChar.getRace() + " " + tempChar.getClazz());
        lblLevel.setText("Level " + tempChar.getLevel());

        //stats
        StringBuilder sb = new StringBuilder();
        tempChar.getAttributes().getAll().forEach((k,v) -> sb.append(String.format("%s %d\n", k, v)));
        lblStats.setText(sb.toString()); // Uso da variável lblStats

        lblHP.setText("HP Max " + tempChar.getHpMax());
        lblMP.setText("MP Max " + tempChar.getMpMax());

        //bars
        pbExp.setProgress((double)tempChar.getExperience() / tempChar.xpToNextLevel());
        pbAction.setProgress((double)engine.getActionProgress() / engine.getActionMax());
        lblCurrentAction.setText(engine.getCurrentAction());

        //lists
        listSpells.getItems().setAll(tempChar.getSpellBook());

        listEquip.getItems().clear();
        tempChar.getEquipment().forEach((slot, item) ->
                listEquip.getItems().add(slot.toString() + ": " + item.getDisplayString()));

        listInv.getItems().clear();
        for(Item i : tempChar.getInventory()) listInv.getItems().add(i.toString());

        listQuests.getItems().clear();
        if(tempChar.getCurrentQuest() != null) {
            listQuests.getItems().add("[] " + tempChar.getCurrentQuest().getTitle());
        }
    }
}