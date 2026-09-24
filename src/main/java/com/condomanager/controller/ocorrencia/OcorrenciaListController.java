package com.condomanager.controller.ocorrencia;

import com.condomanager.model.Ocorrencia;
import com.condomanager.service.OcorrenciaService;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Controller da tela de listagem de Ocorrencias.
 * Gerencia a TableView, busca em tempo real, filtro por situacao
 * e abertura do formulario modal de cadastro/edicao.
 */
public class OcorrenciaListController {

    // ------------------------------------------------------------------
    // Componentes injetados pelo FXMLLoader
    // ------------------------------------------------------------------

    @FXML
    private TableView<Ocorrencia> tabelaOcorrencias;
    @FXML
    private TableColumn<Ocorrencia, String> colDescricao;
    @FXML
    private TableColumn<Ocorrencia, String> colLocal;
    @FXML
    private TableColumn<Ocorrencia, String> colData;
    @FXML
    private TableColumn<Ocorrencia, String> colTipo;
    @FXML
    private TableColumn<Ocorrencia, String> colSituacao;
    @FXML
    private TableColumn<Ocorrencia, Void> colAcoes;
    @FXML
    private TextField txtBusca;
    @FXML
    private ComboBox<String> cmbSituacao;
    @FXML
    private Label lblContagem;
    @FXML
    private Label lblDataAtual;

    // ------------------------------------------------------------------
    // Estado interno
    // ------------------------------------------------------------------

    private final OcorrenciaService service =
            new OcorrenciaService();

    private final ObservableList<Ocorrencia> todasOcorrencias =
            FXCollections.observableArrayList();

    private FilteredList<Ocorrencia> ocorrenciasFiltradas;

    private static final DateTimeFormatter FMT_DATA =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ------------------------------------------------------------------
    // Inicializacao
    // ------------------------------------------------------------------

    @FXML
    public void initialize() {

        if (lblDataAtual != null) {

            lblDataAtual.setText(
                    LocalDate.now().format(
                            DateTimeFormatter.ofPattern(
                                    "EEEE, d 'de' MMMM 'de' yyyy",
                                    new java.util.Locale("pt", "BR")
                            )
                    )
            );
        }

        configurarFiltroSituacao();
        configurarColunas();
        configurarBusca();
        carregarOcorrencias();
    }

    // ------------------------------------------------------------------
    // Filtro por situacao
    // ------------------------------------------------------------------

    private void configurarFiltroSituacao() {

        cmbSituacao.setItems(
                FXCollections.observableArrayList(
                        "Todos",
                        "Aberta",
                        "Em andamento",
                        "Em análise",
                        "Resolvida"
                )
        );

        cmbSituacao
                .getSelectionModel()
                .selectFirst();
    }

    // ------------------------------------------------------------------
    // Colunas
    // ------------------------------------------------------------------

    private void configurarColunas() {

        // Apesar do nome colDescricao, esta coluna exibe o titulo.
        // O fx:id pode permanecer assim para evitar quebrar o FXML.
        colDescricao.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().getTitulo()
                )
        );

        // Local digitado no formulario e persistido no banco.
        colLocal.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getLocal() != null
                                ? data.getValue().getLocal()
                                : ""
                )
        );

        colLocal.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String valor, boolean empty) {
                super.updateItem(valor, empty);

                if (empty || valor == null || valor.isBlank()) {
                    setText(null);
                } else {
                    setText(valor);
                }

                setStyle("-fx-text-fill: #374151;");
            }
        });

        // DATA
        colData.setCellValueFactory(data -> {
            Ocorrencia ocorrencia = data.getValue();

            String dataFormatada = ocorrencia.getDataAbertura() != null
                    ? ocorrencia.getDataAbertura().format(FMT_DATA)
                    : "";

            return new SimpleStringProperty(dataFormatada);
        });

        colData.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String valor, boolean empty) {
                super.updateItem(valor, empty);

                setText(empty || valor == null ? null : valor);
                setStyle("-fx-text-fill: #374151;");
            }
        });

        // TIPO
        colTipo.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().getCategoria()
                )
        );

        colTipo.setCellFactory(
                col -> new TableCell<>() {

                    private final Label badge =
                            new Label();

                    @Override
                    protected void updateItem(
                            String categoria,
                            boolean empty
                    ) {

                        super.updateItem(
                                categoria,
                                empty
                        );

                        if (
                                empty ||
                                        categoria == null
                        ) {

                            setGraphic(null);
                            return;
                        }

                        badge.setText(
                                categoriaExibicao(categoria)
                        );

                        badge.getStyleClass()
                                .removeIf(
                                        c -> c.startsWith(
                                                "badge-tipo-"
                                        )
                                );

                        badge.getStyleClass()
                                .add(
                                        badgeCssTipo(categoria)
                                );

                        setGraphic(badge);
                    }
                }
        );

        // SITUACAO
        colSituacao.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().getSituacao()
                )
        );

        colSituacao.setCellFactory(
                col -> new TableCell<>() {

                    private final Label badge =
                            new Label();

                    @Override
                    protected void updateItem(
                            String situacao,
                            boolean empty
                    ) {

                        super.updateItem(
                                situacao,
                                empty
                        );

                        if (
                                empty ||
                                        situacao == null
                        ) {

                            setGraphic(null);
                            return;
                        }

                        badge.setText(
                                situacaoExibicao(situacao)
                        );

                        badge.getStyleClass()
                                .removeIf(
                                        c -> c.startsWith(
                                                "badge-ocorrencia-"
                                        )
                                );

                        badge.getStyleClass()
                                .add(
                                        badgeCssSituacao(situacao)
                                );

                        setGraphic(badge);
                    }
                }
        );

        // ACOES
        colAcoes.setCellFactory(
                col -> new TableCell<>() {

                    private final Button btnEditar =
                            new Button("✎");

                    private final Button btnExcluir =
                            new Button("🗑");

                    private final HBox caixa =
                            new HBox(
                                    8,
                                    btnEditar,
                                    btnExcluir
                            );

                    {
                        btnEditar.setStyle(
                                "-fx-background-color: #dcfce7; " +
                                        "-fx-text-fill: #15803d; " +
                                        "-fx-background-radius: 4; " +
                                        "-fx-padding: 4 8; " +
                                        "-fx-cursor: hand; " +
                                        "-fx-border-color: transparent;"
                        );

                        btnExcluir.setStyle(
                                "-fx-background-color: #fee2e2; " +
                                        "-fx-text-fill: #b91c1c; " +
                                        "-fx-background-radius: 4; " +
                                        "-fx-padding: 4 8; " +
                                        "-fx-cursor: hand; " +
                                        "-fx-border-color: transparent;"
                        );

                        btnEditar.setOnAction(
                                e -> {

                                    Ocorrencia o =
                                            getTableView()
                                                    .getItems()
                                                    .get(getIndex());

                                    abrirFormulario(o);
                                }
                        );

                        btnExcluir.setOnAction(
                                e -> {

                                    Ocorrencia o =
                                            getTableView()
                                                    .getItems()
                                                    .get(getIndex());

                                    onExcluir(o);
                                }
                        );
                    }

                    @Override
                    protected void updateItem(
                            Void item,
                            boolean empty
                    ) {

                        super.updateItem(
                                item,
                                empty
                        );

                        setGraphic(
                                empty
                                        ? null
                                        : caixa
                        );
                    }
                }
        );
    }

    // ------------------------------------------------------------------
    // Busca
    // ------------------------------------------------------------------

    private void configurarBusca() {

        ocorrenciasFiltradas =
                new FilteredList<>(
                        todasOcorrencias,
                        o -> true
                );

        txtBusca
                .textProperty()
                .addListener(
                        (obs, antigo, novo) ->
                                aplicarFiltro()
                );

        cmbSituacao
                .valueProperty()
                .addListener(
                        (obs, antigo, novo) ->
                                aplicarFiltro()
                );

        tabelaOcorrencias.setItems(
                ocorrenciasFiltradas
        );
    }

    private void aplicarFiltro() {

        String termo =
                txtBusca.getText() == null
                        ? ""
                        : txtBusca
                        .getText()
                        .toLowerCase()
                        .trim();

        String situacao =
                cmbSituacao.getValue();

        ocorrenciasFiltradas.setPredicate(
                o -> {

                    if (
                            situacao != null &&
                                    !"Todos".equals(situacao) &&
                                    !situacao.equals(
                                            o.getSituacao()
                                    )
                    ) {

                        return false;
                    }

                    if (!termo.isEmpty()) {

                        boolean matchTitulo =
                                o.getTitulo() != null &&
                                        o.getTitulo()
                                                .toLowerCase()
                                                .contains(termo);

                        boolean matchLocal =
                                o.getLocal() != null &&
                                        o.getLocal()
                                                .toLowerCase()
                                                .contains(termo);

                        return (
                                matchTitulo ||
                                        matchLocal
                        );
                    }

                    return true;
                }
        );

        atualizarContagem();
    }

    // ------------------------------------------------------------------
    // Carregamento
    // ------------------------------------------------------------------

    private void carregarOcorrencias() {

        try {

            List<Ocorrencia> lista =
                    service.listarTodas();

            todasOcorrencias.setAll(
                    lista
            );

            atualizarContagem();

        } catch (Exception e) {

            mostrarErro(
                    "Erro ao carregar ocorrencias",
                    e.getMessage()
            );
        }
    }

    private void atualizarContagem() {

        int qtd =
                ocorrenciasFiltradas != null
                        ? ocorrenciasFiltradas.size()
                        : todasOcorrencias.size();

        lblContagem.setText(
                qtd + " registro(s)"
        );
    }

    // ------------------------------------------------------------------
    // Acoes
    // ------------------------------------------------------------------

    @FXML
    private void onNovaOcorrencia() {
        abrirFormulario(null);
    }

    private void abrirFormulario(
            Ocorrencia ocorrencia
    ) {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/ocorrencia/OcorrenciaForm.fxml"
                            )
                    );

            VBox conteudo =
                    loader.load();

            OcorrenciaFormController formCtrl =
                    loader.getController();

            if (ocorrencia != null) {
                formCtrl.setOcorrencia(
                        ocorrencia
                );
            }

            Dialog<Void> dialog =
                    new Dialog<>();

            dialog.setTitle(
                    ocorrencia == null
                            ? "Nova Ocorrência"
                            : "Editar Ocorrência"
            );

            dialog
                    .getDialogPane()
                    .setContent(conteudo);

            dialog
                    .getDialogPane()
                    .getButtonTypes()
                    .add(ButtonType.CLOSE);

            dialog
                    .getDialogPane()
                    .lookupButton(ButtonType.CLOSE)
                    .setVisible(false);

            dialog
                    .getDialogPane()
                    .lookupButton(ButtonType.CLOSE)
                    .setManaged(false);

            formCtrl.setDialog(dialog);

            dialog.showAndWait();

            carregarOcorrencias();

        } catch (IOException e) {

            mostrarErro(
                    "Erro ao abrir formulario",
                    e.getMessage()
            );
        }
    }

    private void onExcluir(
            Ocorrencia ocorrencia
    ) {

        Alert confirmacao =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        confirmacao
                .getDialogPane()
                .getStylesheets()
                .add(
                        getClass()
                                .getResource(
                                        "/css/style.css"
                                )
                                .toExternalForm()
                );

        confirmacao.setTitle(
                "Confirmar exclusão"
        );

        confirmacao.setHeaderText(
                "Excluir ocorrência: "
                        + ocorrencia.getTitulo()
                        + "?"
        );

        confirmacao.setContentText(
                "Esta acao nao pode ser desfeita."
        );

        Optional<ButtonType> resposta =
                confirmacao.showAndWait();

        if (
                resposta.isPresent() &&
                        resposta.get() == ButtonType.OK
        ) {

            try {

                service.remover(
                        ocorrencia.getId()
                );

                carregarOcorrencias();

            } catch (Exception e) {

                mostrarErro(
                        "Erro ao excluir ocorrencia",
                        e.getMessage()
                );
            }
        }
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private String situacaoExibicao(
            String situacao
    ) {

        if (situacao == null) {
            return "";
        }

        return switch (
                situacao.toUpperCase()
                ) {

            case "ABERTA" -> "Aberta";

            case "EM_ANDAMENTO",
                 "EM ANDAMENTO" -> "Em andamento";

            case "EM_ANALISE",
                 "EM ANÁLISE" -> "Em análise";

            case "ENCERRADA",
                 "RESOLVIDA" -> "Resolvida";

            default -> situacao;
        };
    }

    private String badgeCssSituacao(
            String situacao
    ) {

        if (situacao == null) {
            return "badge-ocorrencia-aberta";
        }

        return switch (
                situacao.toUpperCase()
                ) {

            case "ABERTA" -> "badge-ocorrencia-aberta";

            case "EM_ANDAMENTO",
                 "EM ANDAMENTO" -> "badge-ocorrencia-em-andamento";

            case "EM_ANALISE",
                 "EM ANÁLISE" -> "badge-ocorrencia-analise";

            case "ENCERRADA",
                 "RESOLVIDA" -> "badge-ocorrencia-encerrada";

            default -> "badge-ocorrencia-aberta";
        };
    }

    private String categoriaExibicao(
            String categoria
    ) {

        if (categoria == null) {
            return "";
        }

        return switch (
                categoria.toUpperCase()
                ) {

            case "INFRAESTRUTURA" -> "Infraestrutura";

            case "CONVIVÊNCIA",
                 "CONVIVENCIA" -> "Convivência";

            case "ELÉTRICA",
                 "ELETRICA" -> "Elétrica";

            case "EQUIPAMENTO" -> "Equipamento";

            case "SEGURANÇA",
                 "SEGURANCA" -> "Segurança";

            case "HIDRÁULICA",
                 "HIDRAULICA" -> "Hidráulica";

            default -> categoria;
        };
    }

    private String badgeCssTipo(
            String categoria
    ) {

        if (categoria == null) {
            return "badge-tipo-infra";
        }

        return switch (
                categoria.toUpperCase()
                ) {

            case "INFRAESTRUTURA" -> "badge-tipo-infra";

            case "CONVIVÊNCIA",
                 "CONVIVENCIA" -> "badge-tipo-convivencia";

            case "ELÉTRICA",
                 "ELETRICA" -> "badge-tipo-eletrica";

            case "EQUIPAMENTO" -> "badge-tipo-equipamento";

            case "SEGURANÇA",
                 "SEGURANCA" -> "badge-tipo-seguranca";

            case "HIDRÁULICA",
                 "HIDRAULICA" -> "badge-tipo-hidraulica";

            default -> "badge-tipo-infra";
        };
    }

    private void mostrarErro(
            String titulo,
            String mensagem
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.ERROR
                );

        alert
                .getDialogPane()
                .getStylesheets()
                .add(
                        getClass()
                                .getResource(
                                        "/css/style.css"
                                )
                                .toExternalForm()
                );

        alert.setTitle(titulo);
        alert.setHeaderText(titulo);
        alert.setContentText(mensagem);

        alert.showAndWait();
    }
}