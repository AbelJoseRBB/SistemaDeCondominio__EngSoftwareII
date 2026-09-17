package com.condomanager.controller.ocorrencia;

import com.condomanager.model.Ocorrencia;
import com.condomanager.service.OcorrenciaService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * Controller do formulario de cadastro e edicao de Ocorrencia.
 * Recebe uma Ocorrencia opcionalmente via setOcorrencia() para o modo edicao.
 * O Dialog que exibe este formulario e gerenciado pelo OcorrenciaListController.
 * Segue o mesmo padrao do UnidadeFormController.
 */
public class OcorrenciaFormController {

    // ------------------------------------------------------------------
    // Componentes injetados pelo FXMLLoader
    // ------------------------------------------------------------------
    @FXML private ComboBox<String> cmbCategoria;
    @FXML private ComboBox<String> cmbSituacao;
    @FXML private TextField        txtTitulo;
    @FXML private TextField        txtLocal;
    @FXML private TextArea         txtDescricao;
    @FXML private TextArea         txtObservacoes;
    @FXML private Button           btnSalvar;
    @FXML private Button           btnCancelar;

    // ------------------------------------------------------------------
    // Estado interno
    // ------------------------------------------------------------------
    private final OcorrenciaService service = new OcorrenciaService();

    /** Ocorrencia sendo editada. Null quando for um novo cadastro. */
    private Ocorrencia ocorrenciaEmEdicao = null;

    /** Referencia ao Dialog pai, usada para fechar programaticamente. */
    private Dialog<?> dialog;

    // ------------------------------------------------------------------
    // Inicializacao
    // ------------------------------------------------------------------

    @FXML
    public void initialize() {
        // Popula o ComboBox de categorias com os valores do banco
        cmbCategoria.setItems(FXCollections.observableArrayList(
            "RECLAMACAO", "INFORMACAO", "MANUTENCAO", "SEGURANCA"
        ));
        cmbCategoria.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : categoriaExibicao(item));
            }
        });
        cmbCategoria.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : categoriaExibicao(item));
            }
        });
        cmbCategoria.getSelectionModel().selectFirst();

        // Popula o ComboBox de situacao
        cmbSituacao.setItems(FXCollections.observableArrayList(
            "ABERTA", "EM_ANDAMENTO", "ENCERRADA"
        ));
        cmbSituacao.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : situacaoExibicao(item));
            }
        });
        cmbSituacao.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : situacaoExibicao(item));
            }
        });

        // No cadastro: situacao bloqueada em "ABERTA" (definida automaticamente pelo service)
        cmbSituacao.getSelectionModel().select("ABERTA");
        cmbSituacao.setDisable(true);
    }

    // ------------------------------------------------------------------
    // API publica — chamada pelo OcorrenciaListController antes de exibir
    // ------------------------------------------------------------------

    /**
     * Preenche o formulario com os dados de uma ocorrencia existente (modo edicao).
     * Tambem libera o ComboBox de situacao para que o usuario possa altera-la.
     */
    public void setOcorrencia(Ocorrencia ocorrencia) {
        this.ocorrenciaEmEdicao = ocorrencia;

        cmbCategoria.getSelectionModel().select(ocorrencia.getCategoria());
        cmbSituacao.getSelectionModel().select(ocorrencia.getSituacao());
        cmbSituacao.setDisable(false); // libera situacao na edicao

        txtTitulo.setText(ocorrencia.getTitulo());
        txtLocal.setText(
            ocorrencia.getLocalFormatado() != null ? ocorrencia.getLocalFormatado() : "");
        txtDescricao.setText(ocorrencia.getDescricao());
        txtObservacoes.setText(ocorrencia.getResposta() != null ? ocorrencia.getResposta() : "");
    }

    /**
     * Recebe a referencia do Dialog pai para fechar programaticamente.
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

        Ocorrencia ocorrencia = montarOcorrencia();

        try {
            if (ocorrenciaEmEdicao == null) {
                // Novo cadastro: service define situacao="ABERTA" e dataAbertura automaticamente
                service.registrar(ocorrencia);
            } else {
                // Edicao: preserva o id e a dataAbertura original
                ocorrencia.setId(ocorrenciaEmEdicao.getId());
                ocorrencia.setDataAbertura(ocorrenciaEmEdicao.getDataAbertura());
                service.atualizar(ocorrencia);
            }
            fecharDialog();

        } catch (IllegalArgumentException e) {
            // Erro de validacao vindo do service (campo obrigatorio vazio)
            mostrarAlerta(Alert.AlertType.WARNING, "Campo obrigatório", e.getMessage());
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
        txtTitulo.getStyleClass().remove("field-error");
        txtDescricao.getStyleClass().remove("field-error");
        cmbCategoria.getStyleClass().remove("field-error");

        if (cmbCategoria.getSelectionModel().isEmpty()) {
            cmbCategoria.getStyleClass().add("field-error");
            valido = false;
        }

        if (txtTitulo.getText() == null || txtTitulo.getText().isBlank()) {
            txtTitulo.getStyleClass().add("field-error");
            valido = false;
        }

        if (txtDescricao.getText() == null || txtDescricao.getText().isBlank()) {
            txtDescricao.getStyleClass().add("field-error");
            valido = false;
        }

        if (!valido) {
            mostrarAlerta(Alert.AlertType.WARNING,
                "Campos obrigatórios",
                "Preencha Categoria, Título e Descrição antes de salvar.");
        }

        return valido;
    }

    // ------------------------------------------------------------------
    // Helpers privados
    // ------------------------------------------------------------------

    /**
     * Constroi um objeto Ocorrencia com os valores atuais dos campos do formulario.
     */
    private Ocorrencia montarOcorrencia() {
        Ocorrencia o = new Ocorrencia();
        o.setCategoria(cmbCategoria.getValue());
        o.setSituacao(cmbSituacao.getValue());
        o.setTitulo(txtTitulo.getText().trim());
        o.setLocalFormatado(emptyToNull(txtLocal.getText()));
        o.setDescricao(txtDescricao.getText().trim());
        o.setResposta(emptyToNull(txtObservacoes.getText()));
        return o;
    }

    /** Fecha o Dialog pai sem retornar resultado. */
    private void fecharDialog() {
        if (dialog != null) {
            dialog.setResult(null);
            dialog.close();
        }
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

    private String situacaoExibicao(String situacao) {
        return switch (situacao == null ? "" : situacao.toUpperCase()) {
            case "ABERTA"       -> "Aberta";
            case "EM_ANDAMENTO" -> "Em andamento";
            case "ENCERRADA"    -> "Encerrada";
            default             -> situacao;
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



