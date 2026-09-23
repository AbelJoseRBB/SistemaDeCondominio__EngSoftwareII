package com.condomanager.controller.veiculo;

import com.condomanager.model.Veiculo;
import com.condomanager.service.VeiculoService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.util.Locale;

public class VeiculoListController {
    @FXML private TextField txtBusca;
    @FXML private TableView<Veiculo> tabela;
    @FXML private TableColumn<Veiculo,String> colPlaca, colModelo, colMarca, colCor, colUnidade, colVaga, colProprietario;
    @FXML private TableColumn<Veiculo,Void> colAcoes;
    @FXML private Label lblContagem, lblMensagem;
    private final VeiculoService service;
    private final ObservableList<Veiculo> dados=FXCollections.observableArrayList();
    private final FilteredList<Veiculo> filtrados=new FilteredList<>(dados);
    public VeiculoListController() { this(new VeiculoService()); }
    public VeiculoListController(VeiculoService service) { this.service=service; }
    @FXML public void initialize() {
        colPlaca.setCellValueFactory(new PropertyValueFactory<>("placa"));
        colModelo.setCellValueFactory(new PropertyValueFactory<>("modelo"));
        colMarca.setCellValueFactory(new PropertyValueFactory<>("marca"));
        colCor.setCellValueFactory(new PropertyValueFactory<>("cor"));
        colUnidade.setCellValueFactory(new PropertyValueFactory<>("unidade"));
        colVaga.setCellValueFactory(new PropertyValueFactory<>("numeroVaga"));
        colProprietario.setCellValueFactory(new PropertyValueFactory<>("proprietario"));
        colPlaca.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String value, boolean empty) {
                super.updateItem(value,empty); setText(null);
                Label badge=new Label(value); badge.getStyleClass().add("placa");
                setGraphic(empty || value==null ? null : badge);
            }
        });
        colAcoes.setCellFactory(c -> new TableCell<>() {
            final Button editar=new Button("✎"), excluir=new Button();
            final HBox acoes=new HBox(5,editar,excluir);
            {
                acoes.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                javafx.scene.shape.SVGPath lixeira=new javafx.scene.shape.SVGPath();
                lixeira.setContent("M 2 4 L 12 4 M 5 4 L 5 2 L 9 2 L 9 4 M 3 4 L 4 13 L 10 13 L 11 4 M 6 6 L 6 11 M 8 6 L 8 11");
                lixeira.setFill(Color.TRANSPARENT); lixeira.setStroke(Color.web("#ef3038"));
                excluir.setGraphic(lixeira);
                editar.getStyleClass().add("acao-editar"); excluir.getStyleClass().add("acao-excluir");
                editar.setAccessibleText("Editar veículo"); excluir.setAccessibleText("Excluir veículo");
                editar.setTooltip(new Tooltip("Editar veículo")); excluir.setTooltip(new Tooltip("Excluir veículo"));
                editar.setOnAction(e -> abrirFormulario(getTableRow().getItem()));
                excluir.setOnAction(e -> excluir(getTableRow().getItem()));
            }
            @Override protected void updateItem(Void value,boolean empty) { super.updateItem(value,empty); setGraphic(empty?null:acoes); }
        });
        SortedList<Veiculo> ordenados=new SortedList<>(filtrados);
        ordenados.comparatorProperty().bind(tabela.comparatorProperty()); tabela.setItems(ordenados);
        txtBusca.textProperty().addListener((o,a,b) -> filtrar());
        carregarDados();
    }
    public void carregarDados() {
        try { dados.setAll(service.listarTodos()); mensagem(""); filtrar(); }
        catch(RuntimeException e) { mensagem(e.getMessage()); }
    }
    private void filtrar() {
        String busca=txtBusca.getText().trim().toLowerCase(Locale.ROOT);
        filtrados.setPredicate(v -> contem(v.getPlaca(),busca) || contem(v.getModelo(),busca) || contem(v.getProprietario(),busca));
        lblContagem.setText(filtrados.size()+" registro(s)");
        tabela.setPrefHeight(Math.min(530,Math.max(90,40+filtrados.size()*49)));
        tabela.setPlaceholder(new Label(dados.isEmpty()?"Nenhum veículo cadastrado.":"Nenhum veículo encontrado."));
    }
    private boolean contem(String valor,String busca) { return valor!=null && valor.toLowerCase(Locale.ROOT).contains(busca); }
    @FXML private void onNovoVeiculo() { abrirFormulario(null); }
    @FXML private void onAtualizar() { carregarDados(); }
    private void abrirFormulario(Veiculo veiculo) {
        Parent owner=tabela.getScene().getRoot();
        var efeito=owner.getEffect();
        try {
            FXMLLoader loader=new FXMLLoader(getClass().getResource("/fxml/veiculo/VeiculoForm.fxml"));
            Parent root=loader.load();
            VeiculoFormController controller=loader.getController();
            controller.configurar(veiculo,() -> { carregarDados(); mensagem("Veículo salvo com sucesso!"); });
            Stage stage=new Stage(StageStyle.TRANSPARENT);
            stage.initOwner(tabela.getScene().getWindow()); stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle(veiculo==null?"Novo Veículo":"Editar Veículo");
            Scene scene=new Scene(root); scene.setFill(Color.TRANSPARENT); stage.setScene(scene); stage.setResizable(false);
            owner.setEffect(new ColorAdjust(0,-0.2,-0.35,0));
            stage.showAndWait();
        } catch(Exception e) { mensagem("Não foi possível abrir o formulário. "+e.getMessage()); }
        finally { owner.setEffect(efeito); }
    }
    private void excluir(Veiculo v) {
        if(v==null) return;
        Alert alert=new Alert(Alert.AlertType.CONFIRMATION,"Excluir o veículo "+v.getPlaca()+"? A vaga será liberada.",ButtonType.CANCEL,ButtonType.OK);
        alert.initOwner(tabela.getScene().getWindow()); alert.setHeaderText("Excluir veículo");
        if(alert.showAndWait().orElse(ButtonType.CANCEL)!=ButtonType.OK) return;
        try { service.deletar(v.getId()); carregarDados(); mensagem("Veículo excluído com sucesso!"); }
        catch(RuntimeException e) { mensagem(e.getMessage()); }
    }
    private void mensagem(String texto) { lblMensagem.setText(texto); lblMensagem.setManaged(!texto.isEmpty()); lblMensagem.setVisible(!texto.isEmpty()); }
}
