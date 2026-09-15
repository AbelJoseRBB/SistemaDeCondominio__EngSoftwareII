package com.condomanager.controller;

import com.condomanager.service.AuthService;
import com.condomanager.util.NavigationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TextField;

import java.util.Optional;

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
            NavigationUtil.navegar(
                "/fxml/MainLayout.fxml",
                "CondoManager",
                1280, 720,
                true
            );
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
     * Exibe um dialogo pedindo o e-mail e mostra confirmacao generica
     * (sem revelar se o e-mail esta ou nao cadastrado).
     */
    @FXML
    private void handleEsqueciSenha() {
        TextInputDialog dialogo = new TextInputDialog();
        dialogo.setTitle("Recuperacao de Senha");
        dialogo.setHeaderText("Esqueceu sua senha?");
        dialogo.setContentText("Informe seu e-mail cadastrado:");
        dialogo.getEditor().setPromptText("exemplo@email.com");

        // Estiliza o dialogo com o mesmo CSS da aplicacao
        dialogo.getDialogPane().getStylesheets().add(
            getClass().getResource("/css/style.css").toExternalForm()
        );

        Optional<String> resultado = dialogo.showAndWait();

        resultado.ifPresent(email -> {
            if (!email.trim().isEmpty() && email.contains("@")) {
                // TODO: integrar com servico de e-mail para envio real da recuperacao
                mostrarConfirmacaoEmail();
            } else {
                Alert aviso = new Alert(AlertType.WARNING);
                aviso.setTitle("E-mail invalido");
                aviso.setHeaderText(null);
                aviso.setContentText("Por favor, informe um e-mail valido.");
                aviso.showAndWait();
            }
        });
    }

    /**
     * Exibe alert de confirmacao apos solicitacao de recuperacao.
     * A mensagem e generica intencionalmente para nao revelar
     * se o e-mail esta ou nao cadastrado no sistema.
     */
    private void mostrarConfirmacaoEmail() {
        Alert confirmacao = new Alert(AlertType.INFORMATION, "", ButtonType.OK);
        confirmacao.setTitle("Solicitacao Enviada");
        confirmacao.setHeaderText("Verifique sua caixa de entrada");
        confirmacao.setContentText(
            "Se o e-mail informado estiver cadastrado, voce recebera\n" +
            "as instrucoes de recuperacao de senha em breve."
        );
        confirmacao.showAndWait();
    }
}
