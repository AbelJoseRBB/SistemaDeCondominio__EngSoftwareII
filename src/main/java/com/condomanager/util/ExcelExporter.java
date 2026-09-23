package com.condomanager.util;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExcelExporter {

    public static void exportar(
            TableView<Object> tabela,
            File arquivo) throws IOException {

        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet planilha = workbook.createSheet("Relatório");

            criarCabecalho(workbook, planilha, tabela);
            preencherDados(planilha, tabela);
            ajustarColunas(planilha, tabela);

            try (FileOutputStream output =
                         new FileOutputStream(arquivo)) {

                workbook.write(output);
            }
        }
    }

    private static void criarCabecalho(
            Workbook workbook,
            Sheet planilha,
            TableView<Object> tabela) {

        Row cabecalho = planilha.createRow(0);

        CellStyle estiloCabecalho =
                workbook.createCellStyle();

        Font fonte = workbook.createFont();
        fonte.setBold(true);

        estiloCabecalho.setFont(fonte);

        for (int coluna = 0;
             coluna < tabela.getColumns().size();
             coluna++) {

            Cell celula = cabecalho.createCell(coluna);

            celula.setCellValue(
                    tabela.getColumns()
                            .get(coluna)
                            .getText()
            );

            celula.setCellStyle(estiloCabecalho);
        }
    }

    private static void preencherDados(
            Sheet planilha,
            TableView<Object> tabela) {

        for (int linha = 0;
             linha < tabela.getItems().size();
             linha++) {

            Row linhaExcel =
                    planilha.createRow(linha + 1);

            Object item = tabela.getItems().get(linha);

            for (int coluna = 0;
                 coluna < tabela.getColumns().size();
                 coluna++) {

                TableColumn<Object, ?> colunaTabela =
                        tabela.getColumns().get(coluna);

                Object valor =
                        colunaTabela.getCellObservableValue(item)
                                .getValue();

                Cell celula =
                        linhaExcel.createCell(coluna);

                celula.setCellValue(
                        valor != null
                                ? valor.toString()
                                : ""
                );
            }
        }
    }

    private static void ajustarColunas(
            Sheet planilha,
            TableView<Object> tabela) {

        for (int coluna = 0;
             coluna < tabela.getColumns().size();
             coluna++) {

            planilha.autoSizeColumn(coluna);
        }
    }

    private ExcelExporter() {
        // Impede instanciacao da classe utilitaria.
    }
}