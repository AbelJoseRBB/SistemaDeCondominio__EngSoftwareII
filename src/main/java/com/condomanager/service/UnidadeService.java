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

    /**
     * Cadastra uma nova unidade apos validar os campos obrigatorios
     * e garantir que a combinacao bloco + numero e unica no banco.
     *
     * @throws IllegalArgumentException se algum campo obrigatorio estiver vazio
     * @throws IllegalStateException    se ja existir uma unidade com o mesmo bloco e numero
     */
    public void cadastrar(Unidade unidade) {
        validarCamposObrigatorios(unidade);

        if (unidadeDAO.existePorBlocoNumero(unidade.getBloco(), unidade.getNumero(), 0)) {
            throw new IllegalStateException(
                "Ja existe uma unidade cadastrada no Bloco " + unidade.getBloco()
                + ", numero " + unidade.getNumero() + "."
            );
        }

        unidadeDAO.salvar(unidade);
    }

    /**
     * Atualiza os dados de uma unidade existente.
     * Tambem verifica unicidade, ignorando o proprio id da unidade em edicao.
     *
     * @throws IllegalArgumentException se algum campo obrigatorio estiver vazio
     * @throws IllegalStateException    se outra unidade ja usar o mesmo bloco e numero
     */
    public void atualizar(Unidade unidade) {
        validarCamposObrigatorios(unidade);

        if (unidadeDAO.existePorBlocoNumero(unidade.getBloco(), unidade.getNumero(), unidade.getId())) {
            throw new IllegalStateException(
                "Ja existe outra unidade cadastrada no Bloco " + unidade.getBloco()
                + ", numero " + unidade.getNumero() + "."
            );
        }

        unidadeDAO.atualizar(unidade);
    }

    /**
     * Remove uma unidade pelo id.
     */
    public void remover(int id) {
        // TODO: verificar se ha moradores ativos antes de remover
        unidadeDAO.deletar(id);
    }

    /**
     * Busca uma unidade pelo id.
     */
    public Unidade buscarPorId(int id) {
        return unidadeDAO.buscarPorId(id);
    }

    /**
     * Retorna todas as unidades cadastradas, ordenadas por bloco e numero.
     */
    public List<Unidade> listarTodas() {
        return unidadeDAO.listarTodas();
    }

    // -------------------------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------------------------

    /**
     * Valida os campos obrigatorios de uma unidade.
     *
     * @throws IllegalArgumentException se bloco ou numero estiverem vazios
     */
    private void validarCamposObrigatorios(Unidade unidade) {
        if (unidade.getBloco() == null || unidade.getBloco().isBlank()) {
            throw new IllegalArgumentException("O campo Bloco e obrigatorio.");
        }
        if (unidade.getNumero() == null || unidade.getNumero().isBlank()) {
            throw new IllegalArgumentException("O campo Numero e obrigatorio.");
        }
    }
}
