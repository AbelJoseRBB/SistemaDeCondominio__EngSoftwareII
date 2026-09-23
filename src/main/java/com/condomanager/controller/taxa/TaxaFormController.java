package com.condomanager.controller.taxa;

import com.condomanager.dao.TaxaDAO;
import com.condomanager.dao.UnidadeDAO;
import com.condomanager.model.Taxa;
import com.condomanager.model.Unidade;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

public class TaxaFormController {

    @FXML private Label lblTituloForm;
    @FXML private ComboBox<Unidade> cbUnidade;
    @FXML private TextField txtValor;
    @FXML private DatePicker dpVencimento;
    @FXML private ComboBox<String> cbSituacao;

    private final TaxaDAO taxaDAO = new TaxaDAO();
    private final UnidadeDAO unidadeDAO = new UnidadeDAO();

    private Taxa taxaEdicao;
    private Dialog<Void> dialog;

    @FXML
    public void initialize() {
        cbSituacao.setItems(FXCollections.observableArrayList("PENDENTE", "PAGO", "ATRASADO"));
        cbSituacao.setValue("PENDENTE");

        carregarUnidades();
    }

    private void carregarUnidades() {
        cbUnidade.setItems(FXCollections.observableArrayList(unidadeDAO.listarTodas()));
        cbUnidade.setConverter(new StringConverter<>() {
            @Override
            public String toString(Unidade u) {
                return u == null ? "" : "Bloco " + u.getBloco() + " - " + u.getNumero();
            }
            @Override
            public Unidade fromString(String string) { return null; }
        });
    }

    public void setDialog(Dialog<Void> dialog) {
        this.dialog = dialog;
    }

    public void setTaxa(Taxa t) {
        this.taxaEdicao = t;
        lblTituloForm.setText("Editar Taxa Condominial");

        // Seleciona a unidade no ComboBox
        cbUnidade.getItems().stream()
                 .filter(u -> u.getId() == t.getIdUnidade())
                 .findFirst().ifPresent(u -> cbUnidade.setValue(u));

        txtValor.setText(t.getValor() != null ? t.getValor().toString().replace(".", ",") : "");
        dpVencimento.setValue(t.getVencimento());
        cbSituacao.setValue(t.getSituacao());
    }

    @FXML
    private void onSalvar() {
        if (!validarFormulario()) return;

        Taxa taxa = (taxaEdicao != null) ? taxaEdicao : new Taxa();
        
        taxa.setIdUnidade(cbUnidade.getValue().getId());
        taxa.setValor(new BigDecimal(txtValor.getText().replace(".", "").replace(",", ".")));
        taxa.setVencimento(dpVencimento.getValue());
        taxa.setSituacao(cbSituacao.getValue());

        String mes = taxa.getVencimento().getMonth().getDisplayName(TextStyle.SHORT, new Locale("pt", "BR"));
        String ref = mes.substring(0, 1).toUpperCase() + mes.substring(1).toLowerCase() + "/" + taxa.getVencimento().getYear();
        taxa.setDescricao("Taxa Condominial - " + ref);

        try {
            if (taxaEdicao == null) {
                taxaDAO.salvar(taxa);
                mostrarMensagem("Sucesso", "Taxa cadastrada com sucesso!", Alert.AlertType.INFORMATION);
            } else {
                taxaDAO.atualizar(taxa);
                mostrarMensagem("Sucesso", "Taxa atualizada com sucesso!", Alert.AlertType.INFORMATION);
            }
            if (dialog != null) dialog.close();
        } catch (Exception e) {
            mostrarMensagem("Erro", "Falha ao salvar a taxa: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean validarFormulario() {
        if (cbUnidade.getValue() == null) {
            mostrarMensagem("Validação", "Selecione uma unidade.", Alert.AlertType.WARNING);
            return false;
        }

        String valorStr = txtValor.getText();
        if (valorStr == null || valorStr.trim().isEmpty()) {
            mostrarMensagem("Validação", "Informe o valor da taxa.", Alert.AlertType.WARNING);
            return false;
        }

        try {
            new BigDecimal(valorStr.replace(".", "").replace(",", "."));
        } catch (NumberFormatException e) {
            mostrarMensagem("Validação", "Valor inválido. Use o formato 0,00 sem pontos de milhar.", Alert.AlertType.WARNING);
            return false;
        }

        if (dpVencimento.getValue() == null) {
            mostrarMensagem("Validação", "Informe a data de vencimento.", Alert.AlertType.WARNING);
            return false;
        }

        if (cbSituacao.getValue() == null) {
            mostrarMensagem("Validação", "Selecione a situação da taxa.", Alert.AlertType.WARNING);
            return false;
        }

        if (taxaEdicao == null && dpVencimento.getValue().isBefore(LocalDate.now())) {
            mostrarMensagem("Validação", "A data de vencimento de uma nova taxa não pode ser anterior à data atual.", Alert.AlertType.WARNING);
            return false;
        }

        if (taxaEdicao != null && "PAGO".equalsIgnoreCase(taxaEdicao.getSituacao())) {
            BigDecimal valorNovo = new BigDecimal(valorStr.replace(".", "").replace(",", "."));
            if (taxaEdicao.getValor().compareTo(valorNovo) != 0) {
                mostrarMensagem("Validação", "Não é permitido alterar o valor de uma taxa já consolidada (PAGA).", Alert.AlertType.WARNING);
                return false;
            }
        }

        return true;
    }

    @FXML
    private void onCancelar() {
        if (dialog != null) dialog.close();
    }

    private void mostrarMensagem(String titulo, String mensagem, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(titulo);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}
