package com.condomanager.dao;

import com.condomanager.model.Ocorrencia;
import com.condomanager.util.DBConnection;

import java.sql.*;
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

        String sql =
                "INSERT INTO ocorrencia "
                        + "(id_unidade, local, titulo, descricao, categoria, situacao, data_abertura) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        sql,
                        Statement.RETURN_GENERATED_KEYS
                )
        ) {

            if (ocorrencia.getIdUnidade() != null) {
                stmt.setInt(
                        1,
                        ocorrencia.getIdUnidade()
                );
            } else {
                stmt.setNull(
                        1,
                        Types.INTEGER
                );
            }

            stmt.setString(
                    2,
                    ocorrencia.getLocal()
            );

            stmt.setString(
                    3,
                    ocorrencia.getTitulo()
            );

            stmt.setString(
                    4,
                    ocorrencia.getDescricao()
            );

            stmt.setString(
                    5,
                    ocorrencia.getCategoria()
            );

            stmt.setString(
                    6,
                    ocorrencia.getSituacao()
            );

            stmt.setTimestamp(
                    7,
                    Timestamp.valueOf(
                            ocorrencia.getDataAbertura()
                    )
            );

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {

                if (keys.next()) {
                    ocorrencia.setId(
                            keys.getInt(1)
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Erro ao salvar ocorrencia: "
                            + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Atualiza uma ocorrencia existente.
     */
    public void atualizar(Ocorrencia ocorrencia) {

        String sql =
                "UPDATE ocorrencia "
                        + "SET id_unidade=?, local=?, titulo=?, descricao=?, categoria=?, "
                        + "situacao=?, data_encerramento=?, resposta=? "
                        + "WHERE id=?";

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            if (ocorrencia.getIdUnidade() != null) {

                stmt.setInt(
                        1,
                        ocorrencia.getIdUnidade()
                );

            } else {

                stmt.setNull(
                        1,
                        Types.INTEGER
                );
            }

            stmt.setString(
                    2,
                    ocorrencia.getLocal()
            );

            stmt.setString(
                    3,
                    ocorrencia.getTitulo()
            );

            stmt.setString(
                    4,
                    ocorrencia.getDescricao()
            );

            stmt.setString(
                    5,
                    ocorrencia.getCategoria()
            );

            stmt.setString(
                    6,
                    ocorrencia.getSituacao()
            );

            if (ocorrencia.getDataEncerramento() != null) {

                stmt.setTimestamp(
                        7,
                        Timestamp.valueOf(
                                ocorrencia.getDataEncerramento()
                        )
                );

            } else {

                stmt.setNull(
                        7,
                        Types.TIMESTAMP
                );
            }

            stmt.setString(
                    8,
                    ocorrencia.getResposta()
            );

            stmt.setInt(
                    9,
                    ocorrencia.getId()
            );

            stmt.executeUpdate();

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Erro ao atualizar ocorrencia: "
                            + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Remove uma ocorrencia pelo id.
     */
    public void deletar(int id) {

        String sql =
                "DELETE FROM ocorrencia WHERE id=?";

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Erro ao deletar ocorrencia: "
                            + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Busca uma ocorrencia pelo id.
     */
    public Ocorrencia buscarPorId(int id) {

        String sql =
                "SELECT * "
                        + "FROM ocorrencia "
                        + "WHERE id = ?";

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    return mapearOcorrencia(rs);
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Erro ao buscar ocorrencia por id: "
                            + e.getMessage(),
                    e
            );
        }

        return null;
    }

    /**
     * Retorna todas as ocorrencias da mais recente para a mais antiga.
     */
    public List<Ocorrencia> listarTodas() {

        String sql =
                "SELECT * "
                        + "FROM ocorrencia "
                        + "ORDER BY data_abertura DESC";

        return executarListagem(sql);
    }

    /**
     * Retorna ocorrencias filtradas pela situacao.
     */
    public List<Ocorrencia> listarPorSituacao(
            String situacao
    ) {

        String sql =
                "SELECT * "
                        + "FROM ocorrencia "
                        + "WHERE situacao = ? "
                        + "ORDER BY data_abertura DESC";

        List<Ocorrencia> lista =
                new ArrayList<>();

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setString(
                    1,
                    situacao
            );

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    lista.add(
                            mapearOcorrencia(rs)
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Erro ao listar ocorrencias por situacao: "
                            + e.getMessage(),
                    e
            );
        }

        return lista;
    }

    // ------------------------------------------------------------------
    // Helpers privados
    // ------------------------------------------------------------------

    /**
     * Executa uma query de listagem sem parametros.
     */
    private List<Ocorrencia> executarListagem(
            String sql
    ) {

        List<Ocorrencia> lista =
                new ArrayList<>();

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()
        ) {

            while (rs.next()) {
                lista.add(
                        mapearOcorrencia(rs)
                );
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Erro ao listar ocorrencias: "
                            + e.getMessage(),
                    e
            );
        }

        return lista;
    }

    /**
     * Converte uma linha do ResultSet em uma Ocorrencia.
     */
    private Ocorrencia mapearOcorrencia(
            ResultSet rs
    ) throws SQLException {

        Ocorrencia o = new Ocorrencia();

        o.setId(
                rs.getInt("id")
        );

        int idUnidade =
                rs.getInt("id_unidade");

        o.setIdUnidade(
                rs.wasNull()
                        ? null
                        : idUnidade
        );

        o.setLocal(
                rs.getString("local")
        );

        o.setTitulo(
                rs.getString("titulo")
        );

        o.setDescricao(
                rs.getString("descricao")
        );

        o.setCategoria(
                rs.getString("categoria")
        );

        o.setSituacao(
                rs.getString("situacao")
        );

        Timestamp tsAbertura =
                rs.getTimestamp("data_abertura");

        if (tsAbertura != null) {
            o.setDataAbertura(
                    tsAbertura.toLocalDateTime()
            );
        }

        Timestamp tsEncerramento =
                rs.getTimestamp("data_encerramento");

        if (tsEncerramento != null) {
            o.setDataEncerramento(
                    tsEncerramento.toLocalDateTime()
            );
        }

        o.setResposta(
                rs.getString("resposta")
        );

        return o;
    }
}