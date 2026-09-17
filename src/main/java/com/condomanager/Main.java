package com.condomanager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

/**
 * Classe principal da aplicacao.
 * Inicializa o JavaFX e carrega a tela de Login.
 * Apos o login, o LoginController carrega o MainLayout.fxml.
 */
public class Main extends Application {

    /** Stage principal da aplicacao, acessivel pelos controllers via getStage(). */
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/fxml/Login.fxml")
        );
        Parent root = loader.load();

        primaryStage.setTitle("CondoManager");
        primaryStage.setScene(new Scene(root, 480, 360));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    /**
     * Retorna o Stage principal para ser usado pelos controllers
     * ao trocar de tela (Login → Dashboard e vice-versa).
     */
    public static Stage getStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

