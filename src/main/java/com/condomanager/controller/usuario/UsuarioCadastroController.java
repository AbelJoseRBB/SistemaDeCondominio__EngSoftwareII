package com.condomanager.controller.usuario;

import com.condomanager.service.UsuarioService;
import com.condomanager.util.NavigationUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * Controller do formulario de cadastro de novo usuario / perfil de acesso.
 */
public class UsuarioCadastroController {

    @FXML private TextField nomeField;
    @FXML private TextField usuarioField;
    @FXML private TextField emailField;
    @FXML private PasswordField senhaField;
    @FXML private ComboBox<String> perfilComboBox;
    @FXML private Label erroLabel;
    @FXML private Button btnCadastrar;

    private final UsuarioService usuarioService = new UsuarioService();

    @FXML
    public void initialize() {
        if (perfilComboBox != null) {
            perfilComboBox.setItems(FXCollections.observableArrayList("OPERADOR", "ADMIN"));
            perfilComboBox.setValue("OPERADOR");
        }

        // Limpa erros visuais ao digitar
        configurarLimpezaErros(nomeField);
        configurarLimpezaErros(usuarioField);
        configurarLimpezaErros(emailField);
        configurarLimpezaErros(senhaField);
    }

    private void configurarLimpezaErros(TextInputControl field) {
        if (field != null) {
            field.textProperty().addListener((obs, oldVal, newVal) -> {
                field.getStyleClass().remove("field-error");
                if (erroLabel != null && erroLabel.isVisible()) {
                    erroLabel.setVisible(false);
                    erroLabel.setManaged(false);
                }
            });
        }
    }

    @FXML
    private void handleCadastrar() {
        limparEstilosErro();

        String nome = nomeField.getText() != null ? nomeField.getText().trim() : "";
        String usuario = usuarioField.getText() != null ? usuarioField.getText().trim() : "";
        String email = emailField.getText() != null ? emailField.getText().trim() : "";
        String senha = senhaField.getText();
        String perfil = perfilComboBox != null && perfilComboBox.getValue() != null
                ? perfilComboBox.getValue()
                : "OPERADOR";

        try {
            usuarioService.cadastrar(nome, usuario, senha, email, perfil);

            // Sucesso! Exibe confirmacao
            mostrarConfirmacaoSucesso(usuario);

        } catch (IllegalArgumentException ex) {
            marcarErro(ex.getMessage());
        } catch (Exception ex) {
            marcarErro("Erro inesperado ao salvar: " + ex.getMessage());
        }
    }

    private void marcarErro(String mensagem) {
        if (erroLabel != null) {
            erroLabel.setText(mensagem);
            erroLabel.setVisible(true);
            erroLabel.setManaged(true);
        }

        String msgLower = mensagem != null ? mensagem.toLowerCase() : "";
        if (msgLower.contains("nome")) {
            nomeField.getStyleClass().add("field-error");
            nomeField.requestFocus();
        } else if (msgLower.contains("usuário") || msgLower.contains("usuario") || msgLower.contains("login")) {
            usuarioField.getStyleClass().add("field-error");
            usuarioField.requestFocus();
        } else if (msgLower.contains("e-mail") || msgLower.contains("email")) {
            emailField.getStyleClass().add("field-error");
            emailField.requestFocus();
        } else if (msgLower.contains("senha")) {
            senhaField.getStyleClass().add("field-error");
            senhaField.requestFocus();
        }
    }

    private void limparEstilosErro() {
        if (erroLabel != null) {
            erroLabel.setVisible(false);
            erroLabel.setManaged(false);
        }
        if (nomeField != null) nomeField.getStyleClass().remove("field-error");
        if (usuarioField != null) usuarioField.getStyleClass().remove("field-error");
        if (emailField != null) emailField.getStyleClass().remove("field-error");
        if (senhaField != null) senhaField.getStyleClass().remove("field-error");
    }

    private void mostrarConfirmacaoSucesso(String usuarioCriado) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Cadastro Realizado");
        alert.setHeaderText("Usuario cadastrado com sucesso!");
        alert.setContentText("O perfil de acesso para '" + usuarioCriado + "' foi criado e ja pode ser utilizado para autenticacao.");

        try {
            alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm()
            );
        } catch (Exception ignored) {}

        alert.showAndWait();

        // Redireciona para o login
        handleVoltarLogin();
    }

    @FXML
    private void handleVoltarLogin() {
        NavigationUtil.navegar(
            "/fxml/Login.fxml",
            "CondoManager - Login",
            780, 720,
            false
        );
    }
}