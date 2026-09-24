package com.condomanager.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Representa uma taxa condominial associada a uma unidade.
 */
public class Taxa {
    private int id;
    private int idUnidade;
    private String descricao;
    private BigDecimal valor;
    private LocalDate vencimento;
    private String situacao; // "PENDENTE", "PAGO", "ATRASADO"
    private String unidadeTexto; // Adicionado para exibicao na tabela

    public Taxa() {}

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIdUnidade() { return idUnidade; }
    public void setIdUnidade(int idUnidade) { this.idUnidade = idUnidade; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public LocalDate getVencimento() { return vencimento; }
    public void setVencimento(LocalDate vencimento) { this.vencimento = vencimento; }
    public String getSituacao() { return situacao; }
    public void setSituacao(String situacao) { this.situacao = situacao; }

    public String getUnidadeTexto() { return unidadeTexto; }
    public void setUnidadeTexto(String unidadeTexto) { this.unidadeTexto = unidadeTexto; }
}

