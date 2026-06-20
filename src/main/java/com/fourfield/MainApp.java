package com.fourfield;

import com.fourfield.view.MenuView;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Ponto de entrada principal da aplicação Four Field Kono.
 */
public class MainApp extends Application {

    /** Abre o menu inicial quando a aplicação arranca. */
    @Override
    public void start(Stage primaryStage) {
        MenuView menu = new MenuView(primaryStage);
        menu.show();
    }

    /** Arranca a aplicação JavaFX. */
    public static void main(String[] args) {
        launch(args);
    }
}
