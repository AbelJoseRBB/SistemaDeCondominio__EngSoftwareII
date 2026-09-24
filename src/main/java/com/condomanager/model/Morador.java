package com.condomanager.model;

/**
 * Representa um morador vinculado a uma unidade do condominio.
 */
public class Morador {
    private int id;
    private int idUnidade;
    private String nome;
    private String cpf;
    private String telefone;
    private String email;
    private String tipo; // "PROPRIETARIO", "INQUILINO", "DEPENDENTE"

    public Morador() {}

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIdUnidade() { return idUnidade; }
    public void setIdUnidade(int idUnidade) { this.idUnidade = idUnidade; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}

