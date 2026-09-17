package com.condomanager.service;

import com.condomanager.dao.ReservaDAO;
import com.condomanager.model.Reserva;
import java.util.List;

/**
 * Servico com as regras de negocio para Reservas de Areas Comuns.
 * Inclui controle de conflitos de horario (requisito do projeto).
 */
public class ReservaService {

    private final ReservaDAO reservaDAO = new ReservaDAO();

    public void registrar(Reserva reserva) {
        // Regra de negocio: verificar conflito de horario
        boolean conflito = reservaDAO.existeConflito(
            reserva.getAreaComum(),
            reserva.getData(),
            reserva.getHoraInicio(),
            reserva.getHoraFim(),
            null
        );
        if (conflito) {
            throw new IllegalStateException("Ja existe uma reserva para essa area nesse horario.");
        }
        reservaDAO.salvar(reserva);
    }

    public void atualizar(Reserva reserva) {
        boolean conflito = reservaDAO.existeConflito(
            reserva.getAreaComum(),
            reserva.getData(),
            reserva.getHoraInicio(),
            reserva.getHoraFim(),
            reserva.getId()
        );
        if (conflito) {
            throw new IllegalStateException("Ja existe uma reserva para essa area nesse horario.");
        }
        reservaDAO.atualizar(reserva);
    }

    public void cancelar(int id) {
        reservaDAO.cancelar(id);
    }

    public List<Reserva> listarTodas() {
        return reservaDAO.listarTodas();
    }
}

