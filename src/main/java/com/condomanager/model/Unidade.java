package com.condomanager.model;

/**
 * Representa uma unidade (apartamento/casa) do condominio.
 */
public class Unidade {
    private int id;
    private String bloco;
    private String numero;
    private String proprietario;
    private String situacao; // "OCUPADO", "DESOCUPADO", "VENDA", "ALUGUEL"
    private String telefoneContato;
    private String emailContato;

    public Unidade() {}

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getBloco() { return bloco; }
    public void setBloco(String bloco) { this.bloco = bloco; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public String getProprietario() { return proprietario; }
    public void setProprietario(String proprietario) { this.proprietario = proprietario; }
    public String getSituacao() { return situacao; }
    public void setSituacao(String situacao) { this.situacao = situacao; }
    public String getTelefoneContato() { return telefoneContato; }
    public void setTelefoneContato(String telefoneContato) { this.telefoneContato = telefoneContato; }
    public String getEmailContato() { return emailContato; }
    public void setEmailContato(String emailContato) { this.emailContato = emailContato; }

    @Override
    public String toString() {
        return "Bloco " + bloco + " - Unidade " + numero;
    }
}

