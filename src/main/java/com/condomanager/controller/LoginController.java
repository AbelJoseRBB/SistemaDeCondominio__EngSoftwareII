package com.condomanager.controller;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller da tela de Login.
 * Vinculado ao arquivo Login.fxml.
 * A inicializacao da janela (Stage/Scene) e feita pela classe Main.
 */
public class LoginController {

    @FXML
    private TextField usuarioField;

    @FXML
    private PasswordField senhaField;

    @FXML
    private CheckBox manterConectadoCheckBox;

    /**
     * Chamado automaticamente pelo FXMLLoader apos o FXML ser carregado.
     * Use este metodo para configurar estado inicial dos componentes da tela.
     */
    @FXML
    public void initialize() {
        // Configuracoes iniciais da tela de login (se necessario)
    }

    /**
     * Acao executada ao clicar no botao "Entrar".
     */
    @FXML
    private void handleLogin() {
        String usuario = usuarioField != null ? usuarioField.getText() : "";
        String senha = senhaField != null ? senhaField.getText() : "";

        // TODO: Chamar AuthService para autenticar o usuario
        System.out.println("Tentando login com usuario: " + usuario);
    }

    /**
     * Acao executada ao clicar no link "Esqueci minha senha".
     */
    @FXML
    private void handleEsqueciSenha() {
        // TODO: Implementar recuperacao de senha
        System.out.println("Recuperacao de senha solicitada.");
    }
}
