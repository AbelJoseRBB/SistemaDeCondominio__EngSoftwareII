package com.condomanager.service;

import com.condomanager.dao.OcorrenciaDAO;
import com.condomanager.model.Ocorrencia;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servico com as regras de negocio para Ocorrencias.
 * Centraliza validacoes e comportamentos automaticos,
 * mantendo o Controller e o DAO livres de logica de negocio.
 */
public class OcorrenciaService {

    private final OcorrenciaDAO ocorrenciaDAO = new OcorrenciaDAO();

    // ------------------------------------------------------------------
    // Escrita
    // ------------------------------------------------------------------

    /**
     * Registra uma nova ocorrencia aplicando as regras de negocio:
     *   - Titulo e descricao sao obrigatorios
     *   - Status inicial e definido automaticamente como "ABERTA"
     *   - Data de abertura e preenchida automaticamente com o momento atual
     *
     * @throws IllegalArgumentException se titulo ou descricao estiverem vazios
     */
    public void registrar(Ocorrencia ocorrencia) {
        validarCamposObrigatorios(ocorrencia);

        // Regras automaticas do sistema (nao editaveis pelo usuario no cadastro)
        ocorrencia.setSituacao("ABERTA");
        ocorrencia.setDataAbertura(LocalDateTime.now());

        ocorrenciaDAO.salvar(ocorrencia);
    }

    /**
     * Atualiza os dados de uma ocorrencia existente.
     * Aplica regra automatica: se a nova situacao for "ENCERRADA",
     * define data_encerramento com o momento atual (caso ainda nao esteja definida).
     *
     * @throws IllegalArgumentException se titulo ou descricao estiverem vazios
     */
    public void atualizar(Ocorrencia ocorrencia) {
        validarCamposObrigatorios(ocorrencia);

        // Regra automatica: encerramento define a data de encerramento
        if ("ENCERRADA".equals(ocorrencia.getSituacao())
                && ocorrencia.getDataEncerramento() == null) {
            ocorrencia.setDataEncerramento(LocalDateTime.now());
        }

        // Regra inversa: se reaberta, limpa a data de encerramento
        if (!"ENCERRADA".equals(ocorrencia.getSituacao())) {
            ocorrencia.setDataEncerramento(null);
        }

        ocorrenciaDAO.atualizar(ocorrencia);
    }

    /**
     * Atalho para encerrar uma ocorrencia com resposta (mantido por compatibilidade).
     * Equivale a chamar atualizar() com situacao = "ENCERRADA".
     */
    public void encerrar(Ocorrencia ocorrencia, String resposta) {
        ocorrencia.setSituacao("ENCERRADA");
        ocorrencia.setResposta(resposta);
        atualizar(ocorrencia);
    }

    /**
     * Remove permanentemente uma ocorrencia pelo id.
     */
    public void remover(int id) {
        ocorrenciaDAO.deletar(id);
    }

    // ------------------------------------------------------------------
    // Leitura
    // ------------------------------------------------------------------

    /**
     * Retorna todas as ocorrencias ordenadas da mais recente para a mais antiga.
     */
    public List<Ocorrencia> listarTodas() {
        return ocorrenciaDAO.listarTodas();
    }

    /**
     * Retorna ocorrencias filtradas pela situacao.
     *
     * @param situacao "ABERTA", "EM_ANDAMENTO" ou "ENCERRADA"
     */
    public List<Ocorrencia> listarPorSituacao(String situacao) {
        return ocorrenciaDAO.listarPorSituacao(situacao);
    }

    // ------------------------------------------------------------------
    // Helpers privados
    // ------------------------------------------------------------------

    /**
     * Valida os campos obrigatorios de uma ocorrencia.
     * Lanca IllegalArgumentException com mensagem clara para o Controller exibir.
     */
    private void validarCamposObrigatorios(Ocorrencia ocorrencia) {
        if (ocorrencia.getTitulo() == null || ocorrencia.getTitulo().isBlank()) {
            throw new IllegalArgumentException("O titulo da ocorrencia e obrigatorio.");
        }
        if (ocorrencia.getDescricao() == null || ocorrencia.getDescricao().isBlank()) {
            throw new IllegalArgumentException("A descricao da ocorrencia e obrigatoria.");
        }
        if (ocorrencia.getCategoria() == null || ocorrencia.getCategoria().isBlank()) {
            throw new IllegalArgumentException("A categoria da ocorrencia e obrigatoria.");
        }
    }
}


