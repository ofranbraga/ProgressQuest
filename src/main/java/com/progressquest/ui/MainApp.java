package com.progressquest.ui;

import com.progressquest.model.Character;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    private Stage window;

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
        CreationScreen creationScreen = new CreationScreen(window, this::startGame);
        creationScreen.show();
    }

    private void startGame(Character character, Boolean is2DMode) {
        if (is2DMode) {
            AdventureMode adventure = new AdventureMode(window, character, this::showCreationScreen);
            adventure.start();
        } else {
            IdleMode idle = new IdleMode(window, character, this::showCreationScreen);
            idle.start();
        }
    }
}