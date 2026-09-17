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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Controller da tela de listagem de Ocorrencias.
 * Gerencia a TableView, busca em tempo real, filtro por situacao
 * e abertura do formulario modal de cadastro/edicao.
 * Segue o mesmo padrao do UnidadeListController.
 */
public class OcorrenciaListController {

    // ------------------------------------------------------------------
    // Componentes injetados pelo FXMLLoader
    // ------------------------------------------------------------------
    @FXML private TableView<Ocorrencia>           tabelaOcorrencias;
    @FXML private TableColumn<Ocorrencia, String> colDescricao;
    @FXML private TableColumn<Ocorrencia, String> colLocal;
    @FXML private TableColumn<Ocorrencia, String> colData;
    @FXML private TableColumn<Ocorrencia, String> colTipo;
    @FXML private TableColumn<Ocorrencia, String> colSituacao;
    @FXML private TableColumn<Ocorrencia, Void>   colAcoes;
    @FXML private TextField                        txtBusca;
    @FXML private ComboBox<String>                 cmbSituacao;
    @FXML private Label                            lblContagem;

    // ------------------------------------------------------------------
    // Estado interno
    // ------------------------------------------------------------------
    private final OcorrenciaService service = new OcorrenciaService();
    private final ObservableList<Ocorrencia> todasOcorrencias = FXCollections.observableArrayList();
    private FilteredList<Ocorrencia> ocorrenciasFiltradas;

    private static final DateTimeFormatter FMT_DATA = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ------------------------------------------------------------------
    // Inicializacao
    // ------------------------------------------------------------------

    @FXML
    public void initialize() {
        configurarFiltroSituacao();
        configurarColunas();
        configurarBusca();
        carregarOcorrencias();
    }

    // ------------------------------------------------------------------
    // Configuracao do ComboBox de situacao
    // ------------------------------------------------------------------

    private void configurarFiltroSituacao() {
        cmbSituacao.setItems(FXCollections.observableArrayList(
            "Todos", "ABERTA", "EM_ANDAMENTO", "ENCERRADA"
        ));
        cmbSituacao.getSelectionModel().selectFirst(); // "Todos" por padrao
    }

    // ------------------------------------------------------------------
    // Configuracao das colunas da TableView
    // ------------------------------------------------------------------

    private void configurarColunas() {

        // Coluna DESCRICAO: exibe o titulo da ocorrencia
        colDescricao.setCellValueFactory(
            data -> new SimpleStringProperty(data.getValue().getTitulo()));

        // Coluna LOCAL/UNIDADE: exibe o campo transiente montado pelo DAO
        colLocal.setCellValueFactory(
            data -> new SimpleStringProperty(
                data.getValue().getLocalFormatado() != null
                    ? data.getValue().getLocalFormatado() : ""));
        colLocal.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String valor, boolean empty) {
                super.updateItem(valor, empty);
                if (empty || valor == null || valor.isBlank()) {
                    setText(null);
                    setStyle("-fx-text-fill: #9ca3af;");
                } else {
                    setText(valor);
                    setStyle("-fx-text-fill: #9ca3af;");
                }
            }
        });

        // Coluna DATA: formata LocalDateTime → "yyyy-MM-dd"
        colData.setCellValueFactory(data -> {
            Ocorrencia o = data.getValue();
            String dataStr = (o.getDataAbertura() != null)
                ? o.getDataAbertura().format(FMT_DATA)
                : "";
            return new SimpleStringProperty(dataStr);
        });

        // Coluna TIPO: badge colorido por categoria
        colTipo.setCellValueFactory(
            data -> new SimpleStringProperty(data.getValue().getCategoria()));
        colTipo.setCellFactory(col -> new TableCell<>() {
            private final Label badge = new Label();
            @Override
            protected void updateItem(String categoria, boolean empty) {
                super.updateItem(categoria, empty);
                if (empty || categoria == null) {
                    setGraphic(null);
                    return;
                }
                badge.setText(categoriaExibicao(categoria));
                badge.getStyleClass().removeIf(c -> c.startsWith("badge-tipo-"));
                badge.getStyleClass().add(badgeCssTipo(categoria));
                setGraphic(badge);
            }
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
                badge.getStyleClass().removeIf(c -> c.startsWith("badge-ocorrencia-"));
                badge.getStyleClass().add(badgeCssSituacao(situacao));
                setGraphic(badge);
            }
        });

        // Coluna ACOES: botoes editar + excluir
        colAcoes.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar  = new Button("✎");
            private final Button btnExcluir = new Button("🗑");
            private final HBox   caixa      = new HBox(4, btnEditar, btnExcluir);

            {
                btnEditar .getStyleClass().add("btn-acao-editar");
                btnExcluir.getStyleClass().add("btn-acao-excluir");

                btnEditar.setOnAction(e -> {
                    Ocorrencia o = getTableView().getItems().get(getIndex());
                    abrirFormulario(o);
                });
                btnExcluir.setOnAction(e -> {
                    Ocorrencia o = getTableView().getItems().get(getIndex());
                    onExcluir(o);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : caixa);
            }
        });
    }

    // ------------------------------------------------------------------
    // Busca em tempo real + filtro por situacao
    // ------------------------------------------------------------------

    private void configurarBusca() {
        ocorrenciasFiltradas = new FilteredList<>(todasOcorrencias, o -> true);

        // Atualiza o predicado sempre que o texto OU a situacao mudar
        txtBusca.textProperty().addListener((obs, antigo, novo) -> aplicarFiltro());
        cmbSituacao.valueProperty().addListener((obs, antigo, novo) -> aplicarFiltro());

        tabelaOcorrencias.setItems(ocorrenciasFiltradas);
    }

    private void aplicarFiltro() {
        String termo     = txtBusca.getText() == null ? "" : txtBusca.getText().toLowerCase().trim();
        String situacao  = cmbSituacao.getValue();

        ocorrenciasFiltradas.setPredicate(o -> {
            // Filtro por situacao
            if (situacao != null && !"Todos".equals(situacao)
                    && !situacao.equals(o.getSituacao())) {
                return false;
            }
            // Filtro por texto (titulo ou local)
            if (!termo.isEmpty()) {
                boolean matchTitulo = o.getTitulo() != null
                    && o.getTitulo().toLowerCase().contains(termo);
                boolean matchLocal  = o.getLocalFormatado() != null
                    && o.getLocalFormatado().toLowerCase().contains(termo);
                return matchTitulo || matchLocal;
            }
            return true;
        });
        atualizarContagem();
    }

    // ------------------------------------------------------------------
    // Carregamento de dados
    // ------------------------------------------------------------------

    private void carregarOcorrencias() {
        try {
            List<Ocorrencia> lista = service.listarTodas();
            todasOcorrencias.setAll(lista);
            atualizarContagem();
        } catch (Exception e) {
            mostrarErro("Erro ao carregar ocorrencias", e.getMessage());
        }
    }

    private void atualizarContagem() {
        int qtd = ocorrenciasFiltradas != null
            ? ocorrenciasFiltradas.size()
            : todasOcorrencias.size();
        lblContagem.setText(qtd + " registro(s)");
    }

    // ------------------------------------------------------------------
    // Acoes de botoes
    // ------------------------------------------------------------------

    /** Abre o formulario em modo cadastro (sem ocorrencia pre-carregada). */
    @FXML
    private void onNovaOcorrencia() {
        abrirFormulario(null);
    }

    /** Abre o formulario em modo edicao. */
    private void abrirFormulario(Ocorrencia ocorrencia) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/ocorrencia/OcorrenciaForm.fxml")
            );
            VBox conteudo = loader.load();
            OcorrenciaFormController formCtrl = loader.getController();

            if (ocorrencia != null) {
                formCtrl.setOcorrencia(ocorrencia);
            }

            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle(ocorrencia == null ? "Nova Ocorrência" : "Editar Ocorrência");
            dialog.getDialogPane().setContent(conteudo);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.getDialogPane().lookupButton(ButtonType.CLOSE).setVisible(false);
            dialog.getDialogPane().lookupButton(ButtonType.CLOSE).setManaged(false);

            formCtrl.setDialog(dialog);
            dialog.showAndWait();

            // Recarrega a lista apos fechar (cadastro ou edicao realizada)
            carregarOcorrencias();

        } catch (IOException e) {
            mostrarErro("Erro ao abrir formulario", e.getMessage());
        }
    }

    /** Confirma e executa a exclusao de uma ocorrencia. */
    private void onExcluir(Ocorrencia ocorrencia) {
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar exclusão");
        confirmacao.setHeaderText("Excluir ocorrência: " + ocorrencia.getTitulo() + "?");
        confirmacao.setContentText("Esta acao nao pode ser desfeita.");

        Optional<ButtonType> resposta = confirmacao.showAndWait();
        if (resposta.isPresent() && resposta.get() == ButtonType.OK) {
            try {
                service.remover(ocorrencia.getId());
                carregarOcorrencias();
            } catch (Exception e) {
                mostrarErro("Erro ao excluir ocorrencia", e.getMessage());
            }
        }
    }

    // ------------------------------------------------------------------
    // Helpers de exibicao e CSS
    // ------------------------------------------------------------------

    private String situacaoExibicao(String situacao) {
        return switch (situacao == null ? "" : situacao.toUpperCase()) {
            case "ABERTA"       -> "Aberta";
            case "EM_ANDAMENTO" -> "Em andamento";
            case "ENCERRADA"    -> "Encerrada";
            default             -> situacao;
        };
    }

    private String badgeCssSituacao(String situacao) {
        return switch (situacao == null ? "" : situacao.toUpperCase()) {
            case "ABERTA"       -> "badge-ocorrencia-aberta";
            case "EM_ANDAMENTO" -> "badge-ocorrencia-em-andamento";
            case "ENCERRADA"    -> "badge-ocorrencia-encerrada";
            default             -> "badge-desocupada";
        };
    }

    private String categoriaExibicao(String categoria) {
        return switch (categoria == null ? "" : categoria.toUpperCase()) {
            case "RECLAMACAO" -> "Reclamação";
            case "INFORMACAO" -> "Informação";
            case "MANUTENCAO" -> "Manutenção";
            case "SEGURANCA"  -> "Segurança";
            default           -> categoria;
        };
    }

    private String badgeCssTipo(String categoria) {
        return switch (categoria == null ? "" : categoria.toUpperCase()) {
            case "RECLAMACAO" -> "badge-tipo-reclamacao";
            case "INFORMACAO" -> "badge-tipo-informacao";
            case "MANUTENCAO" -> "badge-tipo-manutencao";
            case "SEGURANCA"  -> "badge-tipo-seguranca";
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



