package com.condomanager.model;

/**
 * Representa um usuario do sistema (administrador ou operador).
 * Perfis possiveis: ADMIN, OPERADOR
 */
public class Usuario {
    private int id;
    private String nome;
    private String login;
    private String senhaHash; // Armazenada como hash BCrypt
    private String perfil;    // "ADMIN" ou "OPERADOR"

    public Usuario() {}

    public Usuario(int id, String nome, String login, String senhaHash, String perfil) {
        this.id = id;
        this.nome = nome;
        this.login = login;
        this.senhaHash = senhaHash;
        this.perfil = perfil;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getSenhaHash() { return senhaHash; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }
    public String getPerfil() { return perfil; }
    public void setPerfil(String perfil) { this.perfil = perfil; }
}

