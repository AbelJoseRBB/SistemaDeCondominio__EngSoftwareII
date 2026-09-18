package com.condomanager.model;

/**
 * Representa os indicadores consolidados do resumo das operacoes
 * exibidos nos cards do Dashboard.
 */
public class DashboardResumo {

    private int totalUnidades;
    private int totalMoradores;
    private int taxasPendentes;
    private int manutencoesAbertas;
    private int reservasConfirmadas;
    private int ocorrenciasAbertas;

    public DashboardResumo() {}

    public DashboardResumo(int totalUnidades, int totalMoradores, int taxasPendentes,
                           int manutencoesAbertas, int reservasConfirmadas, int ocorrenciasAbertas) {
        this.totalUnidades = totalUnidades;
        this.totalMoradores = totalMoradores;
        this.taxasPendentes = taxasPendentes;
        this.manutencoesAbertas = manutencoesAbertas;
        this.reservasConfirmadas = reservasConfirmadas;
        this.ocorrenciasAbertas = ocorrenciasAbertas;
    }

    public int getTotalUnidades() {
        return totalUnidades;
    }

    public void setTotalUnidades(int totalUnidades) {
        this.totalUnidades = totalUnidades;
    }

    public int getTotalMoradores() {
        return totalMoradores;
    }

    public void setTotalMoradores(int totalMoradores) {
        this.totalMoradores = totalMoradores;
    }

    public int getTaxasPendentes() {
        return taxasPendentes;
    }

    public void setTaxasPendentes(int taxasPendentes) {
        this.taxasPendentes = taxasPendentes;
    }

    public int getManutencoesAbertas() {
        return manutencoesAbertas;
    }

    public void setManutencoesAbertas(int manutencoesAbertas) {
        this.manutencoesAbertas = manutencoesAbertas;
    }

    public int getReservasConfirmadas() {
        return reservasConfirmadas;
    }

    public void setReservasConfirmadas(int reservasConfirmadas) {
        this.reservasConfirmadas = reservasConfirmadas;
    }

    public int getOcorrenciasAbertas() {
        return ocorrenciasAbertas;
    }

    public void setOcorrenciasAbertas(int ocorrenciasAbertas) {
        this.ocorrenciasAbertas = ocorrenciasAbertas;
    }
}
