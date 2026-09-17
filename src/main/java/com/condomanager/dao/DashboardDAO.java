package com.condomanager.dao;

import com.condomanager.model.DashboardResumo;
import com.condomanager.model.Ocorrencia;
import com.condomanager.model.Reserva;
import com.condomanager.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO responsavel por consultas consolidadas e estatisticas de resumo
 * exibidas na interface principal (Dashboard).
 */
public class DashboardDAO {

    /**
     * Consolida todos os indicadores quantitativos em um unico DTO.
     */
    public DashboardResumo obterResumoOperacoes() {
        int unidades = executarContagem("SELECT COUNT(*) FROM unidade");
        int moradores = executarContagem("SELECT COUNT(*) FROM morador");
        int taxasPendentes = executarContagem("SELECT COUNT(*) FROM taxa WHERE situacao IN ('PENDENTE', 'ATRASADO')");
        int manutencoes = executarContagem("SELECT COUNT(*) FROM manutencao WHERE situacao IN ('SOLICITADA', 'EM_ANDAMENTO')");
        int reservasConfirmadas = executarContagem("SELECT COUNT(*) FROM reserva WHERE situacao = 'CONFIRMADA'");
        int ocorrenciasAbertas = executarContagem("SELECT COUNT(*) FROM ocorrencia WHERE situacao IN ('ABERTA', 'EM_ANDAMENTO')");

        return new DashboardResumo(
            unidades,
            moradores,
            taxasPendentes,
            manutencoes,
            reservasConfirmadas,
            ocorrenciasAbertas
        );
    }

    /**
     * Retorna as ultimas reservas cadastradas com limite de registros.
     */
    public List<Reserva> listarReservasRecentes(int limite) {
        String sql = "SELECT r.*, u.bloco, u.numero "
                   + "FROM reserva r "
                   + "JOIN unidade u ON r.id_unidade = u.id "
                   + "ORDER BY r.data DESC, r.hora_inicio DESC "
                   + "LIMIT ?";

        List<Reserva> lista = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limite);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Reserva r = new Reserva();
                    r.setId(rs.getInt("id"));
                    r.setIdUnidade(rs.getInt("id_unidade"));
                    r.setAreaComum(rs.getString("area_comum"));
                    r.setData(rs.getDate("data").toLocalDate());
                    r.setHoraInicio(rs.getTime("hora_inicio").toLocalTime());
                    r.setHoraFim(rs.getTime("hora_fim").toLocalTime());
                    r.setSituacao(rs.getString("situacao"));
                    r.setObservacoes(rs.getString("observacoes"));
                    r.setUnidadeFormatada(
                        "Bloco " + rs.getString("bloco") + " – " + rs.getString("numero")
                    );
                    lista.add(r);
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro ao listar reservas recentes para o dashboard: " + e.getMessage());
        }

        return lista;
    }

    /**
     * Retorna as ultimas ocorrencias cadastradas com limite de registros.
     */
    public List<Ocorrencia> listarOcorrenciasRecentes(int limite) {
        String sql = "SELECT o.*, u.bloco, u.numero "
                   + "FROM ocorrencia o "
                   + "LEFT JOIN unidade u ON o.id_unidade = u.id "
                   + "ORDER BY o.data_abertura DESC "
                   + "LIMIT ?";

        List<Ocorrencia> lista = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limite);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Ocorrencia o = new Ocorrencia();
                    o.setId(rs.getInt("id"));

                    int idUnidade = rs.getInt("id_unidade");
                    if (!rs.wasNull()) {
                        o.setIdUnidade(idUnidade);
                    }

                    o.setTitulo(rs.getString("titulo"));
                    o.setDescricao(rs.getString("descricao"));
                    o.setCategoria(rs.getString("categoria"));
                    o.setSituacao(rs.getString("situacao"));

                    java.sql.Timestamp dataAbertura = rs.getTimestamp("data_abertura");
                    if (dataAbertura != null) {
                        o.setDataAbertura(dataAbertura.toLocalDateTime());
                    }

                    java.sql.Timestamp dataEncerramento = rs.getTimestamp("data_encerramento");
                    if (dataEncerramento != null) {
                        o.setDataEncerramento(dataEncerramento.toLocalDateTime());
                    }

                    o.setResposta(rs.getString("resposta"));

                    String bloco = rs.getString("bloco");
                    String numero = rs.getString("numero");
                    if (bloco != null && numero != null) {
                        o.setLocalFormatado("Bloco " + bloco + " – " + numero);
                    } else {
                        o.setLocalFormatado("Área Comum");
                    }

                    lista.add(o);
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro ao listar ocorrencias recentes para o dashboard: " + e.getMessage());
        }

        return lista;
    }

    /**
     * Executa consulta simples de contagem com tratamento de erro.
     */
    private int executarContagem(String sql) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("Erro ao executar contagem [" + sql + "]: " + e.getMessage());
        }
        return 0;
    }
}