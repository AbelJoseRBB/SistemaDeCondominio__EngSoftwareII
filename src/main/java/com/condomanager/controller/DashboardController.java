cpackage com.condomanager.controller;

import com.condomanager.model.DashboardResumo;
import com.condomanager.model.Ocorrencia;
import com.condomanager.model.Reserva;
import com.condomanager.model.Usuario;
import com.condomanager.service.DashboardService;
import com.condomanager.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Controller do painel inicial (Dashboard).
 * Apresenta o resumo geral das operacoes (indicadores e ultimas ocorrencias/reservas)
 * e permite navegacao rapida para os respectivos modulos.
 */
public class DashboardController {

    @FXML private Label lblSaudacao;
    @FXML private Label lblTotalUnidades;
    @FXML private Label lblTotalMoradores;
    @FXML private Label lblTaxasPendentes;
    @FXML private Label lblManutencoesAbertas;

    @FXML private VBox cardUnidades;
    @FXML private VBox cardMoradores;
    @FXML private VBox cardTaxas;
    @FXML private VBox cardManutencoes;

    @FXML private TableView<Reserva> tblReservasRecentes;
    @FXML private TableColumn<Reserva, String> colReservaArea;
    @FXML private TableColumn<Reserva, String> colReservaUnidade;
    @FXML private TableColumn<Reserva, String> colReservaData;
    @FXML private TableColumn<Reserva, String> colReservaHorario;
    @FXML private TableColumn<Reserva, String> colReservaSituacao;

    @FXML private TableView<Ocorrencia> tblOcorrenciasRecentes;
    @FXML private TableColumn<Ocorrencia, String> colOcorrDescricao;
    @FXML private TableColumn<Ocorrencia, String> colOcorrLocal;
    @FXML private TableColumn<Ocorrencia, String> colOcorrData;
    @FXML private TableColumn<Ocorrencia, String> colOcorrSituacao;

    @FXML private Button btnAtualizar;

    private final DashboardService dashboardService;
    private MainLayoutController mainLayoutController;

    private static final DateTimeFormatter FMT_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FMT_HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FMT_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Locale LOCALE_BR = Locale.forLanguageTag("pt-BR");

    public DashboardController() {
        this.dashboardService = new DashboardService();
    }

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    public void setMainLayoutController(MainLayoutController mainLayoutController) {
        this.mainLayoutController = mainLayoutController;
    }

    @FXML
    public void initialize() {
        configurarSaudacao();
        configurarColunasReservas();
        configurarColunasOcorrencias();
        carregarDados();
    }

    /**
     * Define o texto de boas-vindas com nome do usuario logado e data atual.
     */
    private void configurarSaudacao() {
        Usuario usuario = SessionManager.getUsuarioLogado();
        String nome = (usuario != null && usuario.getNome() != null) ? usuario.getNome() : "Administrador";

        LocalDate hoje = LocalDate.now();
        DateTimeFormatter formatoDataExtenso = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", LOCALE_BR);
        String dataExtenso = hoje.format(formatoDataExtenso);
        // Capitaliza a primeira letra do dia da semana
        dataExtenso = dataExtenso.substring(0, 1).toUpperCase(LOCALE_BR) + dataExtenso.substring(1);

        lblSaudacao.setText("Bem-vindo(a), " + nome + " • " + dataExtenso);
    }

    /**
     * Configura as colunas da tabela de reservas recentes.
     */
    private void configurarColunasReservas() {
        colReservaArea.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getAreaComum() != null ? d.getValue().getAreaComum() : "-"));

        colReservaUnidade.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getUnidadeFormatada() != null ? d.getValue().getUnidadeFormatada() : "-"));

        colReservaData.setCellValueFactory(d -> {
            Reserva r = d.getValue();
            return new SimpleStringProperty(r.getData() != null ? r.getData().format(FMT_DATA) : "-");
        });

        colReservaHorario.setCellValueFactory(d -> {
            Reserva r = d.getValue();
            String inicio = r.getHoraInicio() != null ? r.getHoraInicio().format(FMT_HORA) : "";
            String fim = r.getHoraFim() != null ? r.getHoraFim().format(FMT_HORA) : "";
            return new SimpleStringProperty(!inicio.isEmpty() && !fim.isEmpty() ? inicio + " - " + fim : "-");
        });

        colReservaSituacao.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getSituacao() != null ? d.getValue().getSituacao() : "-"));

        colReservaSituacao.setCellFactory(col -> new TableCell<>() {
            private final Label badge = new Label();
            @Override
            protected void updateItem(String situacao, boolean empty) {
                super.updateItem(situacao, empty);
                if (empty || situacao == null || situacao.isBlank() || "-".equals(situacao)) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                badge.setText(situacao);
                badge.getStyleClass().removeIf(c -> c.startsWith("badge-reserva-"));
                switch (situacao.toUpperCase(Locale.ROOT)) {
                    case "CONFIRMADA" -> badge.getStyleClass().add("badge-reserva-confirmada");
                    case "PENDENTE"   -> badge.getStyleClass().add("badge-reserva-pendente");
                    case "CANCELADA"  -> badge.getStyleClass().add("badge-reserva-cancelada");
                    default           -> badge.getStyleClass().add("badge-reserva-cancelada");
                }
                setGraphic(badge);
                setText(null);
            }
        });
    }

    /**
     * Configura as colunas da tabela de ocorrencias recentes.
     */
    private void configurarColunasOcorrencias() {
        colOcorrDescricao.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getTitulo() != null ? d.getValue().getTitulo() : "-"));

        colOcorrLocal.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getLocal() != null ? d.getValue().getLocal() : "Área Comum"));

        colOcorrData.setCellValueFactory(d -> {
            Ocorrencia o = d.getValue();
            return new SimpleStringProperty(o.getDataAbertura() != null ? o.getDataAbertura().format(FMT_DATA_HORA) : "-");
        });

        colOcorrSituacao.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getSituacao() != null ? d.getValue().getSituacao() : "-"));

        colOcorrSituacao.setCellFactory(col -> new TableCell<>() {
            private final Label badge = new Label();
            @Override
            protected void updateItem(String situacao, boolean empty) {
                super.updateItem(situacao, empty);
                if (empty || situacao == null || situacao.isBlank() || "-".equals(situacao)) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                badge.setText(situacao.replace("_", " "));
                badge.getStyleClass().removeIf(c -> c.startsWith("badge-ocorrencia-"));
                switch (situacao.toUpperCase(Locale.ROOT)) {
                    case "ABERTA"        -> badge.getStyleClass().add("badge-ocorrencia-aberta");
                    case "EM_ANDAMENTO"  -> badge.getStyleClass().add("badge-ocorrencia-em-andamento");
                    case "ENCERRADA"     -> badge.getStyleClass().add("badge-ocorrencia-encerrada");
                    default              -> badge.getStyleClass().add("badge-ocorrencia-encerrada");
                }
                setGraphic(badge);
                setText(null);
            }
        });
    }

    /**
     * Busca os dados consolidados do servico e preenche a interface.
     */
    public void carregarDados() {
        DashboardResumo resumo = dashboardService.carregarResumo();
        lblTotalUnidades.setText(String.valueOf(resumo.getTotalUnidades()));
        lblTotalMoradores.setText(String.valueOf(resumo.getTotalMoradores()));
        lblTaxasPendentes.setText(String.valueOf(resumo.getTaxasPendentes()));
        lblManutencoesAbertas.setText(String.valueOf(resumo.getManutencoesAbertas()));

        tblReservasRecentes.setItems(
            FXCollections.observableArrayList(dashboardService.listarReservasRecentes(6))
        );
        tblOcorrenciasRecentes.setItems(
            FXCollections.observableArrayList(dashboardService.listarOcorrenciasRecentes(6))
        );
    }

    @FXML
    public void onAtualizar() {
        carregarDados();
    }

    // ------------------------------------------------------------------
    // Navegacao Rapida atraves dos Cards e Links do Dashboard
    // ------------------------------------------------------------------

    @FXML
    public void navegarUnidades() {
        if (mainLayoutController != null) {
            mainLayoutController.onUnidades();
        }
    }

    @FXML
    public void navegarMoradores() {
        if (mainLayoutController != null) {
            mainLayoutController.onMoradores();
        }
    }

    @FXML
    public void navegarTaxas() {
        if (mainLayoutController != null) {
            mainLayoutController.onTaxas();
        }
    }

    @FXML
    public void navegarManutencoes() {
        if (mainLayoutController != null) {
            mainLayoutController.onManutencoes();
        }
    }

    @FXML
    public void navegarReservas() {
        if (mainLayoutController != null) {
            mainLayoutController.onReservas();
        }
    }

    @FXML
    public void navegarOcorrencias() {
        if (mainLayoutController != null) {
            mainLayoutController.onOcorrencias();
        }
    }
}