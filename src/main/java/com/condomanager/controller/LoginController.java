package com.condomanager.controller;

import com.condomanager.service.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
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

    @FXML
    private Label erroLabel;

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

        // Esconde qualquer erro anterior e limpa borda de erro
        setErro(false);

        if (login.isEmpty() || senha.isEmpty()) {
            setErro(true);
            return;
        }

        boolean autenticado = authService.autenticar(login, senha);

        if (autenticado) {
            // TODO (Commit 3): redirecionar para o Dashboard apos login bem-sucedido
            System.out.println("Login bem-sucedido para: " + login);
        } else {
            setErro(true);
            senhaField.clear();
        }
    }

    /**
     * Exibe ou oculta a mensagem de erro e aplica/remove o estilo de borda vermelha nos campos.
     */
    private void setErro(boolean visivel) {
        erroLabel.setVisible(visivel);
        erroLabel.setManaged(visivel);
        if (visivel) {
            usuarioField.getStyleClass().add("field-error");
            senhaField.getStyleClass().add("field-error");
        } else {
            usuarioField.getStyleClass().remove("field-error");
            senhaField.getStyleClass().remove("field-error");
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
