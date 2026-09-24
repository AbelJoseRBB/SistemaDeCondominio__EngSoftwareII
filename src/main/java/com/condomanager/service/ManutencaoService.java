package com.condomanager.service;

import com.condomanager.dao.ManutencaoDAO;
import com.condomanager.model.Manutencao;

import java.util.List;

/** Service com regras de negocio para Manutencao */
public class ManutencaoService {
    
    private final ManutencaoDAO manutencaoDAO;
    
    public ManutencaoService() {
        this.manutencaoDAO = new ManutencaoDAO();
    }
    
    public void salvar(Manutencao manutencao) {
        validarManutencao(manutencao);
        manutencaoDAO.salvar(manutencao);
    }
    
    public void atualizar(Manutencao manutencao) {
        validarManutencao(manutencao);
        manutencaoDAO.atualizar(manutencao);
    }
    
    public void deletar(int id) {
        manutencaoDAO.deletar(id);
    }
    
    public List<Manutencao> listarTodas() {
        return manutencaoDAO.listarTodos();
    }
    
    public List<Manutencao> listarComFiltros(String busca, String status) {
        return manutencaoDAO.listarComFiltros(busca, status);
    }
    
    private void validarManutencao(Manutencao manutencao) {
        if (manutencao.getDescricao() == null || manutencao.getDescricao().trim().isEmpty()) {
            throw new IllegalArgumentException("A descrição da manutenção é obrigatória.");
        }
        if (manutencao.getDataSolicitacao() == null) {
            throw new IllegalArgumentException("A data de solicitação/prevista é obrigatória.");
        }
        if (manutencao.getSituacao() == null || manutencao.getSituacao().trim().isEmpty()) {
            throw new IllegalArgumentException("A situação da manutenção é obrigatória.");
        }
    }
}


