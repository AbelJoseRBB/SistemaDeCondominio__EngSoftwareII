package com.condomanager.controller.relatorio;

import com.condomanager.model.*;
import com.condomanager.service.RelatorioService;
import com.condomanager.util.ExcelExporter;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class RelatorioListController {

    @FXML
    private DatePicker dpDataInicial;

    @FXML
    private DatePicker dpDataFinal;

    @FXML
    private TableView<Object> tabelaRelatorio;

    @FXML
    private Label lblTituloRelatorio;

    @FXML
    private Label lblMensagem;

    @FXML
    private Button btnExportar;

    private final RelatorioService relatorioService = new RelatorioService();

    private final DateTimeFormatter formatadorData =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        lblMensagem.setText("");
        lblTituloRelatorio.setText("Selecione um relatório");
    }

    // ---------------------------------------------------------
    // ACOES DOS RELATORIOS
    // ---------------------------------------------------------

    @FXML
    private void onGerarUnidades() {
        List<Unidade> unidades =
                relatorioService.gerarRelatorioUnidades();

        configurarTabelaUnidades();
        exibirDados(unidades, "Relatório de Unidades");
    }

    @FXML
    private void onGerarMoradores() {
        List<Morador> moradores =
                relatorioService.gerarRelatorioMoradores();

        configurarTabelaMoradores();
        exibirDados(moradores, "Relatório de Moradores");
    }

    @FXML
    private void onGerarTaxas() {
        try {
            List<Taxa> taxas =
                    relatorioService.gerarRelatorioTaxas(
                            dpDataInicial.getValue(),
                            dpDataFinal.getValue()
                    );

            configurarTabelaTaxas();
            exibirDados(taxas, "Relatório de Taxas Condominiais");

        } catch (IllegalArgumentException e) {
            exibirErro(e.getMessage());
        }
    }

    @FXML
    private void onGerarReservas() {
        try {
            List<Reserva> reservas =
                    relatorioService.gerarRelatorioReservas(
                            dpDataInicial.getValue(),
                            dpDataFinal.getValue()
                    );

            configurarTabelaReservas();
            exibirDados(reservas, "Relatório de Reservas");

        } catch (IllegalArgumentException e) {
            exibirErro(e.getMessage());
        }
    }

    @FXML
    private void onGerarOcorrencias() {
        try {
            List<Ocorrencia> ocorrencias =
                    relatorioService.gerarRelatorioOcorrencias(
                            dpDataInicial.getValue(),
                            dpDataFinal.getValue()
                    );

            configurarTabelaOcorrencias();
            exibirDados(ocorrencias, "Relatório de Ocorrências");

        } catch (IllegalArgumentException e) {
            exibirErro(e.getMessage());
        }
    }

    @FXML
    private void onGerarManutencoes() {
        try {
            List<Manutencao> manutencoes =
                    relatorioService.gerarRelatorioManutencoes(
                            dpDataInicial.getValue(),
                            dpDataFinal.getValue()
                    );

            configurarTabelaManutencoes();
            exibirDados(manutencoes, "Relatório de Manutenções");

        } catch (IllegalArgumentException e) {
            exibirErro(e.getMessage());
        }
    }

    // ---------------------------------------------------------
    // CONFIGURACAO DAS TABELAS
    // ---------------------------------------------------------

    private void configurarTabelaUnidades() {
        tabelaRelatorio.getColumns().clear();

        adicionarColuna("Bloco",
                u -> ((Unidade) u).getBloco());

        adicionarColuna("Número",
                u -> ((Unidade) u).getNumero());

        adicionarColuna("Proprietário",
                u -> ((Unidade) u).getProprietario());

        adicionarColuna("Situação",
                u -> ((Unidade) u).getSituacao());

        adicionarColuna("Telefone",
                u -> ((Unidade) u).getTelefoneContato());

        adicionarColuna("E-mail",
                u -> ((Unidade) u).getEmailContato());
    }

    private void configurarTabelaMoradores() {
        tabelaRelatorio.getColumns().clear();

        adicionarColuna("Nome",
                m -> ((Morador) m).getNome());

        adicionarColuna("CPF",
                m -> ((Morador) m).getCpf());

        adicionarColuna("Unidade",
                m -> String.valueOf(((Morador) m).getIdUnidade()));

        adicionarColuna("Telefone",
                m -> ((Morador) m).getTelefone());

        adicionarColuna("E-mail",
                m -> ((Morador) m).getEmail());

        adicionarColuna("Tipo",
                m -> ((Morador) m).getTipo());

        adicionarColuna("Situação",
                m -> ((Morador) m).getSituacao());
    }

    private void configurarTabelaTaxas() {
        tabelaRelatorio.getColumns().clear();

        adicionarColuna("Unidade",
                t -> ((Taxa) t).getUnidadeTexto());

        adicionarColuna("Descrição",
                t -> ((Taxa) t).getDescricao());

        adicionarColuna("Valor",
                t -> ((Taxa) t).getValor() != null
                        ? ((Taxa) t).getValor().toString()
                        : "");

        adicionarColuna("Vencimento",
                t -> formatarData(((Taxa) t).getVencimento()));

        adicionarColuna("Situação",
                t -> ((Taxa) t).getSituacao());
    }

    private void configurarTabelaReservas() {
        tabelaRelatorio.getColumns().clear();

        adicionarColuna("Unidade",
                r -> ((Reserva) r).getUnidadeFormatada());

        adicionarColuna("Área comum",
                r -> ((Reserva) r).getAreaComum());

        adicionarColuna("Data",
                r -> formatarData(((Reserva) r).getData()));

        adicionarColuna("Início",
                r -> ((Reserva) r).getHoraInicio() != null
                        ? ((Reserva) r).getHoraInicio().toString()
                        : "");

        adicionarColuna("Fim",
                r -> ((Reserva) r).getHoraFim() != null
                        ? ((Reserva) r).getHoraFim().toString()
                        : "");

        adicionarColuna("Situação",
                r -> ((Reserva) r).getSituacao());
    }

    private void configurarTabelaOcorrencias() {
        tabelaRelatorio.getColumns().clear();

        adicionarColuna("Local",
                o -> ((Ocorrencia) o).getLocalFormatado());

        adicionarColuna("Título",
                o -> ((Ocorrencia) o).getTitulo());

        adicionarColuna("Categoria",
                o -> ((Ocorrencia) o).getCategoria());

        adicionarColuna("Situação",
                o -> ((Ocorrencia) o).getSituacao());

        adicionarColuna("Data de abertura",
                o -> ((Ocorrencia) o).getDataAbertura() != null
                        ? ((Ocorrencia) o).getDataAbertura()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                        : "");
    }

    private void configurarTabelaManutencoes() {
        tabelaRelatorio.getColumns().clear();

        adicionarColuna("Descrição",
                m -> ((Manutencao) m).getDescricao());

        adicionarColuna("Local",
                m -> ((Manutencao) m).getLocal());

        adicionarColuna("Responsável",
                m -> ((Manutencao) m).getResponsavel());

        adicionarColuna("Solicitação",
                m -> formatarData(((Manutencao) m).getDataSolicitacao()));

        adicionarColuna("Situação",
                m -> ((Manutencao) m).getSituacao());

        adicionarColuna("Custo",
                m -> ((Manutencao) m).getCusto() != null
                        ? ((Manutencao) m).getCusto().toString()
                        : "");
    }

    // ---------------------------------------------------------
    // METODOS AUXILIARES
    // ---------------------------------------------------------

    private void adicionarColuna(
            String titulo,
            java.util.function.Function<Object, String> valor) {

        TableColumn<Object, String> coluna =
                new TableColumn<>(titulo);

        coluna.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        valor.apply(cell.getValue())
                )
        );

        tabelaRelatorio.getColumns().add(coluna);
    }

    private void exibirDados(List<?> dados, String titulo) {
        lblTituloRelatorio.setText(titulo);
        tabelaRelatorio.setItems(FXCollections.observableArrayList(dados));

        if (dados.isEmpty()) {
            lblMensagem.setText("Nenhum registro encontrado para os critérios selecionados.");
            btnExportar.setDisable(true);
        } else {
            lblMensagem.setText(dados.size() + " registro(s) encontrado(s).");
            btnExportar.setDisable(false);
        }
    }

    private void exibirErro(String mensagem) {
        lblMensagem.setText(mensagem);
        tabelaRelatorio.getItems().clear();
        btnExportar.setDisable(true);
    }

    private String formatarData(LocalDate data) {
        return data != null ? data.format(formatadorData) : "";
    }

    @FXML
    private void onExportar() {
        if (tabelaRelatorio.getItems().isEmpty()) {
            lblMensagem.setText("Não há dados disponíveis para exportação.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salvar relatório");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Arquivo Excel (*.xlsx)", "*.xlsx")
        );
        fileChooser.setInitialFileName("relatorio.xlsx");

        File arquivo = fileChooser.showSaveDialog(tabelaRelatorio.getScene().getWindow());

        if (arquivo == null) {
            return;
        }

        try {
            ExcelExporter.exportar(tabelaRelatorio, arquivo);
            lblMensagem.setText("Relatório exportado com sucesso.");
        } catch (IOException e) {
            lblMensagem.setText("Erro ao exportar o relatório.");
            e.printStackTrace();
        }
    }

}