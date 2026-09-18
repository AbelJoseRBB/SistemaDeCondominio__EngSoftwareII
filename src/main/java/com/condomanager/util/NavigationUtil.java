package com.condomanager.util;

import com.condomanager.Main;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Utilitario para navegacao entre telas da aplicacao.
 * Centraliza a logica de troca de cena no Stage principal.
 */
public class NavigationUtil {

    private NavigationUtil() {}

    /**
     * Navega para uma tela FXML, substituindo a cena atual do Stage principal.
     *
     * @param fxmlPath   caminho do FXML (ex: "/fxml/MainLayout.fxml")
     * @param titulo     titulo da janela
     * @param largura    largura da janela
     * @param altura     altura da janela
     * @param redimensionavel se a janela pode ser redimensionada
     */
    public static void navegar(String fxmlPath, String titulo,
                                double largura, double altura,
                                boolean redimensionavel) {
        try {
            FXMLLoader loader = new FXMLLoader(
                NavigationUtil.class.getResource(fxmlPath)
            );
            Parent root = loader.load();

            Stage stage = Main.getStage();
            if (stage != null) {
                stage.setTitle(titulo);
                stage.setScene(new Scene(root, largura, altura));
                stage.setResizable(redimensionavel);
                stage.show();
            }
        } catch (Exception e) {
            System.err.println("Erro ao navegar para " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}