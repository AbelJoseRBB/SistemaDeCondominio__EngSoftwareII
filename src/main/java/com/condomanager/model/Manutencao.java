package com.condomanager.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Representa uma solicitacao ou servico de manutencao no condominio.
 */
public class Manutencao {
    private int id;
    private Integer idUnidade; // Pode ser nulo se for area comum
    private String descricao;
    private String local;
    private String responsavel;
    private LocalDate dataSolicitacao;
    private LocalDate dataConclusao;
    private String situacao; // "SOLICITADA", "EM_ANDAMENTO", "CONCLUIDA", "CANCELADA"
    private BigDecimal custo;
    private String observacoes;

    public Manutencao() {}

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Integer getIdUnidade() { return idUnidade; }
    public void setIdUnidade(Integer idUnidade) { this.idUnidade = idUnidade; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getLocal() { return local; }
    public void setLocal(String local) { this.local = local; }
    public String getResponsavel() { return responsavel; }
    public void setResponsavel(String responsavel) { this.responsavel = responsavel; }
    public LocalDate getDataSolicitacao() { return dataSolicitacao; }
    public void setDataSolicitacao(LocalDate dataSolicitacao) { this.dataSolicitacao = dataSolicitacao; }
    public LocalDate getDataConclusao() { return dataConclusao; }
    public void setDataConclusao(LocalDate dataConclusao) { this.dataConclusao = dataConclusao; }
    public String getSituacao() { return situacao; }
    public void setSituacao(String situacao) { this.situacao = situacao; }
    public BigDecimal getCusto() { return custo; }
    public void setCusto(BigDecimal custo) { this.custo = custo; }
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
}

