package com.condomanager.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Representa uma reserva de area comum do condominio.
 */
public class Reserva {
    private int id;
    private int idUnidade;
    private String areaComum; // Ex: "Salao de Festas", "Churrasqueira", "Quadra"
    private LocalDate data;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private String situacao; // "CONFIRMADA", "CANCELADA", "PENDENTE"
    private String observacoes;

    /**
     * Campo transiente (nao persiste no banco).
     * Preenchido pelo DAO via JOIN com a tabela unidade.
     * Exibe "Bloco X – NNN" na TableView de reservas.
     */
    private String unidadeFormatada;

    public Reserva() {}

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIdUnidade() { return idUnidade; }
    public void setIdUnidade(int idUnidade) { this.idUnidade = idUnidade; }
    public String getAreaComum() { return areaComum; }
    public void setAreaComum(String areaComum) { this.areaComum = areaComum; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public LocalTime getHoraFim() { return horaFim; }
    public void setHoraFim(LocalTime horaFim) { this.horaFim = horaFim; }
    public String getSituacao() { return situacao; }
    public void setSituacao(String situacao) { this.situacao = situacao; }
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
    public String getUnidadeFormatada() { return unidadeFormatada; }
    public void setUnidadeFormatada(String unidadeFormatada) { this.unidadeFormatada = unidadeFormatada; }
}

