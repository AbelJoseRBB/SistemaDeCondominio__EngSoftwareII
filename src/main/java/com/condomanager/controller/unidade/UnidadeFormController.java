package com.condomanager.controller.unidade;

import com.condomanager.model.Unidade;
import com.condomanager.service.UnidadeService;
import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * Controller do formulario de cadastro e edicao de Unidade.
 * Recebe uma Unidade opcionalmente via setUnidade() para o modo edicao.
 * O Dialog que exibe este formulario e gerenciado pelo UnidadeListController.
 */
public class UnidadeFormController {

    // ------------------------------------------------------------------
    // Componentes injetados pelo FXMLLoader
    // ------------------------------------------------------------------
    @FXML private ComboBox<String> cmbBloco;
    @FXML private TextField        txtNumero;
    @FXML private TextField        txtProprietario;
    @FXML private ComboBox<String> cmbSituacao;
    @FXML private TextField        txtTelefone;
    @FXML private TextField        txtEmail;
    @FXML private Button           btnSalvar;
    @FXML private Button           btnCancelar;

    // ------------------------------------------------------------------
    // Estado interno
    // ------------------------------------------------------------------
    private final UnidadeService service = new UnidadeService();

    /** Unidade sendo editada. Null quando for um novo cadastro. */
    private Unidade unidadeEmEdicao = null;

    /** Referencia ao Dialog pai, usada para fechar programaticamente. */
    private Dialog<?> dialog;

    // ------------------------------------------------------------------
    // Inicializacao
    // ------------------------------------------------------------------

    @FXML
    public void initialize() {
        // Popula o ComboBox de blocos com A a Z
        for (char letra = 'A'; letra <= 'Z'; letra++) {
            cmbBloco.getItems().add(String.valueOf(letra));
        }
        cmbBloco.getSelectionModel().selectFirst(); // seleciona "A" por padrao

        // Popula o ComboBox de situacao com as opcoes de exibicao
        cmbSituacao.getItems().addAll("Ocupada", "Desocupada", "Alugada", "À Venda");
        cmbSituacao.getSelectionModel().selectFirst(); // seleciona "Ocupada" por padrao
    }

    // ------------------------------------------------------------------
    // API publica — chamada pelo UnidadeListController antes de exibir o dialog
    // ------------------------------------------------------------------

    /**
     * Preenche o formulario com os dados de uma unidade existente (modo edicao).
     * Se nao chamado, o formulario abre em branco para novo cadastro.
     */
    public void setUnidade(Unidade unidade) {
        this.unidadeEmEdicao = unidade;
        cmbBloco.getSelectionModel().select(unidade.getBloco());
        txtNumero.setText(unidade.getNumero());
        txtProprietario.setText(unidade.getProprietario());
        cmbSituacao.getSelectionModel().select(situacaoParaExibicao(unidade.getSituacao()));
        txtTelefone.setText(unidade.getTelefoneContato());
        txtEmail.setText(unidade.getEmailContato());
    }

    /**
     * Habilita ou desabilita o modo somente-leitura.
     * Em modo somente-leitura, todos os campos ficam desabilitados e o botao Salvar e ocultado.
     */
    public void setSomenteLeitura(boolean somenteLeitura) {
        cmbBloco.setDisable(somenteLeitura);
        txtNumero.setEditable(!somenteLeitura);
        txtProprietario.setEditable(!somenteLeitura);
        cmbSituacao.setDisable(somenteLeitura);
        txtTelefone.setEditable(!somenteLeitura);
        txtEmail.setEditable(!somenteLeitura);
        btnSalvar.setVisible(!somenteLeitura);
        btnSalvar.setManaged(!somenteLeitura);
    }

    /**
     * Recebe a referencia do Dialog pai para que os botoes do FXML
     * possam fecha-lo programaticamente.
     */
    public void setDialog(Dialog<?> dialog) {
        this.dialog = dialog;
    }

    // ------------------------------------------------------------------
    // Acoes dos botoes
    // ------------------------------------------------------------------

    @FXML
    private void onSalvar() {
        if (!validarCampos()) return;

        Unidade unidade = montarUnidade();

        try {
            if (unidadeEmEdicao == null) {
                // Novo cadastro
                service.cadastrar(unidade);
            } else {
                // Edicao de existente
                unidade.setId(unidadeEmEdicao.getId());
                service.atualizar(unidade);
            }
            fecharDialog();

        } catch (IllegalArgumentException e) {
            // Erro de campo obrigatorio vazio (nao deveria ocorrer apos validarCampos(),
            // mas serve como seguranca para erros vindos do service)
            mostrarAlerta(Alert.AlertType.WARNING, "Campo obrigatorio", e.getMessage());
        } catch (IllegalStateException e) {
            // Erro de unicidade: unidade duplicada
            mostrarAlerta(Alert.AlertType.ERROR, "Unidade duplicada", e.getMessage());
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro ao salvar", e.getMessage());
        }
    }

    @FXML
    private void onCancelar() {
        fecharDialog();
    }

    // ------------------------------------------------------------------
    // Validacao de campos
    // ------------------------------------------------------------------

    /**
     * Valida os campos obrigatorios e destaca em vermelho os que estao vazios.
     *
     * @return true se todos os campos obrigatorios estao preenchidos.
     */
    private boolean validarCampos() {
        boolean valido = true;

        // Reseta estilos de erro anteriores
        txtNumero.getStyleClass().remove("field-error");
        cmbBloco.getStyleClass().remove("field-error");

        if (cmbBloco.getSelectionModel().isEmpty()) {
            cmbBloco.getStyleClass().add("field-error");
            valido = false;
        }

        if (txtNumero.getText() == null || txtNumero.getText().isBlank()) {
            txtNumero.getStyleClass().add("field-error");
            valido = false;
        }

        if (!valido) {
            mostrarAlerta(Alert.AlertType.WARNING,
                "Campos obrigatorios",
                "Preencha os campos Bloco e Numero antes de salvar.");
        }

        return valido;
    }

    // ------------------------------------------------------------------
    // Helpers privados
    // ------------------------------------------------------------------

    /**
     * Constroi um objeto Unidade com os valores atuais dos campos do formulario.
     */
    private Unidade montarUnidade() {
        Unidade u = new Unidade();
        u.setBloco(cmbBloco.getValue());
        u.setNumero(txtNumero.getText().trim());
        u.setProprietario(emptyToNull(txtProprietario.getText()));
        u.setSituacao(exibicaoParaSituacao(cmbSituacao.getValue()));
        u.setTelefoneContato(emptyToNull(txtTelefone.getText()));
        u.setEmailContato(emptyToNull(txtEmail.getText()));
        return u;
    }

    /** Fecha o Dialog pai sem retornar resultado. */
    private void fecharDialog() {
        if (dialog != null) {
            dialog.setResult(null);
            dialog.close();
        }
    }

    /**
     * Converte o valor do banco de dados para o texto de exibicao no ComboBox.
     * Ex: "OCUPADO" → "Ocupada"
     */
    private String situacaoParaExibicao(String situacao) {
        return switch (situacao == null ? "" : situacao.toUpperCase()) {
            case "OCUPADO"    -> "Ocupada";
            case "DESOCUPADO" -> "Desocupada";
            case "ALUGUEL"    -> "Alugada";
            case "VENDA"      -> "À Venda";
            default           -> "Ocupada";
        };
    }

    /**
     * Converte o texto de exibicao do ComboBox para o valor do banco de dados.
     * Ex: "Ocupada" → "OCUPADO"
     */
    private String exibicaoParaSituacao(String exibicao) {
        return switch (exibicao == null ? "" : exibicao) {
            case "Ocupada"    -> "OCUPADO";
            case "Desocupada" -> "DESOCUPADO";
            case "Alugada"    -> "ALUGUEL";
            case "À Venda"    -> "VENDA";
            default           -> "DESOCUPADO";
        };
    }

    /** Retorna null se a string for nula ou apenas espacos. */
    private String emptyToNull(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(titulo);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}
