package com.condomanager.service;

import com.condomanager.dao.OcorrenciaDAO;
import com.condomanager.model.Ocorrencia;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servico com as regras de negocio para Ocorrencias.
 */
public class OcorrenciaService {

    private final OcorrenciaDAO ocorrenciaDAO = new OcorrenciaDAO();

    public void registrar(Ocorrencia ocorrencia) {
        if (ocorrencia.getTitulo() == null || ocorrencia.getTitulo().isBlank()) {
            throw new IllegalArgumentException("Titulo e obrigatorio.");
        }
        ocorrencia.setDataAbertura(LocalDateTime.now());
        ocorrencia.setSituacao("ABERTA");
        ocorrenciaDAO.salvar(ocorrencia);
    }

    public void encerrar(Ocorrencia ocorrencia, String resposta) {
        ocorrencia.setSituacao("ENCERRADA");
        ocorrencia.setDataEncerramento(LocalDateTime.now());
        ocorrencia.setResposta(resposta);
        ocorrenciaDAO.atualizar(ocorrencia);
    }

    public List<Ocorrencia> listarTodas() {
        return ocorrenciaDAO.listarTodas();
    }
}

