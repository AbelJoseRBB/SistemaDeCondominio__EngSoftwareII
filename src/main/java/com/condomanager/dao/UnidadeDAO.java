package com.condomanager.dao;

import com.condomanager.model.Unidade;
import com.condomanager.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para operacoes de banco de dados relacionadas a Unidade.
 * Usa JDBC para executar SQL contra o banco condominio_db.
 */
public class UnidadeDAO {

    /**
     * Insere uma nova unidade no banco de dados.
     * O id gerado automaticamente e atribuido de volta ao objeto.
     */
    public void salvar(Unidade unidade) {
        String sql = "INSERT INTO unidade (bloco, numero, proprietario, situacao, telefone_contato, email_contato) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, unidade.getBloco());
            stmt.setString(2, unidade.getNumero());
            stmt.setString(3, unidade.getProprietario());
            stmt.setString(4, unidade.getSituacao());
            stmt.setString(5, unidade.getTelefoneContato());
            stmt.setString(6, unidade.getEmailContato());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    unidade.setId(keys.getInt(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar unidade: " + e.getMessage(), e);
        }
    }

    /**
     * Atualiza os dados de uma unidade existente pelo seu id.
     */
    public void atualizar(Unidade unidade) {
        String sql = "UPDATE unidade SET bloco=?, numero=?, proprietario=?, situacao=?, "
                   + "telefone_contato=?, email_contato=? WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, unidade.getBloco());
            stmt.setString(2, unidade.getNumero());
            stmt.setString(3, unidade.getProprietario());
            stmt.setString(4, unidade.getSituacao());
            stmt.setString(5, unidade.getTelefoneContato());
            stmt.setString(6, unidade.getEmailContato());
            stmt.setInt(7, unidade.getId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar unidade: " + e.getMessage(), e);
        }
    }

    /**
     * Remove uma unidade pelo id.
     */
    public void deletar(int id) {
        String sql = "DELETE FROM unidade WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao deletar unidade: " + e.getMessage(), e);
        }
    }

    /**
     * Busca uma unidade pelo seu id.
     *
     * @return a Unidade encontrada, ou null se nao existir.
     */
    public Unidade buscarPorId(int id) {
        String sql = "SELECT * FROM unidade WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearUnidade(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar unidade por id: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Retorna todas as unidades ordenadas por bloco e numero.
     */
    public List<Unidade> listarTodas() {
        String sql = "SELECT * FROM unidade ORDER BY bloco, numero";
        List<Unidade> lista = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearUnidade(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar unidades: " + e.getMessage(), e);
        }
        return lista;
    }

    /**
     * Verifica se ja existe uma unidade com a combinacao bloco + numero informada.
     * Usado para garantir unicidade da chave composta antes de salvar.
     *
     * @param bloco  letra/nome do bloco
     * @param numero numero da unidade
     * @param idIgnorar id a ignorar na verificacao (util para edicao); passe 0 para ignorar nenhum
     * @return true se ja existir outra unidade com esse bloco e numero
     */
    public boolean existePorBlocoNumero(String bloco, String numero, int idIgnorar) {
        String sql = "SELECT COUNT(*) FROM unidade WHERE bloco=? AND numero=? AND id != ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, bloco);
            stmt.setString(2, numero);
            stmt.setInt(3, idIgnorar);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar duplicidade de unidade: " + e.getMessage(), e);
        }
        return false;
    }

    /**
     * Mapeia uma linha do ResultSet para um objeto Unidade.
     */
    private Unidade mapearUnidade(ResultSet rs) throws SQLException {
        Unidade u = new Unidade();
        u.setId(rs.getInt("id"));
        u.setBloco(rs.getString("bloco"));
        u.setNumero(rs.getString("numero"));
        u.setProprietario(rs.getString("proprietario"));
        u.setSituacao(rs.getString("situacao"));
        u.setTelefoneContato(rs.getString("telefone_contato"));
        u.setEmailContato(rs.getString("email_contato"));
        return u;
    }
}
