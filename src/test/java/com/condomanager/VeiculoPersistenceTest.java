package com.condomanager;

import com.condomanager.dao.VeiculoDAO;
import com.condomanager.model.Veiculo;
import com.condomanager.service.VeiculoService;
import com.condomanager.util.DBConnection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import java.sql.*;
import static org.junit.jupiter.api.Assertions.*;

/** Executar somente em um banco descartável com o schema completo. */
@EnabledIfEnvironmentVariable(named="VEICULO_MYSQL_TEST", matches="true")
class VeiculoPersistenceTest {
    private int unidade, outraUnidade, dono, outroDono;
    private int inserir(String sql) throws SQLException {
        try(Connection c=DBConnection.getConnection(); PreparedStatement s=c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)) {
            s.executeUpdate(); try(ResultSet r=s.getGeneratedKeys()) { r.next(); return r.getInt(1); }
        }
    }
    private void executar(String sql) throws SQLException {
        try(Connection c=DBConnection.getConnection(); Statement s=c.createStatement()) { s.executeUpdate(sql); }
    }
    private Veiculo veiculo(String placa,String vaga) {
        Veiculo v=new Veiculo(); v.setIdUnidade(unidade); v.setIdProprietario(dono); v.setPlaca(placa);
        v.setModelo("Onix"); v.setMarca("Chevrolet"); v.setCor("Prata"); v.setNumeroVaga(vaga); return v;
    }
    @Test void persistenciaConflitosEdicaoELiberacaoDeVaga() throws Exception {
        try(Connection c=DBConnection.getConnection()) {
            assertTrue(c.getCatalog().startsWith("codex_veiculos_test_"),"Use um banco exclusivo de teste");
        }
        VeiculoDAO dao=new VeiculoDAO(); VeiculoService service=new VeiculoService(dao);
        try {
            unidade=inserir("INSERT INTO unidade(bloco,numero,limite_veiculos) VALUES('TEST','901',2)");
            outraUnidade=inserir("INSERT INTO unidade(bloco,numero,limite_veiculos) VALUES('TEST','902',2)");
            dono=inserir("INSERT INTO morador(id_unidade,nome,tipo) VALUES("+unidade+",'Dono teste','PROPRIETARIO')");
            outroDono=inserir("INSERT INTO morador(id_unidade,nome,tipo) VALUES("+outraUnidade+",'Outro dono','INQUILINO')");
            Veiculo a=veiculo("tst1a01","TEST-01"); service.salvar(a);
            assertTrue(a.getId()>0); Veiculo persistido=dao.buscarPorId(a.getId());
            assertEquals("TST-1A01",persistido.getPlaca()); assertEquals("Chevrolet",persistido.getMarca());
            assertEquals("Dono teste",persistido.getProprietario()); assertEquals("TEST-901",persistido.getUnidade());
            assertThrows(IllegalArgumentException.class,()->service.salvar(veiculo("TST-1A01","TEST-02")));
            assertThrows(IllegalArgumentException.class,()->service.salvar(veiculo("TST1A02","test-01")));
            Veiculo errado=veiculo("TST1A02","TEST-02"); errado.setIdProprietario(outroDono);
            assertThrows(IllegalArgumentException.class,()->service.salvar(errado));
            executar("UPDATE morador SET situacao='INATIVO' WHERE id="+dono);
            assertThrows(IllegalArgumentException.class,()->service.salvar(veiculo("TST1A02","TEST-02")));
            executar("UPDATE morador SET situacao='ATIVO' WHERE id="+dono);
            Veiculo b=veiculo("TST1A02","TEST-02"); service.salvar(b);
            assertThrows(IllegalArgumentException.class,()->service.salvar(veiculo("TST1A03","TEST-03")));
            a.setCor("Azul"); service.salvar(a); assertEquals("Azul",dao.buscarPorId(a.getId()).getCor());
            assertArrayEquals(new int[]{1,2},service.ocupacao(unidade,a.getId()));
            a.setIdUnidade(outraUnidade); a.setIdProprietario(outroDono); service.salvar(a);
            assertEquals("TEST-902",dao.buscarPorId(a.getId()).getUnidade());
            // Índices também rejeitam gravações que contornem o serviço.
            assertThrows(SQLException.class,()->executar("INSERT INTO veiculo(id_unidade,placa,numero_vaga) VALUES("+unidade+",'tst1a01','TEST-04')"));
            assertThrows(SQLException.class,()->executar("INSERT INTO veiculo(id_unidade,placa,numero_vaga) VALUES("+unidade+",'TST1A04','test-01')"));
            service.deletar(a.getId()); assertNull(dao.buscarPorId(a.getId()));
            Veiculo reutilizado=veiculo("TST1A01","TEST-01"); service.salvar(reutilizado);
            assertTrue(dao.listarTodos().stream().anyMatch(v->v.getId()==reutilizado.getId()));
            assertArrayEquals(new int[]{2,2},service.ocupacao(unidade,0));
        } finally {
            executar("DELETE FROM veiculo WHERE id_unidade IN ("+unidade+","+outraUnidade+")");
            executar("DELETE FROM morador WHERE id_unidade IN ("+unidade+","+outraUnidade+")");
            executar("DELETE FROM unidade WHERE id IN ("+unidade+","+outraUnidade+")");
        }
    }
}
