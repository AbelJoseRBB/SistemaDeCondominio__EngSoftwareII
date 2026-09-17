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
     * Exibe um dialogo customizado com o exato mesmo estilo do Login.
     */
    @FXML
    private void handleEsqueciSenha() {
        javafx.scene.control.Dialog<String> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Recuperação de Senha");
        dialog.initOwner(usuarioField.getScene().getWindow());

        // Aplica o CSS global ao painel do dialogo
        dialog.getDialogPane().getStylesheets().add(
            getClass().getResource("/css/style.css").toExternalForm()
        );
        // Remove background padrao e seta branco
        dialog.getDialogPane().setStyle("-fx-background-color: white;");

        // Cria a estrutura visual (idêntica ao login-card)
        javafx.scene.layout.VBox card = new javafx.scene.layout.VBox(15);
        card.setAlignment(javafx.geometry.Pos.TOP_CENTER);
        card.setPadding(new javafx.geometry.Insets(20, 40, 20, 40));
        card.setPrefWidth(400);

        // Titulo e Subtitulo
        Label title = new Label("Recuperação de Senha");
        title.getStyleClass().add("title-label");

        Label subtitle = new Label("Informe seu e-mail cadastrado");
        subtitle.getStyleClass().add("subtitle-label");
        
        javafx.scene.layout.VBox header = new javafx.scene.layout.VBox(5, title, subtitle);
        header.setAlignment(javafx.geometry.Pos.CENTER);

        // Campo de E-mail
        TextField emailField = new TextField();
        emailField.setPromptText("exemplo@email.com");
        emailField.getStyleClass().add("text-field");

        // Botao Enviar (idêntico ao botão Entrar)
        javafx.scene.control.Button btnEnviar = new javafx.scene.control.Button("Enviar Instruções");
        btnEnviar.getStyleClass().add("login-button");
        btnEnviar.setMaxWidth(Double.MAX_VALUE);

        // Botao Cancelar (estilo link)
        javafx.scene.control.Hyperlink linkCancelar = new javafx.scene.control.Hyperlink("Voltar para o login");
        linkCancelar.getStyleClass().add("forgot-password-link");

        // Monta o layout
        card.getChildren().addAll(header, emailField, btnEnviar, linkCancelar);

        // Seta o conteudo no dialog
        dialog.getDialogPane().setContent(card);

        // O Dialog exige pelo menos um ButtonType para poder fechar nativamente
        // Adicionamos um botao invisível para permitir que a janela feche
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        javafx.scene.Node closeButton = dialog.getDialogPane().lookupButton(ButtonType.CLOSE);
        closeButton.setVisible(false);
        closeButton.setManaged(false);

        // Acoes dos nossos botoes customizados
        linkCancelar.setOnAction(e -> dialog.setResult(""));
        
        btnEnviar.setOnAction(e -> {
            String email = emailField.getText().trim();
            if (!email.isEmpty() && email.contains("@")) {
                dialog.setResult(email);
            } else {
                emailField.getStyleClass().add("field-error");
            }
        });

        // Exibe o dialogo
        Optional<String> resultado = dialog.showAndWait();

        resultado.ifPresent(email -> {
            if (!email.isEmpty()) {
                // TODO: integrar com servico de e-mail
                mostrarConfirmacaoEmail();
            }
        });
    }

    /**
     * Exibe alert de confirmacao apos solicitacao de recuperacao.
     * Customizado para manter a mesma identidade visual.
     */
    private void mostrarConfirmacaoEmail() {
        javafx.scene.control.Dialog<Void> confirmDialog = new javafx.scene.control.Dialog<>();
        confirmDialog.setTitle("Solicitação Enviada");
        confirmDialog.initOwner(usuarioField.getScene().getWindow());

        confirmDialog.getDialogPane().getStylesheets().add(
            getClass().getResource("/css/style.css").toExternalForm()
        );
        confirmDialog.getDialogPane().setStyle("-fx-background-color: white;");

        javafx.scene.layout.VBox card = new javafx.scene.layout.VBox(15);
        card.setAlignment(javafx.geometry.Pos.TOP_CENTER);
        card.setPadding(new javafx.geometry.Insets(20, 40, 20, 40));
        card.setPrefWidth(400);

        Label title = new Label("E-mail Enviado!");
        title.getStyleClass().add("title-label");

        Label text = new Label("Se o e-mail informado estiver correto,\nvocê receberá as instruções em breve.");
        text.getStyleClass().add("subtitle-label");
        text.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        javafx.scene.control.Button btnOk = new javafx.scene.control.Button("Entendido");
        btnOk.getStyleClass().add("login-button");
        btnOk.setMaxWidth(Double.MAX_VALUE);

        card.getChildren().addAll(title, text, btnOk);
        confirmDialog.getDialogPane().setContent(card);

        confirmDialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        javafx.scene.Node okNode = confirmDialog.getDialogPane().lookupButton(ButtonType.OK);
        okNode.setVisible(false);
        okNode.setManaged(false);

        btnOk.setOnAction(e -> confirmDialog.setResult(null));

        confirmDialog.showAndWait();
    }
}
