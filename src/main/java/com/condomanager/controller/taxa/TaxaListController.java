package com.condomanager.controller.taxa;

import com.condomanager.dao.TaxaDAO;
import com.condomanager.model.Taxa;
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
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class TaxaListController {

    @FXML private TableView<Taxa> tabelaTaxas;
    @FXML private TableColumn<Taxa, String> colUnidade;
    @FXML private TableColumn<Taxa, String> colReferencia;
    @FXML private TableColumn<Taxa, String> colVencimento;
    @FXML private TableColumn<Taxa, String> colValor;
    @FXML private TableColumn<Taxa, String> colSituacao;
    @FXML private TableColumn<Taxa, Void> colAcoes;
    
    @FXML private TextField txtBusca;
    @FXML private ComboBox<String> cbFiltroSituacao;
    @FXML private Label lblTotal;
    @FXML private Label lblPagas;
    @FXML private Label lblPendentes;
    @FXML private Label lblAtrasadas;
    @FXML private Label lblStatusTabela;

    private final TaxaDAO dao = new TaxaDAO();
    private final ObservableList<Taxa> todasTaxas = FXCollections.observableArrayList();
    private FilteredList<Taxa> taxasFiltradas;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    @FXML
    public void initialize() {
        cbFiltroSituacao.setItems(FXCollections.observableArrayList("Todos", "PENDENTE", "PAGO", "ATRASADO"));
        cbFiltroSituacao.setValue("Todos");

        configurarColunas();
        configurarBusca();
        carregarTaxas();
    }

    private void configurarColunas() {
        colUnidade.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUnidadeTexto()));
        
        colReferencia.setCellValueFactory(data -> {
            LocalDate dt = data.getValue().getVencimento();
            if (dt != null) {
                String mes = dt.getMonth().getDisplayName(TextStyle.SHORT, new Locale("pt", "BR"));
                String ano = String.valueOf(dt.getYear());
                String ref = mes.substring(0, 1).toUpperCase() + mes.substring(1).toLowerCase() + "/" + ano;
                return new SimpleStringProperty(ref);
            }
            return new SimpleStringProperty("");
        });

        colVencimento.setCellValueFactory(data -> {
            LocalDate dt = data.getValue().getVencimento();
            return new SimpleStringProperty(dt != null ? dt.format(dateFormatter) : "");
        });

        colValor.setCellValueFactory(data -> {
            BigDecimal val = data.getValue().getValor();
            return new SimpleStringProperty(val != null ? currencyFormatter.format(val) : "");
        });

        colSituacao.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSituacao()));
        colSituacao.setCellFactory(col -> new TableCell<>() {
            private final Label badge = new Label();
            @Override
            protected void updateItem(String situacao, boolean empty) {
                super.updateItem(situacao, empty);
                if (empty || situacao == null) {
                    setGraphic(null);
                    return;
                }
                badge.setText(situacao);
                badge.getStyleClass().removeAll("badge-pago", "badge-pendente", "badge-atrasado");
                badge.getStyleClass().add(badgeCss(situacao));
                setGraphic(badge);
            }
        });

        colAcoes.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar  = new Button("✎");
            private final Button btnExcluir = new Button("🗑");
            private final HBox   caixa      = new HBox(4, btnEditar, btnExcluir);
            {
                btnEditar.getStyleClass().add("btn-acao-editar");
                btnExcluir.getStyleClass().add("btn-acao-excluir");

                btnEditar.setOnAction(e -> {
                    Taxa t = getTableView().getItems().get(getIndex());
                    abrirFormulario(t);
                });
                btnExcluir.setOnAction(e -> {
                    Taxa t = getTableView().getItems().get(getIndex());
                    onExcluir(t);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : caixa);
            }
        });
    }

    private void configurarBusca() {
        taxasFiltradas = new FilteredList<>(todasTaxas, t -> true);
        
        txtBusca.textProperty().addListener((obs, antigo, novo) -> atualizarFiltro());
        cbFiltroSituacao.valueProperty().addListener((obs, antigo, novo) -> atualizarFiltro());

        tabelaTaxas.setItems(taxasFiltradas);
    }

    private void atualizarFiltro() {
        String termoBusca = txtBusca.getText() == null ? "" : txtBusca.getText().toLowerCase().trim();
        String filtroSituacao = cbFiltroSituacao.getValue();

        taxasFiltradas.setPredicate(t -> {
            boolean bateBusca = true;
            if (!termoBusca.isEmpty()) {
                String unidade = t.getUnidadeTexto() != null ? t.getUnidadeTexto().toLowerCase() : "";
                String referencia = gerarReferencia(t.getVencimento()).toLowerCase();
                bateBusca = unidade.contains(termoBusca) || referencia.contains(termoBusca);
            }
            
            boolean bateSituacao = true;
            if (filtroSituacao != null && !filtroSituacao.equals("Todos")) {
                bateSituacao = filtroSituacao.equalsIgnoreCase(t.getSituacao());
            }

            return bateBusca && bateSituacao;
        });
        
        lblStatusTabela.setText(taxasFiltradas.size() + " registro(s)");
    }

    private void carregarTaxas() {
        try {
            List<Taxa> lista = dao.listarTodos();
            todasTaxas.setAll(lista);
            atualizarFiltro();
            atualizarContadores();
        } catch (Exception e) {
            mostrarErro("Erro ao carregar taxas", e.getMessage());
        }
    }

    private void atualizarContadores() {
        long pagas = todasTaxas.stream().filter(t -> "PAGO".equalsIgnoreCase(t.getSituacao())).count();
        long pendentes = todasTaxas.stream().filter(t -> "PENDENTE".equalsIgnoreCase(t.getSituacao())).count();
        long atrasadas = todasTaxas.stream().filter(t -> "ATRASADO".equalsIgnoreCase(t.getSituacao())).count();
        
        lblTotal.setText(String.valueOf(todasTaxas.size()));
        lblPagas.setText(String.valueOf(pagas));
        lblPendentes.setText(String.valueOf(pendentes));
        lblAtrasadas.setText(String.valueOf(atrasadas));
    }

    @FXML
    private void onNovaTaxa() {
        abrirFormulario(null);
    }

    private void abrirFormulario(Taxa taxa) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/taxa/TaxaForm.fxml"));
            VBox conteudo = loader.load();
            TaxaFormController formCtrl = loader.getController();

            if (taxa != null) {
                formCtrl.setTaxa(taxa);
            }

            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle(taxa == null ? "Nova Taxa" : "Editar Taxa");
            dialog.getDialogPane().setContent(conteudo);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.getDialogPane().lookupButton(ButtonType.CLOSE).setVisible(false);
            dialog.getDialogPane().lookupButton(ButtonType.CLOSE).setManaged(false);

            formCtrl.setDialog(dialog);
            dialog.showAndWait();

            carregarTaxas();
        } catch (IOException e) {
            mostrarErro("Erro ao abrir formulário", e.getMessage());
        }
    }

    private void onExcluir(Taxa taxa) {
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar exclusão");
        confirmacao.setHeaderText("Excluir taxa?");
        confirmacao.setContentText("Deseja realmente excluir a taxa da unidade " + taxa.getUnidadeTexto() + "?");

        Optional<ButtonType> resposta = confirmacao.showAndWait();
        if (resposta.isPresent() && resposta.get() == ButtonType.OK) {
            try {
                dao.deletar(taxa.getId());
                carregarTaxas();
            } catch (Exception e) {
                mostrarErro("Erro ao excluir taxa", e.getMessage());
            }
        }
    }

    private String badgeCss(String situacao) {
        if (situacao == null) return "";
        if (situacao.equalsIgnoreCase("PAGO")) return "badge-pago";
        if (situacao.equalsIgnoreCase("PENDENTE")) return "badge-pendente";
        if (situacao.equalsIgnoreCase("ATRASADO")) return "badge-atrasado";
        return "";
    }

    private String gerarReferencia(LocalDate dt) {
        if (dt == null) return "";
        String mes = dt.getMonth().getDisplayName(TextStyle.SHORT, new Locale("pt", "BR"));
        return mes.substring(0, 1).toUpperCase() + mes.substring(1).toLowerCase() + "/" + dt.getYear();
    }

    private void mostrarErro(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(titulo);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}
