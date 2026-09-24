package com.condomanager.service;

import com.condomanager.dao.*;
import com.condomanager.model.*;

import java.time.LocalDate;
import java.util.List;

public class RelatorioService {

    private final UnidadeDAO unidadeDAO;
    private final MoradorDAO moradorDAO;
    private final TaxaDAO taxaDAO;
    private final ReservaDAO reservaDAO;
    private final OcorrenciaDAO ocorrenciaDAO;
    private final ManutencaoDAO manutencaoDAO;

    public RelatorioService() {
        unidadeDAO = new UnidadeDAO();
        moradorDAO = new MoradorDAO();
        taxaDAO = new TaxaDAO();
        reservaDAO = new ReservaDAO();
        ocorrenciaDAO = new OcorrenciaDAO();
        manutencaoDAO = new ManutencaoDAO();
    }

    public List<Unidade> gerarRelatorioUnidades() {
        return unidadeDAO.listarTodas();
    }

    public List<Morador> gerarRelatorioMoradores() {
        return moradorDAO.listarTodos();
    }

    public List<Taxa> gerarRelatorioTaxas(LocalDate dataInicial, LocalDate dataFinal) {
        validarPeriodo(dataInicial, dataFinal);

        return taxaDAO.listarTodos().stream()
                .filter(taxa -> estaDentroDoPeriodo(taxa.getVencimento(), dataInicial, dataFinal))
                .toList();
    }

    public List<Reserva> gerarRelatorioReservas(LocalDate dataInicial, LocalDate dataFinal) {
        validarPeriodo(dataInicial, dataFinal);

        return reservaDAO.listarTodas().stream()
                .filter(reserva -> estaDentroDoPeriodo(reserva.getData(), dataInicial, dataFinal))
                .toList();
    }

    public List<Ocorrencia> gerarRelatorioOcorrencias(LocalDate dataInicial, LocalDate dataFinal) {
        validarPeriodo(dataInicial, dataFinal);

        return ocorrenciaDAO.listarTodas().stream()
                .filter(ocorrencia -> ocorrencia.getDataAbertura() != null
                        && estaDentroDoPeriodo(ocorrencia.getDataAbertura().toLocalDate(), dataInicial, dataFinal))
                .toList();
    }

    public List<Manutencao> gerarRelatorioManutencoes(LocalDate dataInicial, LocalDate dataFinal) {
        validarPeriodo(dataInicial, dataFinal);

        return manutencaoDAO.listarTodos().stream()
                .filter(manutencao -> estaDentroDoPeriodo(manutencao.getDataSolicitacao(), dataInicial, dataFinal))
                .toList();
    }

    private void validarPeriodo(LocalDate dataInicial, LocalDate dataFinal) {
        if (dataInicial != null && dataFinal != null && dataInicial.isAfter(dataFinal)) {
            throw new IllegalArgumentException("A data inicial não pode ser posterior à data final.");
        }
    }

    private boolean estaDentroDoPeriodo(LocalDate data, LocalDate dataInicial, LocalDate dataFinal) {
        if (data == null) {
            return false;
        }

        if (dataInicial != null && data.isBefore(dataInicial)) {
            return false;
        }

        if (dataFinal != null && data.isAfter(dataFinal)) {
            return false;
        }

        return true;
    }
}