package com.condomanager.service;

import com.condomanager.dao.ReservaDAO;
import com.condomanager.model.Reserva;

import java.util.List;

/**
 * Servico com as regras de negocio para Reservas de Areas Comuns.
 * Centraliza validacoes de campos obrigatorios, consistencia de horarios
 * e controle de conflitos (requisito principal do modulo).
 */
public class ReservaService {

    private final ReservaDAO reservaDAO = new ReservaDAO();

    // ------------------------------------------------------------------
    // Escrita
    // ------------------------------------------------------------------

    /**
     * Registra uma nova reserva aplicando as regras de negocio:
     *   - Unidade, area comum, data, hora inicio e hora fim sao obrigatorios
     *   - Hora fim deve ser posterior a hora inicio
     *   - Nao pode haver sobreposicao com outra reserva ativa na mesma area e data
     *   - Status inicial e definido automaticamente como "CONFIRMADA"
     *
     * @throws IllegalArgumentException se algum campo obrigatorio estiver ausente
     *                                  ou se hora_fim <= hora_inicio
     * @throws IllegalStateException    se houver conflito de horario com reserva existente
     */
    public void registrar(Reserva reserva) {
        validarCamposObrigatorios(reserva);

        // Regra automatica: status inicial sempre CONFIRMADA
        reserva.setSituacao("CONFIRMADA");

        // Regra de negocio: verificar conflito de horario
        if (reservaDAO.existeConflito(
                reserva.getAreaComum(),
                reserva.getData(),
                reserva.getHoraInicio(),
                reserva.getHoraFim(),
                null)) {
            throw new IllegalStateException(
                "Ja existe uma reserva confirmada para \""
                + reserva.getAreaComum()
                + "\" nesse horario. Escolha outro horario ou area.");
        }

        reservaDAO.salvar(reserva);
    }

    /**
     * Atualiza uma reserva existente.
     * Reavalida conflitos de horario excluindo a propria reserva da verificacao.
     *
     * @throws IllegalArgumentException se algum campo obrigatorio estiver ausente
     *                                  ou se hora_fim <= hora_inicio
     * @throws IllegalStateException    se houver conflito de horario com outra reserva
     */
    public void atualizar(Reserva reserva) {
        validarCamposObrigatorios(reserva);

        if (reservaDAO.existeConflito(
                reserva.getAreaComum(),
                reserva.getData(),
                reserva.getHoraInicio(),
                reserva.getHoraFim(),
                reserva.getId())) {
            throw new IllegalStateException(
                "Ja existe uma reserva confirmada para \""
                + reserva.getAreaComum()
                + "\" nesse horario. Escolha outro horario ou area.");
        }

        reservaDAO.atualizar(reserva);
    }

    /**
     * Cancela uma reserva (muda situacao para CANCELADA, preserva o historico).
     */
    public void cancelar(int id) {
        reservaDAO.cancelar(id);
    }

    /**
     * Remove permanentemente uma reserva pelo id.
     */
    public void remover(int id) {
        reservaDAO.deletar(id);
    }

    // ------------------------------------------------------------------
    // Leitura
    // ------------------------------------------------------------------

    /**
     * Retorna todas as reservas ordenadas por data (mais recentes primeiro).
     */
    public List<Reserva> listarTodas() {
        return reservaDAO.listarTodas();
    }

    // ------------------------------------------------------------------
    // Helpers privados
    // ------------------------------------------------------------------

    /**
     * Valida os campos obrigatorios e a consistencia dos horarios.
     * Lanca excecoes com mensagens claras para o Controller exibir na tela.
     *
     * @throws IllegalArgumentException se qualquer campo obrigatorio estiver ausente
     *                                  ou se hora_fim <= hora_inicio
     */
    private void validarCamposObrigatorios(Reserva reserva) {
        if (reserva.getIdUnidade() <= 0) {
            throw new IllegalArgumentException("Selecione a unidade responsavel pela reserva.");
        }
        if (reserva.getAreaComum() == null || reserva.getAreaComum().isBlank()) {
            throw new IllegalArgumentException("Selecione a area comum a ser reservada.");
        }
        if (reserva.getData() == null) {
            throw new IllegalArgumentException("A data da reserva e obrigatoria.");
        }
        if (reserva.getHoraInicio() == null) {
            throw new IllegalArgumentException("O horario de inicio e obrigatorio.");
        }
        if (reserva.getHoraFim() == null) {
            throw new IllegalArgumentException("O horario de termino e obrigatorio.");
        }
        if (!reserva.getHoraFim().isAfter(reserva.getHoraInicio())) {
            throw new IllegalArgumentException(
                "O horario de termino deve ser posterior ao horario de inicio.");
        }
    }
}


