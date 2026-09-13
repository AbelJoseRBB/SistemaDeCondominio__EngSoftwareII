package com.condomanager.dao;

import com.condomanager.model.Ocorrencia;
import java.util.List;

/**
 * DAO para operacoes de banco de dados relacionadas a Ocorrencia.
 */
public class OcorrenciaDAO {

    public void salvar(Ocorrencia ocorrencia) {
        // TODO: implementar
    }

    public void atualizar(Ocorrencia ocorrencia) {
        // TODO: implementar
    }

    public Ocorrencia buscarPorId(int id) {
        // TODO: implementar
        return null;
    }

    public List<Ocorrencia> listarTodas() {
        // TODO: SELECT * FROM ocorrencia ORDER BY data_abertura DESC
        return null;
    }

    public List<Ocorrencia> listarPorSituacao(String situacao) {
        // TODO: SELECT * FROM ocorrencia WHERE situacao=?
        return null;
    }
}

