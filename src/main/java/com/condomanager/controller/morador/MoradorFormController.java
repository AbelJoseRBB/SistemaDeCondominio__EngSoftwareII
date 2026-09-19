package com.condomanager.controller.morador;

import com.condomanager.dao.MoradorDAO;
import com.condomanager.dao.UnidadeDAO;
import com.condomanager.model.Morador;
import com.condomanager.model.Unidade;
import com.condomanager.util.ValidadorCPF;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.List;

public class MoradorFormController {

    @FXML private Label lblTituloForm;
    @FXML private TextField txtNome;
    @FXML private TextField txtCpf;
    @FXML private TextField txtTelefone;
    @FXML private TextField txtEmail;
    @FXML private ComboBox<Unidade> cbUnidade;
    @FXML private ComboBox<String> cbTipo;
    @FXML private ComboBox<String> cbSituacao;

    private MoradorDAO moradorDAO = new MoradorDAO();
    private UnidadeDAO unidadeDAO = new UnidadeDAO();
    private Morador moradorAtual;
    private MoradorListController listController;

    @FXML
    public void initialize() {
        carregarUnidades();
        cbTipo.getSelectionModel().selectFirst();
        cbSituacao.getSelectionModel().selectFirst();
    }

    public void setListController(MoradorListController listController) {
        this.listController = listController;
    }

    public void setMorador(Morador morador) {
        this.moradorAtual = morador;
        lblTituloForm.setText("Editar Morador");
        txtNome.setText(morador.getNome());
        txtCpf.setText(morador.getCpf());
        txtTelefone.setText(morador.getTelefone());
        txtEmail.setText(morador.getEmail());
        cbTipo.setValue(morador.getTipo());
        cbSituacao.setValue(morador.getSituacao());

        for (Unidade u : cbUnidade.getItems()) {
            if (u.getId() == morador.getIdUnidade()) {
                cbUnidade.setValue(u);
                break;
            }
        }
    }

    private void carregarUnidades() {
        List<Unidade> unidades = unidadeDAO.listarTodas();
        cbUnidade.setItems(FXCollections.observableArrayList(unidades));
        cbUnidade.setConverter(new StringConverter<Unidade>() {
            @Override
            public String toString(Unidade u) {
                return u == null ? "" : u.getBloco() + "-" + u.getNumero();
            }
            @Override
            public Unidade fromString(String string) { return null; }
        });
    }

    @FXML
    private void onSalvar() {
        if (!validarCampos()) return;

        if (moradorAtual == null) {
            moradorAtual = new Morador();
        }

        moradorAtual.setNome(txtNome.getText().trim());
        moradorAtual.setCpf(txtCpf.getText().trim().replaceAll("[^0-9]", "")); // salva so numeros
        moradorAtual.setTelefone(txtTelefone.getText().trim());
        moradorAtual.setEmail(txtEmail.getText().trim());
        moradorAtual.setIdUnidade(cbUnidade.getValue().getId());
        moradorAtual.setTipo(cbTipo.getValue());
        moradorAtual.setSituacao(cbSituacao.getValue());

        try {
            if (moradorAtual.getId() == 0) {
                moradorDAO.salvar(moradorAtual);
                exibirAlertaSucesso("Morador cadastrado com sucesso!");
            } else {
                moradorDAO.atualizar(moradorAtual);
                exibirAlertaSucesso("Morador atualizado com sucesso!");
            }
            if (listController != null) {
                listController.carregarDados();
            }
            onCancelar();
        } catch (Exception e) {
            exibirAlertaErro("Erro ao salvar: " + e.getMessage());
        }
    }

    @FXML
    private void onCancelar() {
        Stage stage = (Stage) btnCancelar().getScene().getWindow();
        stage.close();
    }

    private Button btnCancelar() {
        return (Button) lblTituloForm.getScene().lookup("#btnCancelar");
    }

    private boolean validarCampos() {
        if (txtNome.getText().trim().isEmpty() || txtCpf.getText().trim().isEmpty() || cbUnidade.getValue() == null) {
            exibirAlertaErro("Nome, CPF e Unidade são obrigatórios.");
            return false;
        }

        String cpfNumeros = txtCpf.getText().replaceAll("[^0-9]", "");
        if (!ValidadorCPF.isValido(cpfNumeros)) {
            exibirAlertaErro("CPF inválido. Verifique os dígitos.");
            return false;
        }

        int idAtual = moradorAtual != null ? moradorAtual.getId() : 0;
        if (moradorDAO.existeCpf(cpfNumeros, idAtual)) {
            exibirAlertaErro("Este CPF já está cadastrado no sistema.");
            return false;
        }

        return true;
    }

    private void exibirAlertaSucesso(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sucesso");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void exibirAlertaErro(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro de Validação");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}

