package com.condomanager.controller.relatorio;

import com.condomanager.model.*;
import com.condomanager.service.RelatorioService;
import com.condomanager.util.ExcelExporter;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;

public class RelatorioListController {

    @FXML private ComboBox<String> cbPeriodoTaxas;
    @FXML private ComboBox<String> cbPeriodoReservas;
    @FXML private ComboBox<String> cbPeriodoOcorrencias;
    @FXML private ComboBox<String> cbPeriodoManutencoes;

    @FXML private HBox boxPeriodoPersonalizadoTaxas;
    @FXML private HBox boxPeriodoPersonalizadoReservas;
    @FXML private HBox boxPeriodoPersonalizadoOcorrencias;
    @FXML private HBox boxPeriodoPersonalizadoManutencoes;

    @FXML private DatePicker dpDataInicialTaxas;
    @FXML private DatePicker dpDataFinalTaxas;
    @FXML private DatePicker dpDataInicialReservas;
    @FXML private DatePicker dpDataFinalReservas;
    @FXML private DatePicker dpDataInicialOcorrencias;
    @FXML private DatePicker dpDataFinalOcorrencias;
    @FXML private DatePicker dpDataInicialManutencoes;
    @FXML private DatePicker dpDataFinalManutencoes;

    @FXML private TableView<Object> tabelaRelatorio;
    @FXML private Label lblTituloRelatorio;
    @FXML private Label lblMensagem;

    private final RelatorioService relatorioService = new RelatorioService();
    private final DateTimeFormatter formatadorData = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter formatadorDataHora = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        lblMensagem.setText("");
        lblTituloRelatorio.setText("Selecione um relatório");

        configurarPeriodo(cbPeriodoTaxas, boxPeriodoPersonalizadoTaxas);
        configurarPeriodo(cbPeriodoReservas, boxPeriodoPersonalizadoReservas);
        configurarPeriodo(cbPeriodoOcorrencias, boxPeriodoPersonalizadoOcorrencias);
        configurarPeriodo(cbPeriodoManutencoes, boxPeriodoPersonalizadoManutencoes);
        tabelaRelatorio.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        tabelaRelatorio.setPlaceholder(new Label("Nenhum registro para exibir."));
    }

    // ---------------------------------------------------------
    // CONFIGURACAO DOS PERIODOS
    // ---------------------------------------------------------

    private void configurarPeriodo(ComboBox<String> comboBox, HBox boxPersonalizado) {
        comboBox.setItems(FXCollections.observableArrayList(
                "Todos os períodos",
                descricaoMesAtual(),
                descricaoMesAnterior(),
                "Último trimestre",
                "Último semestre",
                descricaoAnoAtual(),
                "Data personalizada"
        ));

        comboBox.getSelectionModel().selectFirst();

        comboBox.valueProperty().addListener((observable, valorAnterior, valorNovo) -> {
            boolean personalizado = "Data personalizada".equals(valorNovo);
            boxPersonalizado.setVisible(personalizado);
            boxPersonalizado.setManaged(personalizado);
        });
    }

    private String descricaoMesAtual() {
        LocalDate hoje = LocalDate.now();
        return "Mês atual (" + nomeMes(hoje.getMonthValue()) + "/" + hoje.getYear() + ")";
    }

    private String descricaoMesAnterior() {
        LocalDate mesAnterior = LocalDate.now().minusMonths(1);
        return "Mês anterior (" + nomeMes(mesAnterior.getMonthValue()) + "/" + mesAnterior.getYear() + ")";
    }

    private String descricaoAnoAtual() {
        return "Ano corrente (" + LocalDate.now().getYear() + ")";
    }

    private String nomeMes(int mes) {
        return switch (mes) {
            case 1 -> "Janeiro";
            case 2 -> "Fevereiro";
            case 3 -> "Março";
            case 4 -> "Abril";
            case 5 -> "Maio";
            case 6 -> "Junho";
            case 7 -> "Julho";
            case 8 -> "Agosto";
            case 9 -> "Setembro";
            case 10 -> "Outubro";
            case 11 -> "Novembro";
            case 12 -> "Dezembro";
            default -> "";
        };
    }

    private LocalDate[] obterPeriodo(ComboBox<String> comboBox, DatePicker dataInicial, DatePicker dataFinal) {
        String periodo = comboBox.getValue();
        LocalDate hoje = LocalDate.now();

        if (periodo == null || "Todos os períodos".equals(periodo)) {
            return new LocalDate[]{null, null};
        }

        if (periodo.startsWith("Mês atual")) {
            YearMonth mesAtual = YearMonth.from(hoje);
            return new LocalDate[]{mesAtual.atDay(1), mesAtual.atEndOfMonth()};
        }

        if (periodo.startsWith("Mês anterior")) {
            YearMonth mesAnterior = YearMonth.from(hoje.minusMonths(1));
            return new LocalDate[]{mesAnterior.atDay(1), mesAnterior.atEndOfMonth()};
        }

        if ("Último trimestre".equals(periodo)) {
            return new LocalDate[]{hoje.minusMonths(3), hoje};
        }

        if ("Último semestre".equals(periodo)) {
            return new LocalDate[]{hoje.minusMonths(6), hoje};
        }

        if (periodo.startsWith("Ano corrente")) {
            return new LocalDate[]{
                    LocalDate.of(hoje.getYear(), 1, 1),
                    LocalDate.of(hoje.getYear(), 12, 31)
            };
        }

        return new LocalDate[]{dataInicial.getValue(), dataFinal.getValue()};
    }

    // ---------------------------------------------------------
    // ACOES DOS RELATORIOS
    // ---------------------------------------------------------

    @FXML
    private void onGerarUnidades() {
        List<Unidade> unidades = relatorioService.gerarRelatorioUnidades();

        configurarTabelaUnidades();
        exibirDados(unidades, "Relatório de Unidades");
    }

    @FXML
    private void onGerarMoradores() {
        List<Morador> moradores = relatorioService.gerarRelatorioMoradores();

        configurarTabelaMoradores();
        exibirDados(moradores, "Relatório de Moradores");
    }

    @FXML
    private void onGerarTaxas() {
        try {
            LocalDate[] periodo = obterPeriodo(cbPeriodoTaxas, dpDataInicialTaxas, dpDataFinalTaxas);
            List<Taxa> taxas = relatorioService.gerarRelatorioTaxas(periodo[0], periodo[1]);

            configurarTabelaTaxas();
            exibirDados(taxas, "Relatório de Taxas Condominiais");
        } catch (IllegalArgumentException e) {
            exibirErro(e.getMessage());
        }
    }

    @FXML
    private void onGerarReservas() {
        try {
            LocalDate[] periodo = obterPeriodo(cbPeriodoReservas, dpDataInicialReservas, dpDataFinalReservas);
            List<Reserva> reservas = relatorioService.gerarRelatorioReservas(periodo[0], periodo[1]);

            configurarTabelaReservas();
            exibirDados(reservas, "Relatório de Reservas");
        } catch (IllegalArgumentException e) {
            exibirErro(e.getMessage());
        }
    }

    @FXML
    private void onGerarOcorrencias() {
        try {
            LocalDate[] periodo = obterPeriodo(cbPeriodoOcorrencias, dpDataInicialOcorrencias, dpDataFinalOcorrencias);
            List<Ocorrencia> ocorrencias = relatorioService.gerarRelatorioOcorrencias(periodo[0], periodo[1]);

            configurarTabelaOcorrencias();
            exibirDados(ocorrencias, "Relatório de Ocorrências");
        } catch (IllegalArgumentException e) {
            exibirErro(e.getMessage());
        }
    }

    @FXML
    private void onGerarManutencoes() {
        try {
            LocalDate[] periodo = obterPeriodo(cbPeriodoManutencoes, dpDataInicialManutencoes, dpDataFinalManutencoes);
            List<Manutencao> manutencoes = relatorioService.gerarRelatorioManutencoes(periodo[0], periodo[1]);

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

        adicionarColuna("Bloco", 0.10, u -> ((Unidade) u).getBloco());
        adicionarColuna("Número", 0.10, u -> ((Unidade) u).getNumero());
        adicionarColuna("Proprietário", 0.22, u -> ((Unidade) u).getProprietario());
        adicionarColuna("Situação", 0.12, u -> ((Unidade) u).getSituacao());
        adicionarColuna("Telefone", 0.16, u -> ((Unidade) u).getTelefoneContato());
        adicionarColuna("E-mail", 0.30, u -> ((Unidade) u).getEmailContato());
    }

    private void configurarTabelaMoradores() {
        tabelaRelatorio.getColumns().clear();

        adicionarColuna("Nome", 0.19, m -> ((Morador) m).getNome());
        adicionarColuna("CPF", 0.13, m -> ((Morador) m).getCpf());
        adicionarColuna("Unidade", 0.09, m -> String.valueOf(((Morador) m).getIdUnidade()));
        adicionarColuna("Telefone", 0.15, m -> ((Morador) m).getTelefone());
        adicionarColuna("E-mail", 0.24, m -> ((Morador) m).getEmail());
        adicionarColuna("Tipo", 0.11, m -> ((Morador) m).getTipo());
        adicionarColuna("Situação", 0.09, m -> ((Morador) m).getSituacao());
    }

    private void configurarTabelaTaxas() {
        tabelaRelatorio.getColumns().clear();

        adicionarColuna("Unidade", 0.20, t -> ((Taxa) t).getUnidadeTexto());
        adicionarColuna("Descrição", 0.32, t -> ((Taxa) t).getDescricao());
        adicionarColuna("Valor", 0.14, t -> ((Taxa) t).getValor() != null ? ((Taxa) t).getValor().toString() : "");
        adicionarColuna("Vencimento", 0.17, t -> formatarData(((Taxa) t).getVencimento()));
        adicionarColuna("Situação", 0.17, t -> ((Taxa) t).getSituacao());
    }

    private void configurarTabelaReservas() {
        tabelaRelatorio.getColumns().clear();

        adicionarColuna("Unidade", 0.18, r -> ((Reserva) r).getUnidadeFormatada());
        adicionarColuna("Área comum", 0.27, r -> ((Reserva) r).getAreaComum());
        adicionarColuna("Data", 0.15, r -> formatarData(((Reserva) r).getData()));
        adicionarColuna("Início", 0.12, r -> ((Reserva) r).getHoraInicio() != null ? ((Reserva) r).getHoraInicio().toString() : "");
        adicionarColuna("Fim", 0.12, r -> ((Reserva) r).getHoraFim() != null ? ((Reserva) r).getHoraFim().toString() : "");
        adicionarColuna("Situação", 0.16, r -> ((Reserva) r).getSituacao());
    }

    private void configurarTabelaOcorrencias() {
        tabelaRelatorio.getColumns().clear();

        adicionarColuna("Local", 0.20, o -> ((Ocorrencia) o).getLocalFormatado());
        adicionarColuna("Título", 0.28, o -> ((Ocorrencia) o).getTitulo());
        adicionarColuna("Categoria", 0.17, o -> ((Ocorrencia) o).getCategoria());
        adicionarColuna("Situação", 0.15, o -> ((Ocorrencia) o).getSituacao());
        adicionarColuna("Data de abertura", 0.20, o -> ((Ocorrencia) o).getDataAbertura() != null
                ? ((Ocorrencia) o).getDataAbertura().format(formatadorDataHora) : "");
    }

    private void configurarTabelaManutencoes() {
        tabelaRelatorio.getColumns().clear();

        adicionarColuna("Descrição", 0.27, m -> ((Manutencao) m).getDescricao());
        adicionarColuna("Local", 0.16, m -> ((Manutencao) m).getLocal());
        adicionarColuna("Responsável", 0.19, m -> ((Manutencao) m).getResponsavel());
        adicionarColuna("Solicitação", 0.14, m -> formatarData(((Manutencao) m).getDataSolicitacao()));
        adicionarColuna("Situação", 0.13, m -> ((Manutencao) m).getSituacao());
        adicionarColuna("Custo", 0.11, m -> ((Manutencao) m).getCusto() != null ? ((Manutencao) m).getCusto().toString() : "");
    }

    // ---------------------------------------------------------
    // METODOS AUXILIARES
    // ---------------------------------------------------------

    private void adicionarColuna(String titulo, double proporcao, Function<Object, String> valor) {
        TableColumn<Object, String> coluna = new TableColumn<>(titulo);

        coluna.setCellValueFactory(cell -> new SimpleStringProperty(valor.apply(cell.getValue())));
        coluna.setResizable(false);
        coluna.setMinWidth(60);
        coluna.prefWidthProperty().bind(tabelaRelatorio.widthProperty().multiply(proporcao));

        tabelaRelatorio.getColumns().add(coluna);
    }

    private void exibirDados(List<?> dados, String titulo) {
        lblTituloRelatorio.setText(titulo);
        tabelaRelatorio.setItems(FXCollections.observableArrayList(dados));

        if (dados.isEmpty()) {
            lblMensagem.setText("Nenhum registro encontrado para os critérios selecionados.");
        } else {
            lblMensagem.setText(dados.size() + " registro(s) encontrado(s).");
        }

        ajustarAlturaTabela();
    }

    private void exibirErro(String mensagem) {
        lblMensagem.setText(mensagem);
        tabelaRelatorio.getItems().clear();
        ajustarAlturaTabela();
    }

    private void ajustarAlturaTabela() {
        int quantidadeLinhas = tabelaRelatorio.getItems().size();

        double alturaCabecalho = 44;
        double alturaLinha = 48;
        double alturaMinima = 150;
        double alturaMaxima = 500;

        double alturaCalculada = alturaCabecalho + quantidadeLinhas * alturaLinha + 2;
        double alturaFinal = Math.max(alturaMinima, Math.min(alturaCalculada, alturaMaxima));

        tabelaRelatorio.setPrefHeight(alturaFinal);
    }

    private String formatarData(LocalDate data) {
        return data != null ? data.format(formatadorData) : "";
    }

    // ---------------------------------------------------------
    // EXPORTACAO
    // ---------------------------------------------------------
    @FXML
    private void onExportarUnidades() {
        onGerarUnidades();

        if (!tabelaRelatorio.getItems().isEmpty()) {
            onExportar();
        }
    }

    @FXML
    private void onExportarMoradores() {
        onGerarMoradores();

        if (!tabelaRelatorio.getItems().isEmpty()) {
            onExportar();
        }
    }

    @FXML
    private void onExportarTaxas() {
        onGerarTaxas();

        if (!tabelaRelatorio.getItems().isEmpty()) {
            onExportar();
        }
    }

    @FXML
    private void onExportarReservas() {
        onGerarReservas();

        if (!tabelaRelatorio.getItems().isEmpty()) {
            onExportar();
        }
    }

    @FXML
    private void onExportarOcorrencias() {
        onGerarOcorrencias();

        if (!tabelaRelatorio.getItems().isEmpty()) {
            onExportar();
        }
    }

    @FXML
    private void onExportarManutencoes() {
        onGerarManutencoes();

        if (!tabelaRelatorio.getItems().isEmpty()) {
            onExportar();
        }
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
        fileChooser.setInitialFileName(obterNomeArquivoRelatorio());

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

    private String obterNomeArquivoRelatorio() {
        return switch (lblTituloRelatorio.getText()) {
            case "Relatório de Unidades" -> "relatorioUnidades.xlsx";
            case "Relatório de Moradores" -> "relatorioMoradores.xlsx";
            case "Relatório de Taxas Condominiais" -> "relatorioTaxas.xlsx";
            case "Relatório de Reservas" -> "relatorioReservas.xlsx";
            case "Relatório de Ocorrências" -> "relatorioOcorrencias.xlsx";
            case "Relatório de Manutenções" -> "relatorioManutencoes.xlsx";
            default -> "relatorio.xlsx";
        };
    }
}

