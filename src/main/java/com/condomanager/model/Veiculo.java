package com.condomanager.model;

/**
 * Representa um veiculo vinculado a uma unidade do condominio.
 */
public class Veiculo {
    private int id;
    private int idUnidade;
    private String placa;
    private String modelo;
    private String cor;
    private String numeroVaga; // Pode ser nulo se nao houver vaga fixa

    public Veiculo() {}

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIdUnidade() { return idUnidade; }
    public void setIdUnidade(int idUnidade) { this.idUnidade = idUnidade; }
    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public String getCor() { return cor; }
    public void setCor(String cor) { this.cor = cor; }
    public String getNumeroVaga() { return numeroVaga; }
    public void setNumeroVaga(String numeroVaga) { this.numeroVaga = numeroVaga; }
}

