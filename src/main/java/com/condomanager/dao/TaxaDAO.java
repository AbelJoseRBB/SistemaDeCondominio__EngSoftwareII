package com.condomanager.dao;

import com.condomanager.model.Taxa;
import com.condomanager.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaxaDAO {

    public void salvar(Taxa taxa) {
        String sql = "INSERT INTO taxa (id_unidade, descricao, valor, vencimento, situacao) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, taxa.getIdUnidade());
            stmt.setString(2, taxa.getDescricao());
            stmt.setBigDecimal(3, taxa.getValor());
            stmt.setDate(4, java.sql.Date.valueOf(taxa.getVencimento()));
            stmt.setString(5, taxa.getSituacao());
            stmt.executeUpdate();
            
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    taxa.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar taxa: " + e.getMessage(), e);
        }
    }

    public void atualizar(Taxa taxa) {
        String sql = "UPDATE taxa SET id_unidade=?, descricao=?, valor=?, vencimento=?, situacao=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, taxa.getIdUnidade());
            stmt.setString(2, taxa.getDescricao());
            stmt.setBigDecimal(3, taxa.getValor());
            stmt.setDate(4, java.sql.Date.valueOf(taxa.getVencimento()));
            stmt.setString(5, taxa.getSituacao());
            stmt.setInt(6, taxa.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar taxa: " + e.getMessage(), e);
        }
    }

    public void deletar(int id) {
        String sql = "DELETE FROM taxa WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao deletar taxa: " + e.getMessage(), e);
        }
    }

    public Taxa buscarPorId(int id) {
        String sql = "SELECT * FROM taxa WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearTaxa(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar taxa por id: " + e.getMessage(), e);
        }
        return null;
    }

    public List<Taxa> listarTodos() {
        String sql = "SELECT t.*, u.bloco, u.numero FROM taxa t JOIN unidade u ON t.id_unidade = u.id ORDER BY t.vencimento DESC";
        List<Taxa> lista = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Taxa t = mapearTaxa(rs);
                t.setUnidadeTexto(rs.getString("bloco") + "-" + rs.getString("numero"));
                lista.add(t);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar taxas: " + e.getMessage(), e);
        }
        return lista;
    }

    private Taxa mapearTaxa(ResultSet rs) throws SQLException {
        Taxa t = new Taxa();
        t.setId(rs.getInt("id"));
        t.setIdUnidade(rs.getInt("id_unidade"));
        t.setDescricao(rs.getString("descricao"));
        t.setValor(rs.getBigDecimal("valor"));
        t.setVencimento(rs.getDate("vencimento").toLocalDate());
        t.setSituacao(rs.getString("situacao"));
        return t;
    }
}
