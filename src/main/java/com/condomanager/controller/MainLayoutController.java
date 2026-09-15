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

/**
 * Controller do layout principal.
 * Gerencia a sidebar e carrega cada modulo na area de conteudo.
 */
public class MainLayoutController {

    @FXML private StackPane contentArea;
    @FXML private Label lblUsuario;
    @FXML private Label lblPerfil;

    private final InactivityWatcher inactivityWatcher = new InactivityWatcher();

    @FXML
    public void initialize() {
        // Exibe o nome do usuario logado na sidebar
        if (SessionManager.getUsuarioLogado() != null) {
            lblUsuario.setText(SessionManager.getUsuarioLogado().getNome());
            lblPerfil.setText(SessionManager.getUsuarioLogado().getPerfil());
        }
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

    @FXML private void onDashboard()   { carregarTela("/fxml/Dashboard.fxml"); }
    @FXML private void onUnidades()    { carregarTela("/fxml/unidade/UnidadeList.fxml"); }
    @FXML private void onMoradores()   { carregarTela("/fxml/morador/MoradorList.fxml"); }
    @FXML private void onTaxas()       { carregarTela("/fxml/taxa/TaxaList.fxml"); }
    @FXML private void onReservas()    { carregarTela("/fxml/reserva/ReservaList.fxml"); }
    @FXML private void onOcorrencias() { carregarTela("/fxml/ocorrencia/OcorrenciaList.fxml"); }
    @FXML private void onManutencoes() { carregarTela("/fxml/manutencao/ManutencaoList.fxml"); }
    @FXML private void onVeiculos()    { carregarTela("/fxml/veiculo/VeiculoList.fxml"); }
    @FXML private void onRelatorios()  { carregarTela("/fxml/relatorio/RelatorioList.fxml"); }

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

