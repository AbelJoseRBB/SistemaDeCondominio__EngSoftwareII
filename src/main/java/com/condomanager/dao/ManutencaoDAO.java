package com.condomanager.dao;

import com.condomanager.model.Manutencao;
import com.condomanager.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** DAO para Manutencao */
public class ManutencaoDAO {

    public void salvar(Manutencao obj) {
        String sql = "INSERT INTO manutencao (id_unidade, descricao, local, responsavel, data_solicitacao, data_conclusao, situacao, custo, observacoes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            if (obj.getIdUnidade() != null) {
                stmt.setInt(1, obj.getIdUnidade());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setString(2, obj.getDescricao());
            stmt.setString(3, obj.getLocal());
            stmt.setString(4, obj.getResponsavel());
            stmt.setDate(5, obj.getDataSolicitacao() != null ? Date.valueOf(obj.getDataSolicitacao()) : null);
            stmt.setDate(6, obj.getDataConclusao() != null ? Date.valueOf(obj.getDataConclusao()) : null);
            stmt.setString(7, obj.getSituacao() != null ? obj.getSituacao() : "SOLICITADA");
            stmt.setBigDecimal(8, obj.getCusto());
            stmt.setString(9, obj.getObservacoes());
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao salvar manutencao: " + e.getMessage());
        }
    }

    public void atualizar(Manutencao obj) {
        String sql = "UPDATE manutencao SET id_unidade=?, descricao=?, local=?, responsavel=?, data_solicitacao=?, data_conclusao=?, situacao=?, custo=?, observacoes=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            if (obj.getIdUnidade() != null) {
                stmt.setInt(1, obj.getIdUnidade());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setString(2, obj.getDescricao());
            stmt.setString(3, obj.getLocal());
            stmt.setString(4, obj.getResponsavel());
            stmt.setDate(5, obj.getDataSolicitacao() != null ? Date.valueOf(obj.getDataSolicitacao()) : null);
            stmt.setDate(6, obj.getDataConclusao() != null ? Date.valueOf(obj.getDataConclusao()) : null);
            stmt.setString(7, obj.getSituacao());
            stmt.setBigDecimal(8, obj.getCusto());
            stmt.setString(9, obj.getObservacoes());
            stmt.setInt(10, obj.getId());
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao atualizar manutencao: " + e.getMessage());
        }
    }

    public void deletar(int id) {
        String sql = "DELETE FROM manutencao WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao deletar manutencao: " + e.getMessage());
        }
    }

    public Manutencao buscarPorId(int id) {
        String sql = "SELECT * FROM manutencao WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao buscar manutencao por id: " + e.getMessage());
        }
        return null;
    }

    public List<Manutencao> listarTodos() {
        return listarComFiltros(null, null);
    }

    public List<Manutencao> listarComFiltros(String busca, String status) {
        List<Manutencao> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM manutencao WHERE 1=1 ");
        
        if (busca != null && !busca.trim().isEmpty()) {
            sql.append(" AND (descricao LIKE ? OR local LIKE ? OR responsavel LIKE ?) ");
        }
        if (status != null && !status.trim().isEmpty() && !status.equals("Todos")) {
            sql.append(" AND situacao = ? ");
        }
        
        sql.append(" ORDER BY data_solicitacao DESC");
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            int paramIndex = 1;
            if (busca != null && !busca.trim().isEmpty()) {
                String like = "%" + busca.trim() + "%";
                stmt.setString(paramIndex++, like);
                stmt.setString(paramIndex++, like);
                stmt.setString(paramIndex++, like);
            }
            if (status != null && !status.trim().isEmpty() && !status.equals("Todos")) {
                stmt.setString(paramIndex++, status);
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao listar manutencoes com filtros: " + e.getMessage());
        }
        return lista;
    }

    private Manutencao mapearResultSet(ResultSet rs) throws SQLException {
        Manutencao m = new Manutencao();
        m.setId(rs.getInt("id"));
        int idUnidade = rs.getInt("id_unidade");
        if (!rs.wasNull()) {
            m.setIdUnidade(idUnidade);
        }
        m.setDescricao(rs.getString("descricao"));
        m.setLocal(rs.getString("local"));
        m.setResponsavel(rs.getString("responsavel"));
        
        Date dataSol = rs.getDate("data_solicitacao");
        if (dataSol != null) m.setDataSolicitacao(dataSol.toLocalDate());
        
        Date dataConc = rs.getDate("data_conclusao");
        if (dataConc != null) m.setDataConclusao(dataConc.toLocalDate());
        
        m.setSituacao(rs.getString("situacao"));
        m.setCusto(rs.getBigDecimal("custo"));
        m.setObservacoes(rs.getString("observacoes"));
        return m;
    }
}


