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
            getClass().getResource("/fxml/manutencao/ManutencaoList.fxml")
        );
        Parent root = loader.load();

        primaryStage.setTitle("CondoManager");
        // Aumentei um pouquinho a altura inicial de 360 para 420 para o botao Entrar nao ficar cortado
        primaryStage.setScene(new Scene(root, 780, 720));
        primaryStage.setResizable(false); // Permite redimensionar a janela

        // Filtro global de atalhos do teclado no Stage para Fullscreen (F11)
        primaryStage.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.F11) {
                primaryStage.setFullScreen(!primaryStage.isFullScreen());
                event.consume();
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

