package com.progressquest.ui;

import com.progressquest.data.GameData;
import com.progressquest.engine.GameEngine;
import com.progressquest.model.Attributes;
import com.progressquest.model.Character;
import com.progressquest.model.Item;
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

    private Label lblName, lblRaceClass, lblLevel;
    private Label lblHP, lblMP, lblUnspentPoints;
    private VBox statsContainer;
    private ProgressBar pbExp, pbAction;
    private Label lblCurrentAction;
    private ListView<String> listEquip, listInv, listSpells, listQuests;

    private int creationPoints = 25; // pontos iniciais para distribuir
    private final int BASE_STAT = 8; // valor base de cada atributo

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.window = primaryStage;
        window.setTitle("Progress Quest - JavaFX");
        showCreationScreen();
        window.show();
    }

    // --- TELA DE CRIAÇÃO ---
    private void showCreationScreen() {
        //inicializa char com stats base 8
        tempChar = new Character(new Attributes());
        tempChar.getAttributes().setAll(BASE_STAT);
        this.creationPoints = 25; // Reseta pontos

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        //nome do personagem
        HBox topBox = new HBox(10);
        topBox.setAlignment(Pos.CENTER_LEFT);
        TextField tfName = new TextField("Vuckmoot");
        topBox.getChildren().addAll(new Label("Name:"), tfName);
        root.setTop(topBox);

        //grid de Raça, Classe e Stats
        GridPane centerGrid = new GridPane();
        centerGrid.setHgap(10);
        centerGrid.setVgap(10);
        centerGrid.setPadding(new Insets(10, 0, 10, 0));

        //raças
        VBox raceBox = createSelectionBox("Race", GameData.RACES);
        ToggleGroup raceGroup = (ToggleGroup) raceBox.getProperties().get("group");
        ((RadioButton)raceBox.getChildren().get(1)).setSelected(true); // Seleciona o primeiro (índice 0 é o label)

        //classes
        VBox classBox = createSelectionBox("Class", GameData.CLASSES);
        ToggleGroup classGroup = (ToggleGroup) classBox.getProperties().get("group");
        ((RadioButton)classBox.getChildren().get(1)).setSelected(true);

        //PAINEL DE ATRIBUTOS (INTERATIVO)
        VBox statsBox = new VBox(5);
        statsBox.setStyle("-fx-border-color: lightgray; -fx-padding: 5;");
        statsBox.getChildren().add(new Label("Stats Distribution"));

        Label lblPoints = new Label("Points: " + creationPoints);
        lblPoints.setFont(Font.font("System", FontWeight.BOLD, 14));
        statsBox.getChildren().add(lblPoints);

        //cria linhas para cada atributo
        VBox statsRows = new VBox(5);
        statsRows.setId("statsRows"); // ID para buscar se necessário

        //atualiza a UI inicial dos atributos
        refreshCreationStats(statsRows, lblPoints);
        statsBox.getChildren().add(statsRows);

        Button btnSold = new Button("Sold!");
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

        //layout Grid
        ScrollPane scrollRace = new ScrollPane(raceBox); scrollRace.setPrefHeight(300);
        ScrollPane scrollClass = new ScrollPane(classBox); scrollClass.setPrefHeight(300);

        centerGrid.add(scrollRace, 0, 0);
        centerGrid.add(scrollClass, 1, 0);
        centerGrid.add(statsBox, 2, 0);

        ColumnConstraints col = new ColumnConstraints(); col.setPercentWidth(33);
        centerGrid.getColumnConstraints().addAll(col, col, col);

        root.setCenter(centerGrid);
        window.setScene(new Scene(root, 700, 500));
    }

    private void refreshCreationStats(VBox container, Label lblPoints) {
        container.getChildren().clear();
        Map<String, Integer> stats = tempChar.getAttributes().getAll();

        stats.forEach((key, val) -> {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);

            Label name = new Label(key);
            name.setPrefWidth(40);

            Button btnMinus = new Button("-");
            btnMinus.setDisable(val <= BASE_STAT); //nao pode descer abaixo da base
            btnMinus.setOnAction(e -> {
                tempChar.getAttributes().decrement(key);
                creationPoints++;
                lblPoints.setText("Points: " + creationPoints);
                refreshCreationStats(container, lblPoints);
            });

            Label value = new Label(String.valueOf(val));
            value.setPrefWidth(30);
            value.setAlignment(Pos.CENTER);

            Button btnPlus = new Button("+");
            btnPlus.setDisable(creationPoints <= 0); //desabilita o botao para adicionar atributos caso acabe os pontos
            btnPlus.setOnAction(e -> {
                if (creationPoints > 0) {
                    tempChar.getAttributes().increment(key);
                    creationPoints--;
                    lblPoints.setText("Points: " + creationPoints);
                    refreshCreationStats(container, lblPoints);
                }
            });

            row.getChildren().addAll(name, btnMinus, value, btnPlus);
            container.getChildren().add(row);
        });
    }

    private VBox createSelectionBox(String title, java.util.List<String> options) {
        VBox box = new VBox(5);
        box.setStyle("-fx-border-color: lightgray; -fx-padding: 5;");
        box.getChildren().add(new Label(title));
        ToggleGroup group = new ToggleGroup();
        box.getProperties().put("group", group); //Salva o grupo no node para recuperar depois

        for (String opt : options) {
            RadioButton rb = new RadioButton(opt);
            rb.setToggleGroup(group);
            box.getChildren().add(rb);
        }
        return box;
    }

    //TELA DO JOGO
    private void startGame() {
        engine = new GameEngine(tempChar);
        BorderPane root = new BorderPane();

        //ficha
        VBox col1 = new VBox(5); col1.setPadding(new Insets(5));

        VBox charSheet = createPanel("Character Sheet");
        lblName = new Label();
        lblRaceClass = new Label();
        lblLevel = new Label();

        // Container onde os atributos (e botões de level up) ficarão
        statsContainer = new VBox(2);

        lblHP = new Label();
        lblMP = new Label();
        lblUnspentPoints = new Label();
        lblUnspentPoints.setStyle("-fx-text-fill: blue; -fx-font-weight: bold;");

        charSheet.getChildren().addAll(lblName, lblRaceClass, lblLevel,
                new Separator(), lblUnspentPoints, statsContainer,
                new Separator(), lblHP, lblMP);

        VBox expBox = new VBox(new Label("Experience"), pbExp = new ProgressBar(0));
        pbExp.setMaxWidth(Double.MAX_VALUE);

        VBox spellBox = createPanel("Spell Book");
        listSpells = new ListView<>();
        spellBox.getChildren().add(listSpells);
        VBox.setVgrow(listSpells, Priority.ALWAYS);

        col1.getChildren().addAll(charSheet, expBox, spellBox);
        VBox.setVgrow(spellBox, Priority.ALWAYS);

        // COLUNA 2: Equipamento
        VBox col2 = new VBox(5); col2.setPadding(new Insets(5));
        VBox equipBox = createPanel("Equipment");
        listEquip = new ListView<>();
        equipBox.getChildren().add(listEquip);
        VBox.setVgrow(listEquip, Priority.ALWAYS);

        VBox invBox = createPanel("Inventory");
        listInv = new ListView<>();
        invBox.getChildren().add(listInv);
        VBox.setVgrow(listInv, Priority.ALWAYS);
        col2.getChildren().addAll(equipBox, invBox);
        VBox.setVgrow(equipBox, Priority.ALWAYS); VBox.setVgrow(invBox, Priority.ALWAYS);

        //quests
        VBox col3 = new VBox(5); col3.setPadding(new Insets(5));
        VBox plotBox = createPanel("Plot Development");
        plotBox.getChildren().add(new Label("Prologue"));

        VBox questBox = createPanel("Quests");
        listQuests = new ListView<>();
        questBox.getChildren().add(listQuests);
        VBox.setVgrow(listQuests, Priority.ALWAYS);
        col3.getChildren().addAll(plotBox, questBox);
        VBox.setVgrow(questBox, Priority.ALWAYS);

        //layout Principal
        GridPane grid = new GridPane();
        grid.add(col1, 0, 0); grid.add(col2, 1, 0); grid.add(col3, 2, 0);
        ColumnConstraints c = new ColumnConstraints(); c.setPercentWidth(33.3);
        grid.getColumnConstraints().addAll(c, c, c);
        root.setCenter(grid);

        //barra Inferior
        VBox bottom = new VBox(2); bottom.setPadding(new Insets(5));
        lblCurrentAction = new Label("Executing...");
        pbAction = new ProgressBar(0); pbAction.setMaxWidth(Double.MAX_VALUE);
        bottom.getChildren().addAll(lblCurrentAction, pbAction);
        root.setBottom(bottom);

        //configura o engine
        engine.setCallbacks(
                msg -> Platform.runLater(() -> {}),
                () -> Platform.runLater(this::updateGameUI)
        );

        engine.start();
        updateGameUI();

        window.setScene(new Scene(root, 850, 650));
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
        //dados básicos
        lblName.setText(tempChar.getName());
        lblRaceClass.setText(tempChar.getRace() + " " + tempChar.getClazz());
        lblLevel.setText("Level " + tempChar.getLevel());

        //se houver pontos sobrando, mostra botões e aviso
        int points = tempChar.getAttributePoints();
        if (points > 0) {
            lblUnspentPoints.setText("Points Available: " + points + "!");
        } else {
            lblUnspentPoints.setText("");
        }

        //reconstrói a lista de stats para incluir ou remover botões, caso seja necessário
        statsContainer.getChildren().clear();
        tempChar.getAttributes().getAll().forEach((key, val) -> {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            Label lblStat = new Label(String.format("%s: %d", key, val));
            row.getChildren().add(lblStat);

            // Adiciona botão [+] se tiver pontos
            if (points > 0) {
                Button btnAdd = new Button("+");
                btnAdd.setStyle("-fx-font-size: 9; -fx-padding: 2 5;");
                btnAdd.setOnAction(e -> {
                    boolean success = tempChar.spendAttributePoint(key);
                    if (success) {
                        updateGameUI(); // Atualiza a tela imediatamente
                    }
                });
                row.getChildren().add(btnAdd);
            }
            statsContainer.getChildren().add(row);
        });

        lblHP.setText("HP Max " + tempChar.getHpMax());
        lblMP.setText("MP Max " + tempChar.getMpMax());

        //barras
        pbExp.setProgress((double)tempChar.getExperience() / tempChar.xpToNextLevel());
        pbAction.setProgress((double)engine.getActionProgress() / engine.getActionMax());
        lblCurrentAction.setText(engine.getCurrentAction());

        //listas
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