package com.condomanager.controller.manutencao;

import com.condomanager.model.Manutencao;
import com.condomanager.service.ManutencaoService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

/** Controller da lista/tabela de Manutencao */
public class ManutencaoListController {

    @FXML private TextField txtBusca;
    @FXML private ComboBox<String> cbFiltroStatus;
    @FXML private TableView<Manutencao> tableView;
    @FXML private TableColumn<Manutencao, String> colDescricao;
    @FXML private TableColumn<Manutencao, String> colLocal;
    @FXML private TableColumn<Manutencao, String> colResponsavel;
    @FXML private TableColumn<Manutencao, LocalDate> colDataSolicitacao;
    @FXML private TableColumn<Manutencao, BigDecimal> colCusto;
    @FXML private TableColumn<Manutencao, String> colSituacao;
    @FXML private TableColumn<Manutencao, Void> colAcoes;
    @FXML private Label lblTotalRegistros;

    private ManutencaoService manutencaoService = new ManutencaoService();
    private ObservableList<Manutencao> manutencaoList = FXCollections.observableArrayList();
    private FilteredList<Manutencao> filteredData;

    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    @FXML
    public void initialize() {
        cbFiltroStatus.setItems(FXCollections.observableArrayList("Todos", "Aberta", "Em andamento", "Concluída", "Cancelada"));
        cbFiltroStatus.setValue("Todos");

        configurarColunas();
        carregarDados();

        txtBusca.textProperty().addListener((observable, oldValue, newValue) -> aplicarFiltros());
        cbFiltroStatus.valueProperty().addListener((observable, oldValue, newValue) -> aplicarFiltros());
    }

    private void configurarColunas() {
        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        colLocal.setCellValueFactory(new PropertyValueFactory<>("local"));
        colResponsavel.setCellValueFactory(new PropertyValueFactory<>("responsavel"));

        colDataSolicitacao.setCellValueFactory(new PropertyValueFactory<>("dataSolicitacao"));
        colDataSolicitacao.setCellFactory(column -> new TableCell<Manutencao, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(dateFormatter.format(item));
                    Manutencao m = getTableView().getItems().get(getIndex());
                    if (item.isBefore(LocalDate.now()) && 
                        ("SOLICITADA".equals(m.getSituacao()) || "EM_ANDAMENTO".equals(m.getSituacao()))) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        colCusto.setCellValueFactory(new PropertyValueFactory<>("custo"));
        colCusto.setCellFactory(column -> new TableCell<Manutencao, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else if (item == null) {
                    setText("—");
                } else {
                    setText(currencyFormatter.format(item));
                }
            }
        });

        colSituacao.setCellValueFactory(cellData -> {
            String status = cellData.getValue().getSituacao();
            if ("SOLICITADA".equals(status)) return new SimpleStringProperty("Aberta");
            if ("EM_ANDAMENTO".equals(status)) return new SimpleStringProperty("Em andamento");
            if ("CONCLUIDA".equals(status)) return new SimpleStringProperty("Concluída");
            if ("CANCELADA".equals(status)) return new SimpleStringProperty("Cancelada");
            return new SimpleStringProperty(status);
        });
        
        colSituacao.setCellFactory(column -> new TableCell<Manutencao, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label lbl = new Label(item);
                    lbl.setStyle("-fx-padding: 4 8; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 11px;");
                    switch (item) {
                        case "Aberta":
                            lbl.setStyle(lbl.getStyle() + "-fx-background-color: #fffbeb; -fx-text-fill: #b45309; -fx-border-color: #fcd34d; -fx-border-radius: 12;");
                            break;
                        case "Em andamento":
                            lbl.setStyle(lbl.getStyle() + "-fx-background-color: #eff6ff; -fx-text-fill: #1d4ed8; -fx-border-color: #bfdbfe; -fx-border-radius: 12;");
                            break;
                        case "Concluída":
                            lbl.setStyle(lbl.getStyle() + "-fx-background-color: #f0fdf4; -fx-text-fill: #15803d; -fx-border-color: #bbf7d0; -fx-border-radius: 12;");
                            break;
                        case "Cancelada":
                            lbl.setStyle(lbl.getStyle() + "-fx-background-color: #fef2f2; -fx-text-fill: #b91c1c; -fx-border-color: #fecaca; -fx-border-radius: 12;");
                            break;
                    }
                    setGraphic(lbl);
                    setAlignment(javafx.geometry.Pos.CENTER);
                }
            }
        });

        colAcoes.setCellFactory(param -> new TableCell<Manutencao, Void>() {
            private final Button btnEditar = new Button("✎");
            private final Button btnExcluir = new Button("🗑");
            private final HBox pane = new HBox(5, btnEditar, btnExcluir);

            {
                pane.setAlignment(javafx.geometry.Pos.CENTER);
                
                btnEditar.getStyleClass().add("btn-secondary");
                btnEditar.setStyle("-fx-padding: 2 6;");
                btnEditar.setOnAction(e -> abrirFormulario(getTableView().getItems().get(getIndex())));

                btnExcluir.getStyleClass().add("btn-secondary");
                btnExcluir.setStyle("-fx-padding: 2 6; -fx-text-fill: red;");
                btnExcluir.setOnAction(e -> excluirManutencao(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    public void carregarDados() {
        manutencaoList.setAll(manutencaoService.listarTodas());
        filteredData = new FilteredList<>(manutencaoList, p -> true);
        tableView.setItems(filteredData);
        aplicarFiltros();
    }

    private void aplicarFiltros() {
        String busca = txtBusca.getText() == null ? "" : txtBusca.getText().toLowerCase();
        String status = cbFiltroStatus.getValue();

        filteredData.setPredicate(manutencao -> {
            boolean matchBusca = true;
            if (!busca.isEmpty()) {
                matchBusca = (manutencao.getDescricao() != null && manutencao.getDescricao().toLowerCase().contains(busca)) ||
                             (manutencao.getLocal() != null && manutencao.getLocal().toLowerCase().contains(busca)) ||
                             (manutencao.getResponsavel() != null && manutencao.getResponsavel().toLowerCase().contains(busca));
            }

            boolean matchStatus = true;
            if (status != null && !status.equals("Todos")) {
                String dbStatus = manutencao.getSituacao();
                if ("Aberta".equals(status)) matchStatus = "SOLICITADA".equals(dbStatus);
                else if ("Em andamento".equals(status)) matchStatus = "EM_ANDAMENTO".equals(dbStatus);
                else if ("Concluída".equals(status)) matchStatus = "CONCLUIDA".equals(dbStatus);
                else if ("Cancelada".equals(status)) matchStatus = "CANCELADA".equals(dbStatus);
            }

            return matchBusca && matchStatus;
        });

        lblTotalRegistros.setText(filteredData.size() + " registro(s)");
    }

    @FXML
    private void abrirFormularioNovo() {
        abrirFormulario(null);
    }

    private void abrirFormulario(Manutencao manutencao) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/manutencao/ManutencaoForm.fxml"));
            Parent root = loader.load();

            ManutencaoFormController controller = loader.getController();
            controller.setListController(this);
            if (manutencao != null) {
                controller.setManutencao(manutencao);
            }

            Stage stage = new Stage();
            stage.setTitle(manutencao == null ? "Nova Manutenção" : "Editar Manutenção");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Erro ao abrir formulário");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void excluirManutencao(Manutencao manutencao) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Excluir Manutenção");
        alert.setHeaderText("Você tem certeza?");
        alert.setContentText("Deseja realmente excluir a manutenção '" + manutencao.getDescricao() + "'?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                manutencaoService.deletar(manutencao.getId());
                carregarDados();
            } catch (Exception e) {
                Alert err = new Alert(Alert.AlertType.ERROR);
                err.setTitle("Erro");
                err.setHeaderText("Erro ao excluir");
                err.setContentText(e.getMessage());
                err.showAndWait();
            }
        }
    }
}

