package com.condomanager.dao;

import com.condomanager.model.Reserva;
import com.condomanager.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para operacoes de banco de dados relacionadas a Reserva.
 * Utiliza JDBC com PreparedStatement seguindo o padrao do UnidadeDAO.
 * Inclui controle de conflitos de horario via SQL de sobreposicao de intervalos.
 */
public class ReservaDAO {

    /**
     * Insere uma nova reserva no banco.
     * O id gerado automaticamente e atribuido de volta ao objeto.
     */
    public void salvar(Reserva reserva) {
        String sql = "INSERT INTO reserva "
                   + "(id_unidade, area_comum, data, hora_inicio, hora_fim, situacao, observacoes) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, reserva.getIdUnidade());
            stmt.setString(2, reserva.getAreaComum());
            stmt.setDate(3, Date.valueOf(reserva.getData()));
            stmt.setTime(4, Time.valueOf(reserva.getHoraInicio()));
            stmt.setTime(5, Time.valueOf(reserva.getHoraFim()));
            stmt.setString(6, reserva.getSituacao());
            stmt.setString(7, reserva.getObservacoes());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    reserva.setId(keys.getInt(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar reserva: " + e.getMessage(), e);
        }
    }

    /**
     * Atualiza os dados de uma reserva existente pelo seu id.
     */
    public void atualizar(Reserva reserva) {
        String sql = "UPDATE reserva "
                   + "SET id_unidade=?, area_comum=?, data=?, hora_inicio=?, "
                   + "    hora_fim=?, situacao=?, observacoes=? "
                   + "WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, reserva.getIdUnidade());
            stmt.setString(2, reserva.getAreaComum());
            stmt.setDate(3, Date.valueOf(reserva.getData()));
            stmt.setTime(4, Time.valueOf(reserva.getHoraInicio()));
            stmt.setTime(5, Time.valueOf(reserva.getHoraFim()));
            stmt.setString(6, reserva.getSituacao());
            stmt.setString(7, reserva.getObservacoes());
            stmt.setInt(8, reserva.getId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar reserva: " + e.getMessage(), e);
        }
    }

    /**
     * Marca uma reserva como CANCELADA (nao remove o registro do banco).
     */
    public void cancelar(int id) {
        String sql = "UPDATE reserva SET situacao='CANCELADA' WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao cancelar reserva: " + e.getMessage(), e);
        }
    }

    /**
     * Remove permanentemente uma reserva pelo id.
     */
    public void deletar(int id) {
        String sql = "DELETE FROM reserva WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao deletar reserva: " + e.getMessage(), e);
        }
    }

    /**
     * Busca uma reserva pelo seu id, com JOIN para montar o unidadeFormatada.
     *
     * @return a Reserva encontrada, ou null se nao existir.
     */
    public Reserva buscarPorId(int id) {
        String sql = "SELECT r.*, u.bloco, u.numero "
                   + "FROM reserva r "
                   + "JOIN unidade u ON r.id_unidade = u.id "
                   + "WHERE r.id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearReserva(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar reserva por id: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Retorna todas as reservas ordenadas por data (mais recentes primeiro)
     * e hora de inicio. Faz JOIN com unidade para montar o unidadeFormatada.
     */
    public List<Reserva> listarTodas() {
        String sql = "SELECT r.*, u.bloco, u.numero "
                   + "FROM reserva r "
                   + "JOIN unidade u ON r.id_unidade = u.id "
                   + "ORDER BY r.data DESC, r.hora_inicio";

        List<Reserva> lista = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearReserva(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar reservas: " + e.getMessage(), e);
        }
        return lista;
    }

    /**
     * Retorna as reservas de uma data especifica, ordenadas por hora de inicio.
     */
    public List<Reserva> listarPorData(LocalDate data) {
        String sql = "SELECT r.*, u.bloco, u.numero "
                   + "FROM reserva r "
                   + "JOIN unidade u ON r.id_unidade = u.id "
                   + "WHERE r.data = ? "
                   + "ORDER BY r.hora_inicio";

        List<Reserva> lista = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(data));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearReserva(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar reservas por data: " + e.getMessage(), e);
        }
        return lista;
    }

    /**
     * Verifica se ha conflito de horario para uma area e data especificas.
     *
     * O algoritmo usa a logica de sobreposicao de intervalos:
     *   dois intervalos [A, B] e [C, D] se sobrepoem quando: A < D && C < B
     *   Invertendo: NAO ha sobreposicao quando: hora_fim <= inicio_novo OR hora_inicio >= fim_novo
     *
     * Ignora reservas CANCELADAS e, opcionalmente, ignora a propria reserva
     * sendo editada (parametro idExcluir).
     *
     * @param areaComum  nome da area a verificar
     * @param data       data da reserva
     * @param inicio     hora de inicio da nova reserva
     * @param fim        hora de fim da nova reserva
     * @param idExcluir  id da reserva a ignorar na verificacao (para edicao);
     *                   passe null quando for um novo cadastro
     * @return true se houver conflito de horario
     */
    public boolean existeConflito(String areaComum, LocalDate data,
                                  LocalTime inicio, LocalTime fim,
                                  Integer idExcluir) {
        String sql = "SELECT COUNT(*) FROM reserva "
                   + "WHERE area_comum = ? "
                   + "  AND data = ? "
                   + "  AND situacao != 'CANCELADA' "
                   + "  AND NOT (hora_fim <= ? OR hora_inicio >= ?) "
                   + "  AND (? IS NULL OR id != ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, areaComum);
            stmt.setDate(2, Date.valueOf(data));
            stmt.setTime(3, Time.valueOf(inicio)); // hora_fim <= inicio_novo → sem conflito
            stmt.setTime(4, Time.valueOf(fim));    // hora_inicio >= fim_novo  → sem conflito

            // Parametros do idExcluir (dois binds para o mesmo valor)
            if (idExcluir != null) {
                stmt.setInt(5, idExcluir);
                stmt.setInt(6, idExcluir);
            } else {
                stmt.setNull(5, Types.INTEGER);
                stmt.setNull(6, Types.INTEGER);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar conflito de reserva: " + e.getMessage(), e);
        }
        return false;
    }

    // ------------------------------------------------------------------
    // Helpers privados
    // ------------------------------------------------------------------

    /**
     * Mapeia uma linha do ResultSet para um objeto Reserva.
     * Monta o campo transiente unidadeFormatada a partir do JOIN com unidade:
     *   "Bloco X – NNN"
     */
    private Reserva mapearReserva(ResultSet rs) throws SQLException {
        Reserva r = new Reserva();
        r.setId(rs.getInt("id"));
        r.setIdUnidade(rs.getInt("id_unidade"));
        r.setAreaComum(rs.getString("area_comum"));

        Date data = rs.getDate("data");
        if (data != null) {
            r.setData(data.toLocalDate());
        }

        Time horaInicio = rs.getTime("hora_inicio");
        if (horaInicio != null) {
            r.setHoraInicio(horaInicio.toLocalTime());
        }

        Time horaFim = rs.getTime("hora_fim");
        if (horaFim != null) {
            r.setHoraFim(horaFim.toLocalTime());
        }

        r.setSituacao(rs.getString("situacao"));
        r.setObservacoes(rs.getString("observacoes"));

        // Monta o campo transiente unidadeFormatada a partir do JOIN
        String bloco  = rs.getString("bloco");
        String numero = rs.getString("numero");
        if (bloco != null && numero != null) {
            r.setUnidadeFormatada("Bloco " + bloco + " – " + numero);
        } else {
            r.setUnidadeFormatada("");
        }

        return r;
    }
}


