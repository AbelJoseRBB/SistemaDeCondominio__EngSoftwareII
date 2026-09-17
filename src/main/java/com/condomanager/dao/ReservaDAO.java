package com.condomanager.dao;

import com.condomanager.model.Reserva;
import java.time.LocalDate;
import java.util.List;

/**
 * DAO para operacoes de banco de dados relacionadas a Reserva.
 */
public class ReservaDAO {

    public void salvar(Reserva reserva) {
        // TODO: implementar
    }

    public void atualizar(Reserva reserva) {
        // TODO: implementar
    }

    public void cancelar(int id) {
        // TODO: UPDATE reserva SET situacao='CANCELADA' WHERE id=?
    }

    public Reserva buscarPorId(int id) {
        // TODO: implementar
        return null;
    }

    public List<Reserva> listarPorData(LocalDate data) {
        // TODO: SELECT * FROM reserva WHERE data=? ORDER BY hora_inicio
        return null;
    }

    public List<Reserva> listarTodas() {
        // TODO: implementar
        return null;
    }

    /**
     * Verifica se ha conflito de horario para uma area e data.
     * Essencial para o requisito de controle de conflitos.
     */
    public boolean existeConflito(String areaComum, LocalDate data, java.time.LocalTime inicio, java.time.LocalTime fim, Integer idExcluir) {
        // TODO: SELECT COUNT(*) FROM reserva WHERE area_comum=? AND data=?
        //       AND situacao != 'CANCELADA'
        //       AND NOT (hora_fim <= ? OR hora_inicio >= ?)
        //       AND (? IS NULL OR id != ?)
        return false;
    }
}

