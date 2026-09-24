package com.condomanager.controller.morador;

import com.condomanager.dao.MoradorDAO;
import com.condomanager.dao.UnidadeDAO;
import com.condomanager.model.Morador;
import com.condomanager.model.Unidade;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class MoradorListController {

    @FXML private TextField txtBusca;
    @FXML private TableView<Morador> tabelaMoradores;
    @FXML private TableColumn<Morador, String> colNome;
    @FXML private TableColumn<Morador, String> colUnidade;
    @FXML private TableColumn<Morador, String> colTelefone;
    @FXML private TableColumn<Morador, String> colEmail;
    @FXML private TableColumn<Morador, String> colTipo;
    @FXML private TableColumn<Morador, String> colSituacao;
    @FXML private TableColumn<Morador, Void> colAcoes;

    private MoradorDAO moradorDAO = new MoradorDAO();
    private UnidadeDAO unidadeDAO = new UnidadeDAO();
    private ObservableList<Morador> moradorList = FXCollections.observableArrayList();
    private FilteredList<Morador> filteredData;

    @FXML
    public void initialize() {
        configurarColunas();
        carregarDados();

        txtBusca.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(morador -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                if (morador.getNome().toLowerCase().contains(lowerCaseFilter)) return true;
                if (morador.getCpf().toLowerCase().contains(lowerCaseFilter)) return true;
                
                Unidade u = unidadeDAO.buscarPorId(morador.getIdUnidade());
                if (u != null && (u.getBloco() + "-" + u.getNumero()).toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });
    }

    private void configurarColunas() {
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colTelefone.setCellValueFactory(new PropertyValueFactory<>("telefone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        
        colUnidade.setCellValueFactory(cellData -> {
            Unidade u = unidadeDAO.buscarPorId(cellData.getValue().getIdUnidade());
            return new SimpleStringProperty(u != null ? u.getBloco() + "-" + u.getNumero() : "N/A");
        });

        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colTipo.setCellFactory(column -> new TableCell<Morador, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label lbl = new Label(item);
                    switch (item.toUpperCase()) {
                        case "PROPRIETARIO": lbl.getStyleClass().add("badge-proprietario"); break;
                        case "DEPENDENTE": lbl.getStyleClass().add("badge-dependente"); break;
                        case "LOCATARIO": lbl.getStyleClass().add("badge-locatario"); break;
                        default: lbl.getStyleClass().add("badge-dependente");
                    }
                    setGraphic(lbl);
                }
            }
        });

        colSituacao.setCellValueFactory(new PropertyValueFactory<>("situacao"));
        colSituacao.setCellFactory(column -> new TableCell<Morador, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label lbl = new Label(item);
                    if ("ATIVO".equalsIgnoreCase(item)) lbl.getStyleClass().add("badge-ativo");
                    else lbl.getStyleClass().add("badge-inativo");
                    setGraphic(lbl);
                }
            }
        });

        colAcoes.setCellFactory(param -> new TableCell<Morador, Void>() {
            private final Button btnEditar = new Button("✎");
            private final Button btnInativar = new Button("🗑");
            private final HBox pane = new HBox(5, btnEditar, btnInativar);

            {
                btnEditar.getStyleClass().add("btn-secondary");
                btnEditar.setStyle("-fx-padding: 2 6;");
                btnEditar.setOnAction(e -> abrirFormulario(getTableView().getItems().get(getIndex())));

                btnInativar.getStyleClass().add("btn-secondary");
                btnInativar.setStyle("-fx-padding: 2 6; -fx-text-fill: red;");
                btnInativar.setOnAction(e -> inativarMorador(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    public void carregarDados() {
        moradorList.setAll(moradorDAO.listarTodos());
        filteredData = new FilteredList<>(moradorList, p -> true);
        tabelaMoradores.setItems(filteredData);
    }

    @FXML
    private void onNovoMorador() {
        abrirFormulario(null);
    }

    private void abrirFormulario(Morador morador) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/morador/MoradorForm.fxml"));
            Parent root = loader.load();

            MoradorFormController controller = loader.getController();
            controller.setListController(this);
            if (morador != null) {
                controller.setMorador(morador);
            }

            Stage stage = new Stage();
            stage.setTitle(morador == null ? "Novo Morador" : "Editar Morador");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void inativarMorador(Morador morador) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Inativar Morador");
        alert.setHeaderText("Você tem certeza?");
        alert.setContentText("Deseja inativar o morador " + morador.getNome() + "? O histórico será mantido.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            moradorDAO.inativar(morador.getId());
            carregarDados();
        }
    }
}

