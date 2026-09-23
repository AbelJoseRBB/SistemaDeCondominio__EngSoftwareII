package com.condomanager.controller.manutencao;

import com.condomanager.model.Manutencao;
import com.condomanager.service.ManutencaoService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Controller do formulario de Manutencao */
public class ManutencaoFormController {

    @FXML private Label lblTitulo;
    @FXML private TextField txtDescricao;
    @FXML private TextField txtLocal;
    @FXML private TextField txtResponsavel;
    @FXML private DatePicker dpDataSolicitacao;
    @FXML private ComboBox<String> cbSituacao;
    @FXML private TextField txtCusto;
    @FXML private TextArea txtObservacoes;

    private ManutencaoService manutencaoService = new ManutencaoService();
    private ManutencaoListController listController;
    private Manutencao manutencaoAtual;

    @FXML
    public void initialize() {
        cbSituacao.setItems(FXCollections.observableArrayList("Aberta", "Em andamento", "Concluída", "Cancelada"));
        cbSituacao.setValue("Aberta");
        
        // Formatar campo de custo para aceitar apenas números e vírgula/ponto
        txtCusto.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*([\\.,]\\d*)?")) {
                txtCusto.setText(oldValue);
            }
        });
    }

    public void setListController(ManutencaoListController listController) {
        this.listController = listController;
    }

    public void setManutencao(Manutencao manutencao) {
        this.manutencaoAtual = manutencao;
        lblTitulo.setText("Editar Manutenção");

        txtDescricao.setText(manutencao.getDescricao());
        txtLocal.setText(manutencao.getLocal());
        txtResponsavel.setText(manutencao.getResponsavel());
        dpDataSolicitacao.setValue(manutencao.getDataSolicitacao());
        
        if (manutencao.getCusto() != null) {
            txtCusto.setText(manutencao.getCusto().toString().replace(".", ","));
        }
        
        txtObservacoes.setText(manutencao.getObservacoes());

        if (manutencao.getSituacao() != null) {
            switch (manutencao.getSituacao()) {
                case "SOLICITADA": cbSituacao.setValue("Aberta"); break;
                case "EM_ANDAMENTO": cbSituacao.setValue("Em andamento"); break;
                case "CONCLUIDA": cbSituacao.setValue("Concluída"); break;
                case "CANCELADA": cbSituacao.setValue("Cancelada"); break;
                default: cbSituacao.setValue(manutencao.getSituacao());
            }
        }
    }

    @FXML
    private void salvar(ActionEvent event) {
        try {
            if (txtDescricao.getText() == null || txtDescricao.getText().trim().isEmpty()) {
                mostrarAlertaErro("A descrição é obrigatória.");
                return;
            }
            if (dpDataSolicitacao.getValue() == null) {
                mostrarAlertaErro("A data prevista é obrigatória.");
                return;
            }

            boolean isNovo = (manutencaoAtual == null);
            if (isNovo) {
                manutencaoAtual = new Manutencao();
            }

            manutencaoAtual.setDescricao(txtDescricao.getText().trim());
            manutencaoAtual.setLocal(txtLocal.getText() != null ? txtLocal.getText().trim() : null);
            manutencaoAtual.setResponsavel(txtResponsavel.getText() != null ? txtResponsavel.getText().trim() : null);
            manutencaoAtual.setDataSolicitacao(dpDataSolicitacao.getValue());
            
            String situacaoSelecionada = cbSituacao.getValue();
            switch (situacaoSelecionada) {
                case "Aberta": manutencaoAtual.setSituacao("SOLICITADA"); break;
                case "Em andamento": manutencaoAtual.setSituacao("EM_ANDAMENTO"); break;
                case "Concluída": 
                    manutencaoAtual.setSituacao("CONCLUIDA"); 
                    if (manutencaoAtual.getDataConclusao() == null) {
                        manutencaoAtual.setDataConclusao(LocalDate.now());
                    }
                    break;
                case "Cancelada": manutencaoAtual.setSituacao("CANCELADA"); break;
            }
            
            if (txtCusto.getText() != null && !txtCusto.getText().trim().isEmpty()) {
                String custoStr = txtCusto.getText().trim().replace(",", ".");
                manutencaoAtual.setCusto(new BigDecimal(custoStr));
            } else {
                manutencaoAtual.setCusto(null);
            }
            
            manutencaoAtual.setObservacoes(txtObservacoes.getText() != null ? txtObservacoes.getText().trim() : null);

            if (isNovo) {
                manutencaoService.salvar(manutencaoAtual);
                mostrarAlertaSucesso("Manutenção cadastrada com sucesso!");
            } else {
                manutencaoService.atualizar(manutencaoAtual);
                mostrarAlertaSucesso("Manutenção atualizada com sucesso!");
            }

            if (listController != null) {
                listController.carregarDados();
            }
            fecharModal();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlertaErro("Erro ao salvar: " + e.getMessage());
        }
    }

    @FXML
    private void cancelar(ActionEvent event) {
        fecharModal();
    }

    private void fecharModal() {
        Stage stage = (Stage) txtDescricao.getScene().getWindow();
        stage.close();
    }
    
    private void mostrarAlertaErro(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro de Validação");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
    
    private void mostrarAlertaSucesso(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sucesso");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}

