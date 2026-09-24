package com.condomanager.service;

import com.condomanager.dao.UnidadeDAO;
import com.condomanager.model.Unidade;
import java.util.List;

/**
 * Servico que contem as regras de negocio para Unidade.
 * O Controller chama o Service, que por sua vez chama o DAO.
 */
public class UnidadeService {

    private final UnidadeDAO unidadeDAO = new UnidadeDAO();

    public void cadastrar(Unidade unidade) {
        // Regras de negocio: validacoes antes de salvar
        if (unidade.getBloco() == null || unidade.getBloco().isBlank()) {
            throw new IllegalArgumentException("Bloco e obrigatorio.");
        }
        if (unidade.getNumero() == null || unidade.getNumero().isBlank()) {
            throw new IllegalArgumentException("Numero da unidade e obrigatorio.");
        }
        unidadeDAO.salvar(unidade);
    }

    public void atualizar(Unidade unidade) {
        // TODO: adicionar validacoes
        unidadeDAO.atualizar(unidade);
    }

    public void remover(int id) {
        // TODO: verificar se ha moradores ativos antes de remover
        unidadeDAO.deletar(id);
    }

    public Unidade buscarPorId(int id) {
        return unidadeDAO.buscarPorId(id);
    }

    public List<Unidade> listarTodas() {
        return unidadeDAO.listarTodas();
    }
}

