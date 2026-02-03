package com.progressquest.ui;

import com.progressquest.model.Character;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main JavaFX entry point.
 *
 * <p>The game runs only in Idle mode. After character creation,
 * the game starts directly in IdleMode.</p>
 */
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

    private void startGame(Character character) {
        startGame(character, Boolean.FALSE);
    }

    // Legacy creation callback signature (Character, is2D). is2D is ignored.
    private void startGame(Character character, Boolean ignoredIs2D) {
        IdleMode idle = new IdleMode(window, character, this::showCreationScreen);
        idle.start();
    }
}
