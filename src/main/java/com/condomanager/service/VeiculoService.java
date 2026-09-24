package com.condomanager.service;

import com.condomanager.dao.VeiculoDAO;
import com.condomanager.model.Veiculo;
import java.util.List;
import java.util.Locale;

public class VeiculoService {
    private final VeiculoDAO dao;
    public VeiculoService() { this(new VeiculoDAO()); }
    public VeiculoService(VeiculoDAO dao) { this.dao = dao; }
    public List<Veiculo> listarTodos() { return dao.listarTodos(); }
    public void deletar(int id) { dao.deletar(id); }
    public int[] ocupacao(int unidade, int ignorar) { return dao.ocupacao(unidade, ignorar); }
    public void salvar(Veiculo v) {
        if (v == null) throw new IllegalArgumentException("Informe o veículo.");
        String placa = campo(v.getPlaca(), "Placa", 10).toUpperCase(Locale.ROOT).replace("-", "");
        if (!placa.matches("[A-Z]{3}[0-9][A-Z0-9][0-9]{2}"))
            throw new IllegalArgumentException("Placa inválida. Use ABC-1234 ou ABC-1D23.");
        v.setPlaca(placa.substring(0, 3) + "-" + placa.substring(3));
        v.setModelo(campo(v.getModelo(), "Modelo", 80));
        v.setMarca(campo(v.getMarca(), "Marca", 80));
        v.setCor(campo(v.getCor(), "Cor", 30));
        v.setNumeroVaga(campo(v.getNumeroVaga(), "Número da vaga", 10).toUpperCase(Locale.ROOT));
        if (v.getIdUnidade() <= 0) throw new IllegalArgumentException("Selecione uma unidade.");
        if (v.getIdProprietario() <= 0) throw new IllegalArgumentException("Selecione o proprietário.");
        dao.gravar(v);
    }
    private String campo(String texto, String nome, int max) {
        if (texto == null || texto.isBlank()) throw new IllegalArgumentException(nome + " é obrigatório.");
        if (texto.trim().length() > max) throw new IllegalArgumentException(nome + " deve ter no máximo " + max + " caracteres.");
        return texto.trim();
    }
    public static void validarDisponibilidade(int limite, int ocupados, boolean placa, boolean vaga, boolean proprietario) {
        if (limite < 0) throw new IllegalArgumentException("Unidade não encontrada.");
        if (!proprietario) throw new IllegalArgumentException("Selecione um morador ativo da unidade como proprietário do veículo.");
        if (placa) throw new IllegalArgumentException("Esta placa já está cadastrada.");
        if (vaga) throw new IllegalArgumentException("Esta vaga já está ocupada por outro veículo.");
        if (ocupados >= limite) throw new IllegalArgumentException("A unidade atingiu o limite máximo de " + limite + " veículo(s)/vaga(s).");
    }
}
