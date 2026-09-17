package com.condomanager.controller.reserva;

import com.condomanager.model.Reserva;
import com.condomanager.service.ReservaService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Controller da tela de listagem de Reservas.
 * Gerencia a TableView, busca em tempo real, filtro por situacao
 * e abertura do formulario modal de cadastro/edicao.
 * Segue o mesmo padrao do OcorrenciaListController.
 */
public class ReservaListController {

    // ------------------------------------------------------------------
    // Componentes injetados pelo FXMLLoader
    // ------------------------------------------------------------------
    @FXML private TableView<Reserva>           tabelaReservas;
    @FXML private TableColumn<Reserva, String> colUnidade;
    @FXML private TableColumn<Reserva, String> colArea;
    @FXML private TableColumn<Reserva, String> colData;
    @FXML private TableColumn<Reserva, String> colHoraInicio;
    @FXML private TableColumn<Reserva, String> colHoraFim;
    @FXML private TableColumn<Reserva, String> colSituacao;
    @FXML private TableColumn<Reserva, Void>   colAcoes;
    @FXML private TextField                    txtBusca;
    @FXML private ComboBox<String>             cmbSituacao;
    @FXML private Label                        lblContagem;

    // ------------------------------------------------------------------
    // Estado interno
    // ------------------------------------------------------------------
    private final ReservaService service = new ReservaService();
    private final ObservableList<Reserva> todasReservas = FXCollections.observableArrayList();
    private FilteredList<Reserva> reservasFiltradas;

    private static final DateTimeFormatter FMT_DATA = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter FMT_HORA = DateTimeFormatter.ofPattern("HH:mm");

    // ------------------------------------------------------------------
    // Inicializacao
    // ------------------------------------------------------------------

    @FXML
    public void initialize() {
        configurarFiltroSituacao();
        configurarColunas();
        configurarBusca();
        carregarReservas();
    }

    // ------------------------------------------------------------------
    // Configuracao do ComboBox de situacao
    // ------------------------------------------------------------------

    private void configurarFiltroSituacao() {
        cmbSituacao.setItems(FXCollections.observableArrayList(
            "Todas", "CONFIRMADA", "PENDENTE", "CANCELADA"
        ));
        cmbSituacao.getSelectionModel().selectFirst(); // "Todas" por padrao
    }

    // ------------------------------------------------------------------
    // Configuracao das colunas da TableView
    // ------------------------------------------------------------------

    private void configurarColunas() {

        // Coluna UNIDADE: campo transiente montado pelo DAO via JOIN
        colUnidade.setCellValueFactory(
            data -> new SimpleStringProperty(
                data.getValue().getUnidadeFormatada() != null
                    ? data.getValue().getUnidadeFormatada() : ""));

        // Coluna AREA COMUM
        colArea.setCellValueFactory(
            data -> new SimpleStringProperty(data.getValue().getAreaComum()));

        // Coluna DATA: formata LocalDate → "yyyy-MM-dd"
        colData.setCellValueFactory(data -> {
            Reserva r = data.getValue();
            String dataStr = (r.getData() != null)
                ? r.getData().format(FMT_DATA)
                : "";
            return new SimpleStringProperty(dataStr);
        });

        // Coluna HORA INICIO: formata LocalTime → "HH:mm"
        colHoraInicio.setCellValueFactory(data -> {
            Reserva r = data.getValue();
            String horaStr = (r.getHoraInicio() != null)
                ? r.getHoraInicio().format(FMT_HORA)
                : "";
            return new SimpleStringProperty(horaStr);
        });

        // Coluna HORA FIM: formata LocalTime → "HH:mm"
        colHoraFim.setCellValueFactory(data -> {
            Reserva r = data.getValue();
            String horaStr = (r.getHoraFim() != null)
                ? r.getHoraFim().format(FMT_HORA)
                : "";
            return new SimpleStringProperty(horaStr);
        });

        // Coluna SITUACAO: badge colorido por situacao
        colSituacao.setCellValueFactory(
            data -> new SimpleStringProperty(data.getValue().getSituacao()));
        colSituacao.setCellFactory(col -> new TableCell<>() {
            private final Label badge = new Label();
            @Override
            protected void updateItem(String situacao, boolean empty) {
                super.updateItem(situacao, empty);
                if (empty || situacao == null) {
                    setGraphic(null);
                    return;
                }
                badge.setText(situacaoExibicao(situacao));
                badge.getStyleClass().removeIf(c -> c.startsWith("badge-reserva-"));
                badge.getStyleClass().add(badgeCssSituacao(situacao));
                setGraphic(badge);
            }
        });

        // Coluna ACOES: botoes editar + cancelar
        colAcoes.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar   = new Button("✎");
            private final Button btnCancelar = new Button("✕");
            private final HBox   caixa       = new HBox(4, btnEditar, btnCancelar);

            {
                btnEditar  .getStyleClass().add("btn-acao-editar");
                btnCancelar.getStyleClass().add("btn-acao-excluir");

                btnEditar.setOnAction(e -> {
                    Reserva r = getTableView().getItems().get(getIndex());
                    abrirFormulario(r);
                });
                btnCancelar.setOnAction(e -> {
                    Reserva r = getTableView().getItems().get(getIndex());
                    onCancelar(r);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                // Desabilita os botoes se a reserva ja estiver cancelada
                Reserva r = getTableView().getItems().get(getIndex());
                boolean cancelada = "CANCELADA".equals(r.getSituacao());
                btnEditar.setDisable(cancelada);
                btnCancelar.setDisable(cancelada);
                setGraphic(caixa);
            }
        });
    }

    // ------------------------------------------------------------------
    // Busca em tempo real + filtro por situacao
    // ------------------------------------------------------------------

    private void configurarBusca() {
        reservasFiltradas = new FilteredList<>(todasReservas, r -> true);

        txtBusca.textProperty().addListener((obs, antigo, novo) -> aplicarFiltro());
        cmbSituacao.valueProperty().addListener((obs, antigo, novo) -> aplicarFiltro());

        tabelaReservas.setItems(reservasFiltradas);
    }

    private void aplicarFiltro() {
        String termo    = txtBusca.getText() == null ? "" : txtBusca.getText().toLowerCase().trim();
        String situacao = cmbSituacao.getValue();

        reservasFiltradas.setPredicate(r -> {
            // Filtro por situacao
            if (situacao != null && !"Todas".equals(situacao)
                    && !situacao.equals(r.getSituacao())) {
                return false;
            }
            // Filtro por texto (area comum ou unidade)
            if (!termo.isEmpty()) {
                boolean matchArea    = r.getAreaComum() != null
                    && r.getAreaComum().toLowerCase().contains(termo);
                boolean matchUnidade = r.getUnidadeFormatada() != null
                    && r.getUnidadeFormatada().toLowerCase().contains(termo);
                return matchArea || matchUnidade;
            }
            return true;
        });
        atualizarContagem();
    }

    // ------------------------------------------------------------------
    // Carregamento de dados
    // ------------------------------------------------------------------

    private void carregarReservas() {
        try {
            List<Reserva> lista = service.listarTodas();
            todasReservas.setAll(lista);
            atualizarContagem();
        } catch (Exception e) {
            mostrarErro("Erro ao carregar reservas", e.getMessage());
        }
    }

    private void atualizarContagem() {
        int qtd = reservasFiltradas != null
            ? reservasFiltradas.size()
            : todasReservas.size();
        lblContagem.setText(qtd + " registro(s)");
    }

    // ------------------------------------------------------------------
    // Acoes de botoes
    // ------------------------------------------------------------------

    /** Abre o formulario em modo cadastro (sem reserva pre-carregada). */
    @FXML
    private void onNovaReserva() {
        abrirFormulario(null);
    }

    /** Abre o formulario em modo edicao. */
    private void abrirFormulario(Reserva reserva) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/reserva/ReservaForm.fxml")
            );
            VBox conteudo = loader.load();
            ReservaFormController formCtrl = loader.getController();

            if (reserva != null) {
                formCtrl.setReserva(reserva);
            }

            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle(reserva == null ? "Nova Reserva" : "Editar Reserva");
            dialog.getDialogPane().setContent(conteudo);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.getDialogPane().lookupButton(ButtonType.CLOSE).setVisible(false);
            dialog.getDialogPane().lookupButton(ButtonType.CLOSE).setManaged(false);

            formCtrl.setDialog(dialog);
            dialog.showAndWait();

            // Recarrega a lista apos fechar
            carregarReservas();

        } catch (IOException e) {
            mostrarErro("Erro ao abrir formulario", e.getMessage());
        }
    }

    /** Confirma e cancela uma reserva (muda situacao para CANCELADA). */
    private void onCancelar(Reserva reserva) {
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar cancelamento");
        confirmacao.setHeaderText("Cancelar reserva de \"" + reserva.getAreaComum() + "\"?");
        confirmacao.setContentText(
            "A reserva sera marcada como CANCELADA. Esta acao nao pode ser desfeita.");

        Optional<ButtonType> resposta = confirmacao.showAndWait();
        if (resposta.isPresent() && resposta.get() == ButtonType.OK) {
            try {
                service.cancelar(reserva.getId());
                carregarReservas();
            } catch (Exception e) {
                mostrarErro("Erro ao cancelar reserva", e.getMessage());
            }
        }
    }

    // ------------------------------------------------------------------
    // Helpers de exibicao e CSS
    // ------------------------------------------------------------------

    private String situacaoExibicao(String situacao) {
        return switch (situacao == null ? "" : situacao.toUpperCase()) {
            case "CONFIRMADA" -> "Confirmada";
            case "PENDENTE"   -> "Pendente";
            case "CANCELADA"  -> "Cancelada";
            default           -> situacao;
        };
    }

    private String badgeCssSituacao(String situacao) {
        return switch (situacao == null ? "" : situacao.toUpperCase()) {
            case "CONFIRMADA" -> "badge-reserva-confirmada";
            case "PENDENTE"   -> "badge-reserva-pendente";
            case "CANCELADA"  -> "badge-reserva-cancelada";
            default           -> "badge-desocupada";
        };
    }

    private void mostrarErro(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(titulo);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}



