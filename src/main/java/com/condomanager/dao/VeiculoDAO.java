package com.condomanager.dao;

import com.condomanager.model.Veiculo;
import com.condomanager.service.VeiculoService;
import com.condomanager.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VeiculoDAO {
    private static final String SELECT = "SELECT v.*, CONCAT(u.bloco, '-', u.numero) unidade, m.nome proprietario FROM veiculo v JOIN unidade u ON u.id=v.id_unidade LEFT JOIN morador m ON m.id=v.id_proprietario";
    public List<Veiculo> listarTodos() {
        try (Connection c = DBConnection.getConnection(); PreparedStatement s = c.prepareStatement(SELECT + " ORDER BY v.placa"); ResultSet r = s.executeQuery()) {
            List<Veiculo> lista = new ArrayList<>();
            while (r.next()) lista.add(mapear(r));
            return lista;
        } catch (SQLException e) { throw erro(e); }
    }
    public Veiculo buscarPorId(int id) {
        try (Connection c = DBConnection.getConnection(); PreparedStatement s = c.prepareStatement(SELECT + " WHERE v.id=?")) {
            s.setInt(1, id);
            try (ResultSet r = s.executeQuery()) { return r.next() ? mapear(r) : null; }
        } catch (SQLException e) { throw erro(e); }
    }
    public void salvar(Veiculo v) { new VeiculoService(this).salvar(v); }
    public void atualizar(Veiculo v) { new VeiculoService(this).salvar(v); }
    public void gravar(Veiculo v) {
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                // O bloqueio da unidade serializa a verificação do limite de vagas.
                int limite = valor(c, "SELECT limite_veiculos FROM unidade WHERE id=? FOR UPDATE", v.getIdUnidade());
                int ocupados = valor(c, "SELECT COUNT(*) FROM veiculo WHERE id_unidade=? AND id<>?", v.getIdUnidade(), v.getId());
                boolean dono = valor(c, "SELECT COUNT(*) FROM morador WHERE id=? AND id_unidade=? AND situacao='ATIVO'", v.getIdProprietario(), v.getIdUnidade()) > 0;
                boolean placa = existe(c, "SELECT COUNT(*) FROM veiculo WHERE placa_normalizada=? AND id<>?", v.getPlaca().replace("-", ""), v.getId());
                boolean vaga = existe(c, "SELECT COUNT(*) FROM veiculo WHERE vaga_normalizada=? AND id<>?", v.getNumeroVaga(), v.getId());
                VeiculoService.validarDisponibilidade(limite, ocupados, placa, vaga, dono);
                String sql = v.getId() == 0
                    ? "INSERT INTO veiculo (id_unidade,id_proprietario,placa,modelo,marca,cor,numero_vaga) VALUES (?,?,?,?,?,?,?)"
                    : "UPDATE veiculo SET id_unidade=?,id_proprietario=?,placa=?,modelo=?,marca=?,cor=?,numero_vaga=? WHERE id=?";
                int gerado = v.getId();
                try (PreparedStatement s = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    s.setInt(1,v.getIdUnidade()); s.setInt(2,v.getIdProprietario()); s.setString(3,v.getPlaca());
                    s.setString(4,v.getModelo()); s.setString(5,v.getMarca()); s.setString(6,v.getCor()); s.setString(7,v.getNumeroVaga());
                    if (v.getId()!=0) s.setInt(8,v.getId());
                    if (s.executeUpdate()!=1) throw new IllegalArgumentException("Veículo não encontrado. Atualize a lista.");
                    if (v.getId()==0) try (ResultSet r=s.getGeneratedKeys()) { if(r.next()) gerado=r.getInt(1); }
                }
                c.commit();
                v.setId(gerado);
            } catch (SQLException | RuntimeException e) { c.rollback(); throw e; }
            finally { c.setAutoCommit(true); }
        } catch (SQLException e) {
            if (e.getErrorCode()==1062) throw new IllegalArgumentException("Placa ou vaga já cadastrada por outro veículo.", e);
            throw erro(e);
        }
    }
    public int[] ocupacao(int unidade, int ignorar) {
        try (Connection c=DBConnection.getConnection()) {
            return new int[]{valor(c,"SELECT COUNT(*) FROM veiculo WHERE id_unidade=? AND id<>?",unidade,ignorar),
                valor(c,"SELECT limite_veiculos FROM unidade WHERE id=?",unidade)};
        } catch(SQLException e) { throw erro(e); }
    }
    public void deletar(int id) {
        try(Connection c=DBConnection.getConnection(); PreparedStatement s=c.prepareStatement("DELETE FROM veiculo WHERE id=?")) {
            s.setInt(1,id);
            if(s.executeUpdate()!=1) throw new IllegalArgumentException("Veículo não encontrado. Atualize a lista.");
        } catch(SQLException e) { throw erro(e); }
    }
    private int valor(Connection c,String sql,int... parametros) throws SQLException {
        try(PreparedStatement s=c.prepareStatement(sql)) {
            for(int i=0;i<parametros.length;i++) s.setInt(i+1,parametros[i]);
            try(ResultSet r=s.executeQuery()) { return r.next()?r.getInt(1):-1; }
        }
    }
    private boolean existe(Connection c,String sql,String texto,int id) throws SQLException {
        try(PreparedStatement s=c.prepareStatement(sql)) {
            s.setString(1,texto); s.setInt(2,id);
            try(ResultSet r=s.executeQuery()) { return r.next() && r.getInt(1)>0; }
        }
    }
    private Veiculo mapear(ResultSet r) throws SQLException {
        Veiculo v=new Veiculo(); v.setId(r.getInt("id")); v.setIdUnidade(r.getInt("id_unidade"));
        v.setIdProprietario(r.getInt("id_proprietario")); v.setPlaca(r.getString("placa"));
        v.setModelo(r.getString("modelo")); v.setMarca(r.getString("marca")); v.setCor(r.getString("cor"));
        v.setNumeroVaga(r.getString("numero_vaga")); v.setUnidade(r.getString("unidade")); v.setProprietario(r.getString("proprietario"));
        return v;
    }
    private RuntimeException erro(SQLException e) {
        return new IllegalStateException("Não foi possível acessar os veículos. Verifique a conexão e a migração do banco.",e);
    }
}
