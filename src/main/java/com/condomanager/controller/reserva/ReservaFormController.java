package com.condomanager.controller.reserva;

import com.condomanager.model.Reserva;
import com.condomanager.model.Unidade;
import com.condomanager.service.ReservaService;
import com.condomanager.service.UnidadeService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.time.LocalDate;
import javafx.scene.control.DateCell;
import java.time.LocalDateTime;
/**
 * Controller do formulario de cadastro e edicao de Reserva.
 * Recebe uma Reserva opcionalmente via setReserva() para o modo edicao.
 * O Dialog que exibe este formulario e gerenciado pelo ReservaListController.
 * Segue o mesmo padrao do OcorrenciaFormController.
 */
public class ReservaFormController {

    // ------------------------------------------------------------------
    // Componentes injetados pelo FXMLLoader
    // ------------------------------------------------------------------
    @FXML private ComboBox<Unidade>  cmbUnidade;
    @FXML private ComboBox<String>   cmbAreaComum;
    @FXML private DatePicker         dpData;
    @FXML private TextField          txtHoraInicio;
    @FXML private TextField          txtHoraFim;
    @FXML private TextArea           txtObservacoes;
    @FXML private Button             btnSalvar;
    @FXML private Button             btnCancelar;

    // ------------------------------------------------------------------
    // Estado interno
    // ------------------------------------------------------------------
    private final ReservaService  reservaService  = new ReservaService();
    private final UnidadeService  unidadeService  = new UnidadeService();

    private static final DateTimeFormatter FMT_HORA = DateTimeFormatter.ofPattern("HH:mm");

    /** Reserva sendo editada. Null quando for um novo cadastro. */
    private Reserva reservaEmEdicao = null;

    /** Referencia ao Dialog pai, usada para fechar programaticamente. */
    private Dialog<?> dialog;

    // ------------------------------------------------------------------
    // Inicializacao
    // ------------------------------------------------------------------

    @FXML
    public void initialize() {
        carregarUnidades();
        configurarAreaComum();
        configurarData();
    }

    private void configurarData() {
        dpData.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate data, boolean vazio) {
                super.updateItem(data, vazio);
                setDisable(vazio || data.isBefore(LocalDate.now()));
            }
        });
    }

    /**
     * Carrega todas as unidades do banco e popula o ComboBox,
     * exibindo "Bloco X - NNN" para cada opcao.
     */
    private void carregarUnidades() {
        try {
            List<Unidade> unidades = unidadeService.listarTodas();
            cmbUnidade.setItems(FXCollections.observableArrayList(unidades));

            // Exibe "Bloco A - 101" no ComboBox em vez do toString() padrao
            cmbUnidade.setButtonCell(new ListCell<>() {
                @Override protected void updateItem(Unidade u, boolean empty) {
                    super.updateItem(u, empty);
                    setText(empty || u == null ? "" : "Bloco " + u.getBloco() + " – " + u.getNumero());
                }
            });
            cmbUnidade.setCellFactory(lv -> new ListCell<>() {
                @Override protected void updateItem(Unidade u, boolean empty) {
                    super.updateItem(u, empty);
                    setText(empty || u == null ? "" : "Bloco " + u.getBloco() + " – " + u.getNumero());
                }
            });

        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR,
                "Erro ao carregar unidades", e.getMessage());
        }
    }

    /**
     * Popula o ComboBox de areas comuns com a lista fixa do condominio.
     * Ajuste os itens conforme as areas reais do condominio.
     */
    private void configurarAreaComum() {
        cmbAreaComum.setItems(FXCollections.observableArrayList(
            "Salão de Festas",
            "Churrasqueira",
            "Quadra Poliesportiva",
            "Piscina",
            "Academia",
            "Salão de Jogos",
            "Espaço Gourmet",
            "Playground"
        ));
    }

    // ------------------------------------------------------------------
    // API publica — chamada pelo ReservaListController antes de exibir
    // ------------------------------------------------------------------

    /**
     * Preenche o formulario com os dados de uma reserva existente (modo edicao).
     * Localiza a unidade na lista do ComboBox pelo id para seleciona-la.
     */
    public void setReserva(Reserva reserva) {
        this.reservaEmEdicao = reserva;

        // Seleciona a unidade correspondente no ComboBox (comparando por id)
        cmbUnidade.getItems().stream()
            .filter(u -> u.getId() == reserva.getIdUnidade())
            .findFirst()
            .ifPresent(u -> cmbUnidade.getSelectionModel().select(u));

        cmbAreaComum.getSelectionModel().select(reserva.getAreaComum());
        dpData.setValue(reserva.getData());

        if (reserva.getHoraInicio() != null) {
            txtHoraInicio.setText(reserva.getHoraInicio().format(FMT_HORA));
        }
        if (reserva.getHoraFim() != null) {
            txtHoraFim.setText(reserva.getHoraFim().format(FMT_HORA));
        }

        txtObservacoes.setText(
            reserva.getObservacoes() != null ? reserva.getObservacoes() : "");
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

        // Parse dos horarios (ja validados por validarCampos())
        LocalTime horaInicio = parseHora(txtHoraInicio.getText());
        LocalTime horaFim    = parseHora(txtHoraFim.getText());

        // parseHora retorna null apenas se o texto nao puder ser convertido;
        // como ja validamos o formato em validarCampos(), isso nao deveria ocorrer.
        if (horaInicio == null || horaFim == null) return;

        Reserva reserva = montarReserva(horaInicio, horaFim);

        try {
            if (reservaEmEdicao == null) {
                // Novo cadastro: service define situacao="CONFIRMADA" automaticamente
                reservaService.registrar(reserva);
            } else {
                // Edicao: preserva o id
                reserva.setId(reservaEmEdicao.getId());
                reservaService.atualizar(reserva);
            }
            fecharDialog();

        } catch (IllegalArgumentException e) {
            // Campos invalidos (validacao do service)
            mostrarAlerta(Alert.AlertType.WARNING, "Campo inválido", e.getMessage());
        } catch (IllegalStateException e) {
            // Conflito de horario — mensagem detalhada do service
            mostrarAlerta(Alert.AlertType.WARNING, "Conflito de horário", e.getMessage());
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
     * Valida todos os campos obrigatorios do formulario.
     * Destaca em vermelho (field-error) os campos invalidos.
     *
     * @return true se todos os campos estao corretos.
     */
    private boolean validarCampos() {
        boolean valido = true;
        LocalTime horaInicio = parseHora(txtHoraInicio.getText());
        // Reseta estilos de erro anteriores
        cmbUnidade.getStyleClass().remove("field-error");
        cmbAreaComum.getStyleClass().remove("field-error");
        dpData.getStyleClass().remove("field-error");
        txtHoraInicio.getStyleClass().remove("field-error");
        txtHoraFim.getStyleClass().remove("field-error");

        if (cmbUnidade.getSelectionModel().isEmpty()) {
            cmbUnidade.getStyleClass().add("field-error");
            valido = false;
        }
        if (cmbAreaComum.getSelectionModel().isEmpty()) {
            cmbAreaComum.getStyleClass().add("field-error");
            valido = false;
        }
        if (dpData.getValue() != null && horaInicio != null) {
            LocalDateTime inicioReserva = LocalDateTime.of(dpData.getValue(), horaInicio);

            if (inicioReserva.isBefore(LocalDateTime.now())) {
                dpData.getStyleClass().add("field-error");
                txtHoraInicio.getStyleClass().add("field-error");

                mostrarAlerta(Alert.AlertType.WARNING,
                        "Horário inválido",
                        "Não é possível realizar uma reserva para uma data e horário que já passaram.");

                return false;
            }
        }

        // Valida formato HH:mm dos horarios
        if (parseHora(txtHoraInicio.getText()) == null) {
            txtHoraInicio.getStyleClass().add("field-error");
            valido = false;
        }
        if (parseHora(txtHoraFim.getText()) == null) {
            txtHoraFim.getStyleClass().add("field-error");
            valido = false;
        }

        if (!valido) {
            mostrarAlerta(Alert.AlertType.WARNING,
                    "Campos inválidos",
                    "Preencha todos os campos marcados.\n"
                    + "A data da reserva não pode ser anterior à data atual.\n"
                    + "Os horários devem estar no formato HH:mm (ex.: 08:00, 14:30).");
        }
        return valido;
    }

    // ------------------------------------------------------------------
    // Helpers privados
    // ------------------------------------------------------------------

    /**
     * Constroi um objeto Reserva com os valores atuais dos campos do formulario.
     */
    private Reserva montarReserva(LocalTime horaInicio, LocalTime horaFim) {
        Reserva r = new Reserva();
        Unidade unidade = cmbUnidade.getValue();
        r.setIdUnidade(unidade.getId());
        r.setAreaComum(cmbAreaComum.getValue());
        r.setData(dpData.getValue());
        r.setHoraInicio(horaInicio);
        r.setHoraFim(horaFim);
        r.setObservacoes(emptyToNull(txtObservacoes.getText()));
        return r;
    }

    /**
     * Converte um texto no formato "HH:mm" para LocalTime.
     *
     * @return o LocalTime correspondente, ou null se o formato for invalido.
     */
    private LocalTime parseHora(String texto) {
        if (texto == null || texto.isBlank()) return null;
        try {
            return LocalTime.parse(texto.trim(), FMT_HORA);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** Fecha o Dialog pai sem retornar resultado. */
    private void fecharDialog() {
        if (dialog != null) {
            dialog.setResult(null);
            dialog.close();
        }
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



