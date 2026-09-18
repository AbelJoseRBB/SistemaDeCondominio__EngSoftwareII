package com.condomanager.controller;

import com.condomanager.model.Usuario;
import com.condomanager.util.InactivityWatcher;
import com.condomanager.util.NavigationUtil;
import com.condomanager.util.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.util.List;

/**
 * Controller do layout principal (Painel de Navegacao Central).
 * Gerencia a sidebar, verificacao de autenticacao de sessao, monitoramento
 * de inatividade e carregamento dinamico dos modulos na area de conteudo central.
 */
public class MainLayoutController {

    @FXML private StackPane contentArea;
    @FXML private Label lblUsuario;
    @FXML private Label lblPerfil;

    @FXML private Button btnDashboard;
    @FXML private Button btnUnidades;
    @FXML private Button btnReservas;
    @FXML private Button btnOcorrencias;
    @FXML private Button btnMoradores;
    @FXML private Button btnTaxas;
    @FXML private Button btnManutencoes;
    @FXML private Button btnVeiculos;
    @FXML private Button btnRelatorios;
    @FXML private Button btnSair;

    private final InactivityWatcher inactivityWatcher = new InactivityWatcher();

    @FXML
    public void initialize() {
        // 1. Verificacao estrita de sessao: impede acesso de usuario nao autenticado
        if (!SessionManager.isLogado()) {
            System.err.println("[Seguranca] Acesso negado a tela principal: nenhum usuario autenticado.");
            Platform.runLater(this::redirecionarParaLogin);
            return;
        }

        // 2. Verificacao de sessao expirada
        if (SessionManager.isSessionExpirada(InactivityWatcher.TIMEOUT_PADRAO_MS)) {
            System.err.println("[Seguranca] Sessao expirada por tempo de inatividade.");
            SessionManager.encerrarSessao();
            Platform.runLater(this::redirecionarParaLogin);
            return;
        }

        // 3. Exibe dados do usuario autenticado na barra lateral
        Usuario usuario = SessionManager.getUsuarioLogado();
        if (usuario != null) {
            lblUsuario.setText(usuario.getNome() != null ? usuario.getNome() : "Usuário");
            lblPerfil.setText(usuario.getPerfil() != null ? usuario.getPerfil() : "OPERADOR");
        }

        // 4. Carrega o Dashboard como tela inicial e destaca o botao
        onDashboard();

        // 5. Inicia o monitoramento de inatividade apos a cena estar associada
        Platform.runLater(this::iniciarMonitoramentoInatividade);
    }

    /**
     * Registra os listeners de mouse e teclado na cena para rastrear atividade
     * e inicia o InactivityWatcher com redirecionamento automatico para Login.
     */
    private void iniciarMonitoramentoInatividade() {
        if (contentArea != null) {
            Scene scene = contentArea.getScene();
            if (scene != null) {
                scene.setOnMouseMoved(e   -> SessionManager.registrarAtividade());
                scene.setOnMouseClicked(e -> SessionManager.registrarAtividade());
                scene.setOnKeyPressed(e   -> SessionManager.registrarAtividade());
            }
        }

        inactivityWatcher.iniciar(() -> {
            System.out.println("[Seguranca] Sessao encerrada por inatividade.");
            SessionManager.encerrarSessao();
            Platform.runLater(this::redirecionarParaLogin);
        });
    }

    /**
     * Carrega um FXML na area de conteudo central e atualiza o botao ativo.
     * Realiza verificacao de sessao antes de qualquer transicao.
     */
    public void carregarTela(String fxmlPath, Button botaoAtivo) {
        if (!SessionManager.isLogado()) {
            System.err.println("[Seguranca] Sessao invalida ao tentar navegar para " + fxmlPath);
            redirecionarParaLogin();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node tela = loader.load();

            // Integra o DashboardController com o MainLayoutController para permitir navegacao via cards
            Object controller = loader.getController();
            if (controller instanceof DashboardController dashboardController) {
                dashboardController.setMainLayoutController(this);
            }

            contentArea.getChildren().setAll(tela);
            destacarBotaoAtivo(botaoAtivo);
        } catch (Exception e) {
            System.err.println("Erro ao carregar tela [" + fxmlPath + "]: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Destaca visualmente o botao do modulo atualmente exibido.
     */
    private void destacarBotaoAtivo(Button botaoAtivo) {
        List<Button> botoes = List.of(
            btnDashboard, btnUnidades, btnReservas, btnOcorrencias,
            btnMoradores, btnTaxas, btnManutencoes, btnVeiculos, btnRelatorios
        );

        for (Button btn : botoes) {
            if (btn != null) {
                btn.getStyleClass().remove("sidebar-nav-btn-active");
            }
        }

        if (botaoAtivo != null && !botaoAtivo.getStyleClass().contains("sidebar-nav-btn-active")) {
            botaoAtivo.getStyleClass().add("sidebar-nav-btn-active");
        }
    }

    /**
     * Redireciona o usuario para a tela de autenticacao (Login).
     */
    private void redirecionarParaLogin() {
        inactivityWatcher.parar();
        NavigationUtil.navegar(
            "/fxml/Login.fxml",
            "CondoManager",
            1024, 768,
            true
        );
    }

    // ------------------------------------------------------------------
    // Acoes do Menu de Navegacao Lateral
    // ------------------------------------------------------------------

    @FXML public void onDashboard()   { carregarTela("/fxml/Dashboard.fxml", btnDashboard); }
    @FXML public void onUnidades()    { carregarTela("/fxml/unidade/UnidadeList.fxml", btnUnidades); }
    @FXML public void onReservas()    { carregarTela("/fxml/reserva/ReservaList.fxml", btnReservas); }
    @FXML public void onOcorrencias() { carregarTela("/fxml/ocorrencia/OcorrenciaList.fxml", btnOcorrencias); }
    @FXML public void onMoradores()   { carregarTela("/fxml/morador/MoradorList.fxml", btnMoradores); }
    @FXML public void onTaxas()       { carregarTela("/fxml/taxa/TaxaList.fxml", btnTaxas); }
    @FXML public void onManutencoes() { carregarTela("/fxml/manutencao/ManutencaoList.fxml", btnManutencoes); }
    @FXML public void onVeiculos()    { carregarTela("/fxml/veiculo/VeiculoList.fxml", btnVeiculos); }
    @FXML public void onRelatorios()  { carregarTela("/fxml/relatorio/RelatorioList.fxml", btnRelatorios); }

    /**
     * Encerra a sessao ativa do usuario e retorna a tela de Login.
     */
    @FXML
    public void onSair() {
        inactivityWatcher.parar();
        SessionManager.encerrarSessao();
        redirecionarParaLogin();
    }
}