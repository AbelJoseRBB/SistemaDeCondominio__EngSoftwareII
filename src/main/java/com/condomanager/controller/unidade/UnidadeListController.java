package com.condomanager.controller.unidade;

import com.condomanager.model.Unidade;
import com.condomanager.service.UnidadeService;
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
import java.util.List;
import java.util.Optional;

/**
 * Controller da tela de listagem de Unidades.
 * Gerencia a TableView, busca em tempo real, e abertura do formulario modal.
 */
public class UnidadeListController {

    // ------------------------------------------------------------------
    // Componentes injetados pelo FXMLLoader
    // ------------------------------------------------------------------
    @FXML private TableView<Unidade>          tabelaUnidades;
    @FXML private TableColumn<Unidade, String> colBloco;
    @FXML private TableColumn<Unidade, String> colNumero;
    @FXML private TableColumn<Unidade, String> colProprietario;
    @FXML private TableColumn<Unidade, String> colSituacao;
    @FXML private TableColumn<Unidade, String> colTelefone;
    @FXML private TableColumn<Unidade, String> colEmail;
    @FXML private TableColumn<Unidade, Void>   colAcoes;
    @FXML private TextField                    txtBusca;
    @FXML private Label                        lblContagem;

    // ------------------------------------------------------------------
    // Estado interno
    // ------------------------------------------------------------------
    private final UnidadeService service = new UnidadeService();
    private final ObservableList<Unidade> todasUnidades = FXCollections.observableArrayList();
    private FilteredList<Unidade> unidadesFiltradas;

    // ------------------------------------------------------------------
    // Inicializacao
    // ------------------------------------------------------------------

    @FXML
    public void initialize() {
        configurarColunas();
        configurarBusca();
        carregarUnidades();
    }

    // ------------------------------------------------------------------
    // Configuracao das colunas da TableView
    // ------------------------------------------------------------------

    private void configurarColunas() {
        colBloco.setCellValueFactory(
            data -> new SimpleStringProperty(data.getValue().getBloco()));

        colNumero.setCellValueFactory(
            data -> new SimpleStringProperty(data.getValue().getNumero()));

        colProprietario.setCellValueFactory(
            data -> new SimpleStringProperty(data.getValue().getProprietario()));

        colTelefone.setCellValueFactory(
            data -> new SimpleStringProperty(data.getValue().getTelefoneContato()));

        colEmail.setCellValueFactory(
            data -> new SimpleStringProperty(data.getValue().getEmailContato()));

        // Coluna OCUPACAO: badge colorido conforme situacao
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
                badge.getStyleClass().removeAll("badge-ocupada", "badge-desocupada", "badge-alugada");
                badge.getStyleClass().add(badgeCss(situacao));
                setGraphic(badge);
            }
        });

        // Coluna ACOES: botoes ver / editar / excluir
        colAcoes.setCellFactory(col -> new TableCell<>() {
            private final Button btnVer     = new Button("👁");
            private final Button btnEditar  = new Button("✎");
            private final Button btnExcluir = new Button("🗑");
            private final HBox   caixa      = new HBox(4, btnVer, btnEditar, btnExcluir);

            {
                btnVer    .getStyleClass().add("btn-acao-ver");
                btnEditar .getStyleClass().add("btn-acao-editar");
                btnExcluir.getStyleClass().add("btn-acao-excluir");

                btnVer.setOnAction(e -> {
                    Unidade u = getTableView().getItems().get(getIndex());
                    abrirFormulario(u, true);
                });
                btnEditar.setOnAction(e -> {
                    Unidade u = getTableView().getItems().get(getIndex());
                    abrirFormulario(u, false);
                });
                btnExcluir.setOnAction(e -> {
                    Unidade u = getTableView().getItems().get(getIndex());
                    onExcluir(u);
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
    // Busca em tempo real
    // ------------------------------------------------------------------

    private void configurarBusca() {
        unidadesFiltradas = new FilteredList<>(todasUnidades, u -> true);

        txtBusca.textProperty().addListener((obs, antigo, novo) -> {
            String termo = novo == null ? "" : novo.toLowerCase().trim();
            unidadesFiltradas.setPredicate(u -> {
                if (termo.isEmpty()) return true;
                return (u.getBloco()        != null && u.getBloco().toLowerCase().contains(termo))
                    || (u.getNumero()       != null && u.getNumero().toLowerCase().contains(termo))
                    || (u.getProprietario() != null && u.getProprietario().toLowerCase().contains(termo));
            });
            atualizarContagem();
        });

        tabelaUnidades.setItems(unidadesFiltradas);
    }

    // ------------------------------------------------------------------
    // Carregamento de dados
    // ------------------------------------------------------------------

    private void carregarUnidades() {
        try {
            List<Unidade> lista = service.listarTodas();
            todasUnidades.setAll(lista);
            atualizarContagem();
        } catch (Exception e) {
            mostrarErro("Erro ao carregar unidades", e.getMessage());
        }
    }

    private void atualizarContagem() {
        int qtd = unidadesFiltradas != null ? unidadesFiltradas.size() : todasUnidades.size();
        lblContagem.setText(qtd + " registro(s) encontrado(s)");
    }

    // ------------------------------------------------------------------
    // Acoes de botoes
    // ------------------------------------------------------------------

    /** Abre o formulario em modo cadastro (sem unidade pre-carregada). */
    @FXML
    private void onNovaUnidade() {
        abrirFormulario(null, false);
    }

    /** Abre o formulario em modo edicao ou visualizacao. */
    private void abrirFormulario(Unidade unidade, boolean somenteLeitura) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/unidade/UnidadeForm.fxml")
            );
            VBox conteudo = loader.load();
            UnidadeFormController formCtrl = loader.getController();

            // Passa a unidade para o controller (null = novo cadastro)
            if (unidade != null) {
                formCtrl.setUnidade(unidade);
            }
            formCtrl.setSomenteLeitura(somenteLeitura);

            // Cria o Dialog e embute o conteudo do FXML
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle(somenteLeitura ? "Visualizar Unidade"
                          : unidade == null ? "Nova Unidade"
                          : "Editar Unidade");
            dialog.getDialogPane().setContent(conteudo);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            // Esconde o botao CLOSE padrao do DialogPane (os botoes estao no FXML)
            dialog.getDialogPane().lookupButton(ButtonType.CLOSE).setVisible(false);
            dialog.getDialogPane().lookupButton(ButtonType.CLOSE).setManaged(false);

            // Passa referencia do dialog para o controller fechar programaticamente
            formCtrl.setDialog(dialog);

            dialog.showAndWait();

            // Recarrega a lista apos fechar o dialog (capturas salvas ou editadas)
            carregarUnidades();

        } catch (IOException e) {
            mostrarErro("Erro ao abrir formulario", e.getMessage());
        }
    }

    /** Confirma e executa a exclusao de uma unidade. */
    private void onExcluir(Unidade unidade) {
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar exclusão");
        confirmacao.setHeaderText("Excluir unidade Bloco " + unidade.getBloco()
                                  + " - " + unidade.getNumero() + "?");
        confirmacao.setContentText("Esta acao nao pode ser desfeita.");

        Optional<ButtonType> resposta = confirmacao.showAndWait();
        if (resposta.isPresent() && resposta.get() == ButtonType.OK) {
            try {
                service.remover(unidade.getId());
                carregarUnidades();
            } catch (Exception e) {
                mostrarErro("Erro ao excluir unidade", e.getMessage());
            }
        }
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /** Converte o valor do banco (OCUPADO/DESOCUPADO/ALUGUEL) para exibicao em portugues. */
    private String situacaoExibicao(String situacao) {
        return switch (situacao == null ? "" : situacao.toUpperCase()) {
            case "OCUPADO"    -> "Ocupada";
            case "DESOCUPADO" -> "Desocupada";
            case "ALUGUEL"    -> "Alugada";
            case "VENDA"      -> "À Venda";
            default           -> situacao;
        };
    }

    /** Retorna a classe CSS do badge de acordo com a situacao. */
    private String badgeCss(String situacao) {
        return switch (situacao == null ? "" : situacao.toUpperCase()) {
            case "OCUPADO"    -> "badge-ocupada";
            case "ALUGUEL"    -> "badge-alugada";
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
