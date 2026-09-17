package com.condomanager.dao;

import com.condomanager.model.Ocorrencia;
import com.condomanager.util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para operacoes de banco de dados relacionadas a Ocorrencia.
 * Utiliza JDBC com PreparedStatement seguindo o padrao do UnidadeDAO.
 */
public class OcorrenciaDAO {

    /**
     * Insere uma nova ocorrencia no banco.
     * O id gerado automaticamente e atribuido de volta ao objeto.
     */
    public void salvar(Ocorrencia ocorrencia) {
        String sql = "INSERT INTO ocorrencia "
                   + "(id_unidade, titulo, descricao, categoria, situacao, data_abertura) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (ocorrencia.getIdUnidade() != null) {
                stmt.setInt(1, ocorrencia.getIdUnidade());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setString(2, ocorrencia.getTitulo());
            stmt.setString(3, ocorrencia.getDescricao());
            stmt.setString(4, ocorrencia.getCategoria());
            stmt.setString(5, ocorrencia.getSituacao());
            stmt.setTimestamp(6, Timestamp.valueOf(ocorrencia.getDataAbertura()));
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    ocorrencia.setId(keys.getInt(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar ocorrencia: " + e.getMessage(), e);
        }
    }

    /**
     * Atualiza os dados de uma ocorrencia existente pelo seu id.
     * Inclui situacao, resposta e data de encerramento (quando encerrada).
     */
    public void atualizar(Ocorrencia ocorrencia) {
        String sql = "UPDATE ocorrencia "
                   + "SET id_unidade=?, titulo=?, descricao=?, categoria=?, "
                   + "    situacao=?, data_encerramento=?, resposta=? "
                   + "WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (ocorrencia.getIdUnidade() != null) {
                stmt.setInt(1, ocorrencia.getIdUnidade());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setString(2, ocorrencia.getTitulo());
            stmt.setString(3, ocorrencia.getDescricao());
            stmt.setString(4, ocorrencia.getCategoria());
            stmt.setString(5, ocorrencia.getSituacao());

            if (ocorrencia.getDataEncerramento() != null) {
                stmt.setTimestamp(6, Timestamp.valueOf(ocorrencia.getDataEncerramento()));
            } else {
                stmt.setNull(6, Types.TIMESTAMP);
            }

            stmt.setString(7, ocorrencia.getResposta());
            stmt.setInt(8, ocorrencia.getId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar ocorrencia: " + e.getMessage(), e);
        }
    }

    /**
     * Remove uma ocorrencia pelo id.
     */
    public void deletar(int id) {
        String sql = "DELETE FROM ocorrencia WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao deletar ocorrencia: " + e.getMessage(), e);
        }
    }

    /**
     * Busca uma ocorrencia pelo seu id, com JOIN para montar o localFormatado.
     *
     * @return a Ocorrencia encontrada, ou null se nao existir.
     */
    public Ocorrencia buscarPorId(int id) {
        String sql = "SELECT o.*, u.bloco, u.numero "
                   + "FROM ocorrencia o "
                   + "LEFT JOIN unidade u ON o.id_unidade = u.id "
                   + "WHERE o.id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearOcorrencia(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar ocorrencia por id: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Retorna todas as ocorrencias ordenadas da mais recente para a mais antiga.
     * Faz LEFT JOIN com unidade para montar o campo localFormatado.
     */
    public List<Ocorrencia> listarTodas() {
        String sql = "SELECT o.*, u.bloco, u.numero "
                   + "FROM ocorrencia o "
                   + "LEFT JOIN unidade u ON o.id_unidade = u.id "
                   + "ORDER BY o.data_abertura DESC";

        return executarListagem(sql);
    }

    /**
     * Retorna ocorrencias filtradas pela situacao informada.
     *
     * @param situacao "ABERTA", "EM_ANDAMENTO" ou "ENCERRADA"
     */
    public List<Ocorrencia> listarPorSituacao(String situacao) {
        String sql = "SELECT o.*, u.bloco, u.numero "
                   + "FROM ocorrencia o "
                   + "LEFT JOIN unidade u ON o.id_unidade = u.id "
                   + "WHERE o.situacao = ? "
                   + "ORDER BY o.data_abertura DESC";

        List<Ocorrencia> lista = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, situacao);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearOcorrencia(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar ocorrencias por situacao: " + e.getMessage(), e);
        }
        return lista;
    }

    // ------------------------------------------------------------------
    // Helpers privados
    // ------------------------------------------------------------------

    /**
     * Executa uma query de listagem sem parametros e retorna a lista mapeada.
     * Usado internamente por listarTodas().
     */
    private List<Ocorrencia> executarListagem(String sql) {
        List<Ocorrencia> lista = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearOcorrencia(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar ocorrencias: " + e.getMessage(), e);
        }
        return lista;
    }

    /**
     * Mapeia uma linha do ResultSet para um objeto Ocorrencia.
     * Monta o campo transiente localFormatado a partir do JOIN com unidade:
     *   - Se vinculada a uma unidade: "Bloco X – NNN"
     *   - Se nao vinculada (area comum): string vazia
     */
    private Ocorrencia mapearOcorrencia(ResultSet rs) throws SQLException {
        Ocorrencia o = new Ocorrencia();
        o.setId(rs.getInt("id"));

        int idUnidade = rs.getInt("id_unidade");
        o.setIdUnidade(rs.wasNull() ? null : idUnidade);

        o.setTitulo(rs.getString("titulo"));
        o.setDescricao(rs.getString("descricao"));
        o.setCategoria(rs.getString("categoria"));
        o.setSituacao(rs.getString("situacao"));

        Timestamp tsAbertura = rs.getTimestamp("data_abertura");
        if (tsAbertura != null) {
            o.setDataAbertura(tsAbertura.toLocalDateTime());
        }

        Timestamp tsEncerramento = rs.getTimestamp("data_encerramento");
        if (tsEncerramento != null) {
            o.setDataEncerramento(tsEncerramento.toLocalDateTime());
        }

        o.setResposta(rs.getString("resposta"));

        // Monta o campo transiente localFormatado a partir do JOIN
        String bloco  = rs.getString("bloco");
        String numero = rs.getString("numero");
        if (bloco != null && numero != null) {
            o.setLocalFormatado("Bloco " + bloco + " – " + numero);
        } else {
            o.setLocalFormatado("");
        }

        return o;
    }
}


