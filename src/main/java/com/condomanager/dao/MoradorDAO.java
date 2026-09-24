package com.condomanager.dao;

import com.condomanager.model.Morador;
import java.util.List;

/**
 * DAO para operacoes de banco de dados relacionadas a Morador.
 */
public class MoradorDAO {

    public void salvar(Morador morador) {
        // TODO: implementar
    }

    public void atualizar(Morador morador) {
        // TODO: implementar
    }

    public void deletar(int id) {
        // TODO: implementar
    }

    public Morador buscarPorId(int id) {
        // TODO: implementar
        return null;
    }

    public List<Morador> listarPorUnidade(int idUnidade) {
        // TODO: SELECT * FROM morador WHERE id_unidade=?
        return null;
    }

    public List<Morador> listarTodos() {
        // TODO: SELECT * FROM morador ORDER BY nome
        return null;
    }
}

