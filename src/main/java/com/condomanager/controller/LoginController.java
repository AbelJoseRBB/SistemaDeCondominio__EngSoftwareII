package com.condomanager.controller;

import com.condomanager.service.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller da tela de Login.
 * Vinculado ao arquivo Login.fxml via fx:controller.
 * A inicializacao da janela (Stage/Scene) e feita pela classe Main.
 */
public class LoginController {

    @FXML
    private TextField usuarioField;

    @FXML
    private PasswordField senhaField;

    @FXML
    private CheckBox manterConectadoCheckBox;

    private final AuthService authService = new AuthService();

    /**
     * Chamado automaticamente pelo FXMLLoader apos o FXML ser carregado.
     */
    @FXML
    public void initialize() {
        // Configuracoes iniciais da tela de login (se necessario)
    }

    /**
     * Acao executada ao clicar no botao "Entrar".
     * Autentica o usuario via AuthService (que usa UsuarioDAO + BCrypt).
     */
    @FXML
    private void handleLogin() {
        String login = usuarioField.getText().trim();
        String senha = senhaField.getText();

        if (login.isEmpty() || senha.isEmpty()) {
            return;
        }

        boolean autenticado = authService.autenticar(login, senha);

        if (autenticado) {
            // TODO (Commit 3): redirecionar para o Dashboard apos login bem-sucedido
            System.out.println("Login bem-sucedido para: " + login);
        } else {
            // TODO (Commit 2): exibir mensagem de erro na tela
            System.out.println("Credenciais invalidas para: " + login);
        }
    }

    /**
     * Acao executada ao clicar no link "Esqueci minha senha".
     */
    @FXML
    private void handleEsqueciSenha() {
        // TODO (Commit 4): abrir dialogo de recuperacao de senha por e-mail
        System.out.println("Recuperacao de senha solicitada.");
    }
}
