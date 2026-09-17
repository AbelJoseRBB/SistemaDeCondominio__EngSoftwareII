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
        // Inicializa em modo janela com resolução confortável
        primaryStage.setScene(new Scene(root, 1024, 768));
        
        // Habilita redimensionamento e o botão de maximizar do Windows
        primaryStage.setResizable(true);

        // Filtro global de atalhos do teclado no Stage
        primaryStage.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.F11) {
                // Alterna o modo tela cheia
                primaryStage.setFullScreen(!primaryStage.isFullScreen());
                event.consume(); // Evita que o evento propague
            }
        });

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

