package com.condomanager.controller;

import com.condomanager.util.InactivityWatcher;
import com.condomanager.util.NavigationUtil;
import com.condomanager.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import java.util.Arrays;
import java.util.List;

/**
 * Controller do layout principal.
 * Gerencia a sidebar e carrega cada modulo na area de conteudo.
 */
public class MainLayoutController {

    @FXML private StackPane contentArea;
    @FXML private Label lblUsuario;
    @FXML private Label lblPerfil;
    @FXML private Label lblIniciais;

    @FXML private Button btnDashboard;
    @FXML private Button btnUnidades;
    @FXML private Button btnMoradores;
    @FXML private Button btnTaxas;
    @FXML private Button btnReservas;
    @FXML private Button btnOcorrencias;
    @FXML private Button btnManutencoes;
    @FXML private Button btnVeiculos;
    @FXML private Button btnRelatorios;

    private List<Button> menuButtons;

    private final InactivityWatcher inactivityWatcher = new InactivityWatcher();

    @FXML
    public void initialize() {
        // Exibe o nome do usuario logado na sidebar
        if (SessionManager.getUsuarioLogado() != null) {
            String nome = SessionManager.getUsuarioLogado().getNome();
            lblUsuario.setText(nome);
            lblPerfil.setText(SessionManager.getUsuarioLogado().getPerfil());
            
            // Define as iniciais (ate 2 caracteres)
            if (nome != null && !nome.isEmpty()) {
                String[] partes = nome.trim().split("\\s+");
                String iniciais = partes[0].substring(0, 1).toUpperCase();
                if (partes.length > 1) {
                    iniciais += partes[partes.length - 1].substring(0, 1).toUpperCase();
                }
                if (lblIniciais != null) {
                    lblIniciais.setText(iniciais);
                }
            }
        }
        
        // Inicializa a lista de botoes do menu para facilitar iteracao
        menuButtons = Arrays.asList(btnDashboard, btnUnidades, btnMoradores, btnTaxas, 
                                    btnReservas, btnOcorrencias, btnManutencoes, btnVeiculos, btnRelatorios);

        // Carrega o Dashboard como tela inicial
        onDashboard();

        // Inicia o monitoramento de inatividade apos a cena estar disponivel
        // Usamos Platform.runLater para garantir que a Scene ja foi atribuida ao Stage
        javafx.application.Platform.runLater(this::iniciarMonitoramentoInatividade);
    }

    /**
     * Registra os listeners de mouse e teclado na cena para rastrear atividade
     * e inicia o InactivityWatcher com redirecionamento para Login ao expirar.
     */
    private void iniciarMonitoramentoInatividade() {
        Scene scene = contentArea.getScene();
        if (scene != null) {
            scene.setOnMouseMoved(e   -> SessionManager.registrarAtividade());
            scene.setOnMouseClicked(e -> SessionManager.registrarAtividade());
            scene.setOnKeyPressed(e   -> SessionManager.registrarAtividade());
        }

        inactivityWatcher.iniciar(() -> NavigationUtil.navegar(
            "/fxml/Login.fxml",
            "CondoManager",
            480, 360,
            false
        ));
    }

    /** Carrega um FXML na area de conteudo central */
    private void carregarTela(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource(fxmlPath)
            );
            Node tela = loader.load();
            contentArea.getChildren().setAll(tela);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setActiveButton(Button activeBtn) {
        if (menuButtons == null) return;
        for (Button btn : menuButtons) {
            if (btn != null) {
                btn.getStyleClass().remove("sidebar-btn-active");
                if (btn == activeBtn) {
                    if (!btn.getStyleClass().contains("sidebar-btn-active")) {
                        btn.getStyleClass().add("sidebar-btn-active");
                    }
                }
            }
        }
    }

    @FXML private void onDashboard()   { setActiveButton(btnDashboard); carregarTela("/fxml/Dashboard.fxml"); }
    @FXML private void onUnidades()    { setActiveButton(btnUnidades); carregarTela("/fxml/unidade/UnidadeList.fxml"); }
    @FXML private void onMoradores()   { setActiveButton(btnMoradores); carregarTela("/fxml/morador/MoradorList.fxml"); }
    @FXML private void onTaxas()       { setActiveButton(btnTaxas); carregarTela("/fxml/taxa/TaxaList.fxml"); }
    @FXML private void onReservas()    { setActiveButton(btnReservas); carregarTela("/fxml/reserva/ReservaList.fxml"); }
    @FXML private void onOcorrencias() { setActiveButton(btnOcorrencias); carregarTela("/fxml/ocorrencia/OcorrenciaList.fxml"); }
    @FXML private void onManutencoes() { setActiveButton(btnManutencoes); carregarTela("/fxml/manutencao/ManutencaoList.fxml"); }
    @FXML private void onVeiculos()    { setActiveButton(btnVeiculos); carregarTela("/fxml/veiculo/VeiculoList.fxml"); }
    @FXML private void onRelatorios()  { setActiveButton(btnRelatorios); carregarTela("/fxml/relatorio/RelatorioList.fxml"); }

    @FXML
    private void onSair() {
        inactivityWatcher.parar();
        SessionManager.encerrarSessao();
        NavigationUtil.navegar(
            "/fxml/Login.fxml",
            "CondoManager",
            480, 360,
            false
        );
    }
}

