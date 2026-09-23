package com.condomanager;

import com.condomanager.dao.VeiculoDAO;
import com.condomanager.model.Veiculo;
import com.condomanager.service.VeiculoService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VeiculoServiceTest {
    private Veiculo valido() {
        Veiculo v=new Veiculo(); v.setPlaca(" abc1d23 "); v.setModelo(" Onix "); v.setMarca("Chevrolet");
        v.setCor("Prata"); v.setNumeroVaga(" a-01 "); v.setIdUnidade(1); v.setIdProprietario(2); return v;
    }
    @Test void normalizaAntesDePersistir() {
        Veiculo v=valido();
        VeiculoDAO dao=new VeiculoDAO() { @Override public void gravar(Veiculo salvo) {
            assertEquals("ABC-1D23",salvo.getPlaca()); assertEquals("A-01",salvo.getNumeroVaga()); assertEquals("Onix",salvo.getModelo()); salvo.setId(42);
        }};
        new VeiculoService(dao).salvar(v); assertEquals(42,v.getId());
    }
    @Test void aceitaPlacaAntiga() {
        Veiculo v=valido(); v.setPlaca("abc-1234");
        new VeiculoService(new VeiculoDAO() { @Override public void gravar(Veiculo salvo) { assertEquals("ABC-1234",salvo.getPlaca()); }}).salvar(v);
    }
    @Test void rejeitaCamposInvalidosSemPersistir() {
        VeiculoService service=new VeiculoService(new VeiculoDAO() { @Override public void gravar(Veiculo v) { fail("Não deve persistir"); }});
        Veiculo v=valido(); v.setPlaca("1234"); assertThrows(IllegalArgumentException.class,()->service.salvar(v));
        v.setPlaca("ABC1234"); v.setMarca(" "); assertThrows(IllegalArgumentException.class,()->service.salvar(v));
        v.setMarca("Chevrolet"); v.setIdProprietario(0); assertThrows(IllegalArgumentException.class,()->service.salvar(v));
        v.setIdProprietario(2); v.setIdUnidade(0); assertThrows(IllegalArgumentException.class,()->service.salvar(v));
        v.setIdUnidade(1); v.setNumeroVaga(" "); assertThrows(IllegalArgumentException.class,()->service.salvar(v));
    }
    @Test void rejeitaConflitosELimite() {
        assertThrows(IllegalArgumentException.class,()->VeiculoService.validarDisponibilidade(2,0,true,false,true));
        assertThrows(IllegalArgumentException.class,()->VeiculoService.validarDisponibilidade(2,0,false,true,true));
        assertThrows(IllegalArgumentException.class,()->VeiculoService.validarDisponibilidade(2,2,false,false,true));
        assertThrows(IllegalArgumentException.class,()->VeiculoService.validarDisponibilidade(0,0,false,false,true));
        assertThrows(IllegalArgumentException.class,()->VeiculoService.validarDisponibilidade(2,0,false,false,false));
        assertThrows(IllegalArgumentException.class,()->VeiculoService.validarDisponibilidade(-1,0,false,false,true));
        assertDoesNotThrow(()->VeiculoService.validarDisponibilidade(2,1,false,false,true));
    }
}
