package com.condomanager.controller;

import com.condomanager.util.NavigationUtil;
import com.condomanager.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
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

    @FXML
    public void initialize() {
        // Exibe o nome do usuario logado na sidebar
        if (SessionManager.getUsuarioLogado() != null) {
            lblUsuario.setText(SessionManager.getUsuarioLogado().getNome());
            lblPerfil.setText(SessionManager.getUsuarioLogado().getPerfil());
        }
        // Carrega o Dashboard como tela inicial
        onDashboard();
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
        SessionManager.encerrarSessao();
        NavigationUtil.navegar(
            "/fxml/Login.fxml",
            "CondoManager",
            480, 360,
            false
        );
    }
}

