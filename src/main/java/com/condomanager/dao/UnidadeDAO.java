package com.condomanager.dao;

import com.condomanager.model.Unidade;
import java.util.List;

/**
 * DAO para operacoes de banco de dados relacionadas a Unidade.
 * Implemente os metodos abaixo usando JDBC + SQL.
 */
public class UnidadeDAO {

    public void salvar(Unidade unidade) {
        // TODO: INSERT INTO unidade (bloco, numero, proprietario, situacao, ...) VALUES (?, ?, ?, ?, ...)
    }

    public void atualizar(Unidade unidade) {
        // TODO: UPDATE unidade SET bloco=?, numero=?, ... WHERE id=?
    }

    public void deletar(int id) {
        // TODO: DELETE FROM unidade WHERE id=?
    }

    public Unidade buscarPorId(int id) {
        // TODO: SELECT * FROM unidade WHERE id=?
        return null;
    }

    public List<Unidade> listarTodas() {
        // TODO: SELECT * FROM unidade ORDER BY bloco, numero
        return null;
    }
}

