package com.petapp;

import com.petapp.ui.MainApp;
import javafx.application.Application;

/**
 * Application entry point.
 * Delegates immediately to the JavaFX {@link MainApp}.
 *
 * Run via Maven:
 *   mvn javafx:run
 */
public class Main {
    public static void main(String[] args) {
        Application.launch(MainApp.class, args);
    }
}
