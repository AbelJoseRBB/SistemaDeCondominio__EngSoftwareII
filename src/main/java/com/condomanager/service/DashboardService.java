package com.condomanager.service;

import com.condomanager.dao.DashboardDAO;
import com.condomanager.model.DashboardResumo;
import com.condomanager.model.Ocorrencia;
import com.condomanager.model.Reserva;

import java.util.List;

/**
 * Servico responsavel pela regra de negocio do painel central (Dashboard).
 * Centraliza a recuperacao do resumo das operacoes do condominio.
 */
public class DashboardService {

    private final DashboardDAO dashboardDAO;

    public DashboardService() {
        this.dashboardDAO = new DashboardDAO();
    }

    public DashboardService(DashboardDAO dashboardDAO) {
        this.dashboardDAO = dashboardDAO;
    }

    /**
     * Retorna os dados consolidados do resumo das operacoes.
     * Caso o banco de dados esteja indisponivel, retorna um objeto com zeros.
     */
    public DashboardResumo carregarResumo() {
        try {
            DashboardResumo resumo = dashboardDAO.obterResumoOperacoes();
            return resumo != null ? resumo : new DashboardResumo(0, 0, 0, 0, 0, 0);
        } catch (Exception e) {
            System.err.println("Falha ao carregar resumo de operacoes: " + e.getMessage());
            return new DashboardResumo(0, 0, 0, 0, 0, 0);
        }
    }

    /**
     * Retorna as reservas mais recentes para exibicao na tabela do Dashboard.
     */
    public List<Reserva> listarReservasRecentes(int limite) {
        try {
            return dashboardDAO.listarReservasRecentes(limite > 0 ? limite : 8);
        } catch (Exception e) {
            System.err.println("Falha ao listar reservas recentes: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Retorna as ocorrencias mais recentes para exibicao na tabela do Dashboard.
     */
    public List<Ocorrencia> listarOcorrenciasRecentes(int limite) {
        try {
            return dashboardDAO.listarOcorrenciasRecentes(limite > 0 ? limite : 8);
        } catch (Exception e) {
            System.err.println("Falha ao listar ocorrencias recentes: " + e.getMessage());
            return List.of();
        }
    }
}