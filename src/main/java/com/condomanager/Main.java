package com.condomanager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Classe principal da aplicacao.
 * Inicializa o JavaFX e carrega a tela de Login.
 * Apos o login, o LoginController carrega o MainLayout.fxml.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/fxml/Login.fxml")
        );
        Parent root = loader.load();

        primaryStage.setTitle("CondoManager");
        primaryStage.setScene(new Scene(root, 480, 340));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
