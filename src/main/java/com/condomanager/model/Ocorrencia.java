package com.condomanager.model;

import java.time.LocalDateTime;

/**
 * Representa uma ocorrencia, reclamacao ou observacao registrada no condominio.
 */
public class Ocorrencia {
    private int id;
    private Integer idUnidade; // Pode ser nulo se for area comum
    private String titulo;
    private String descricao;
    private String categoria; // "RECLAMACAO", "INFORMACAO", "MANUTENCAO", "SEGURANCA"
    private String situacao;  // "ABERTA", "EM_ANDAMENTO", "ENCERRADA"
    private LocalDateTime dataAbertura;
    private LocalDateTime dataEncerramento;
    private String resposta;

    public Ocorrencia() {}

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Integer getIdUnidade() { return idUnidade; }
    public void setIdUnidade(Integer idUnidade) { this.idUnidade = idUnidade; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public String getSituacao() { return situacao; }
    public void setSituacao(String situacao) { this.situacao = situacao; }
    public LocalDateTime getDataAbertura() { return dataAbertura; }
    public void setDataAbertura(LocalDateTime dataAbertura) { this.dataAbertura = dataAbertura; }
    public LocalDateTime getDataEncerramento() { return dataEncerramento; }
    public void setDataEncerramento(LocalDateTime dataEncerramento) { this.dataEncerramento = dataEncerramento; }
    public String getResposta() { return resposta; }
    public void setResposta(String resposta) { this.resposta = resposta; }
}

