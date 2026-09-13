package com.condomanager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Classe principal da aplicacao.
 * Ponto de entrada do sistema de gestao de condominio.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Carrega a tela de login ao iniciar
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/fxml/Login.fxml")
        );
        Parent root = loader.load();

        primaryStage.setTitle("CondoManager");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.setResizable(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

