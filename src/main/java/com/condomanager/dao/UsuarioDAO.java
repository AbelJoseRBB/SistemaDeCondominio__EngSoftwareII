package com.condomanager.dao;

import com.condomanager.model.Usuario;
import com.condomanager.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO para operacoes de banco de dados relacionadas a Usuario.
 */
public class UsuarioDAO {

    /**
     * Busca um usuario pelo login no banco de dados.
     *
     * @param login login do usuario
     * @return Usuario encontrado, ou null se nao existir
     */
    public Usuario buscarPorLogin(String login) {
        String sql = "SELECT id, nome, login, email, senha_hash, perfil FROM usuario WHERE login = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, login);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Usuario(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getString("login"),
                        rs.getString("email"),
                        rs.getString("senha_hash"),
                        rs.getString("perfil")
                    );
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro ao buscar usuario por login: " + e.getMessage());
        }

        return null;
    }

    /**
     * Busca um usuario pelo e-mail no banco de dados.
     *
     * @param email e-mail do usuario
     * @return Usuario encontrado, ou null se nao existir
     */
    public Usuario buscarPorEmail(String email) {
        String sql = "SELECT id, nome, login, email, senha_hash, perfil FROM usuario WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Usuario(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getString("login"),
                        rs.getString("email"),
                        rs.getString("senha_hash"),
                        rs.getString("perfil")
                    );
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro ao buscar usuario por email: " + e.getMessage());
        }

        return null;
    }

    /**
     * Verifica se ja existe um usuario cadastrado com o e-mail informado.
     */
    public boolean existeEmail(String email) {
        return buscarPorEmail(email) != null;
    }

    /**
     * Verifica se ja existe um usuario cadastrado com o login informado.
     */
    public boolean existeLogin(String login) {
        return buscarPorLogin(login) != null;
    }

    /**
     * Insere um novo usuario no banco de dados.
     *
     * @param usuario usuario a ser salvo (senha_hash ja deve estar hasheada com BCrypt)
     */
    public void salvar(Usuario usuario) {
        String sql = "INSERT INTO usuario (nome, login, email, senha_hash, perfil) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getLogin());
            stmt.setString(3, usuario.getEmail());
            stmt.setString(4, usuario.getSenhaHash());
            stmt.setString(5, usuario.getPerfil());
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erro ao salvar usuario: " + e.getMessage());
            throw new RuntimeException("Erro ao salvar usuario no banco de dados: " + e.getMessage(), e);
        }
    }

    /**
     * Atualiza a senha de um usuario no banco de dados.
     *
     * @param id           ID do usuario
     * @param novaSenhaHash novo hash BCrypt da senha
     */
    public void atualizarSenha(int id, String novaSenhaHash) {
        String sql = "UPDATE usuario SET senha_hash = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, novaSenhaHash);
            stmt.setInt(2, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erro ao atualizar senha: " + e.getMessage());
        }
    }
}
