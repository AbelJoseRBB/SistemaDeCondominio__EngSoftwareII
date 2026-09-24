package com.condomanager.dao;

import com.condomanager.model.Morador;
import com.condomanager.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para operacoes de banco de dados relacionadas a Morador.
 */
public class MoradorDAO {

    public void salvar(Morador morador) {
        String sql = "INSERT INTO morador (id_unidade, nome, cpf, telefone, email, tipo, situacao) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, morador.getIdUnidade());
            stmt.setString(2, morador.getNome());
            stmt.setString(3, morador.getCpf());
            stmt.setString(4, morador.getTelefone());
            stmt.setString(5, morador.getEmail());
            stmt.setString(6, morador.getTipo());
            stmt.setString(7, morador.getSituacao() != null ? morador.getSituacao() : "ATIVO");
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    morador.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar morador: " + e.getMessage(), e);
        }
    }

    public void atualizar(Morador morador) {
        String sql = "UPDATE morador SET id_unidade=?, nome=?, cpf=?, telefone=?, email=?, tipo=?, situacao=? WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, morador.getIdUnidade());
            stmt.setString(2, morador.getNome());
            stmt.setString(3, morador.getCpf());
            stmt.setString(4, morador.getTelefone());
            stmt.setString(5, morador.getEmail());
            stmt.setString(6, morador.getTipo());
            stmt.setString(7, morador.getSituacao());
            stmt.setInt(8, morador.getId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar morador: " + e.getMessage(), e);
        }
    }

    /**
     * Inativa um morador (exclusao logica) para manter o historico.
     */
    public void inativar(int id) {
        String sql = "UPDATE morador SET situacao='INATIVO' WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inativar morador: " + e.getMessage(), e);
        }
    }

    public void deletar(int id) {
        String sql = "DELETE FROM morador WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao deletar morador: " + e.getMessage(), e);
        }
    }

    public Morador buscarPorId(int id) {
        String sql = "SELECT * FROM morador WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearMorador(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar morador: " + e.getMessage(), e);
        }
        return null;
    }

    public List<Morador> listarPorUnidade(int idUnidade) {
        String sql = "SELECT * FROM morador WHERE id_unidade=? ORDER BY nome";
        List<Morador> lista = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idUnidade);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearMorador(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar moradores por unidade: " + e.getMessage(), e);
        }
        return lista;
    }

    public List<Morador> listarTodos() {
        String sql = "SELECT * FROM morador ORDER BY nome";
        List<Morador> lista = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
             
            while (rs.next()) {
                lista.add(mapearMorador(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar todos os moradores: " + e.getMessage(), e);
        }
        return lista;
    }

    public boolean existeCpf(String cpf, int idIgnorar) {
        String sql = "SELECT COUNT(*) FROM morador WHERE cpf=? AND id != ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            stmt.setString(1, cpf);
            stmt.setInt(2, idIgnorar);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar duplicidade de CPF: " + e.getMessage(), e);
        }
        return false;
    }

    private Morador mapearMorador(ResultSet rs) throws SQLException {
        Morador m = new Morador();
        m.setId(rs.getInt("id"));
        m.setIdUnidade(rs.getInt("id_unidade"));
        m.setNome(rs.getString("nome"));
        m.setCpf(rs.getString("cpf"));
        m.setTelefone(rs.getString("telefone"));
        m.setEmail(rs.getString("email"));
        m.setTipo(rs.getString("tipo"));
        m.setSituacao(rs.getString("situacao"));
        return m;
    }
}

