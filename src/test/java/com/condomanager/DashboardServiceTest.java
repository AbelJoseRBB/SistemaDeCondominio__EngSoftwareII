package com.condomanager;

import com.condomanager.dao.DashboardDAO;
import com.condomanager.model.DashboardResumo;
import com.condomanager.model.Ocorrencia;
import com.condomanager.model.Reserva;
import com.condomanager.service.DashboardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes do Servico e Modelo do Dashboard")
class DashboardServiceTest {

    @Test
    @DisplayName("Valida criacao e getters/setters do DTO DashboardResumo")
    void testDashboardResumoDTO() {
        DashboardResumo resumo = new DashboardResumo(48, 120, 5, 2, 4, 3);

        assertEquals(48, resumo.getTotalUnidades());
        assertEquals(120, resumo.getTotalMoradores());
        assertEquals(5, resumo.getTaxasPendentes());
        assertEquals(2, resumo.getManutencoesAbertas());
        assertEquals(4, resumo.getReservasConfirmadas());
        assertEquals(3, resumo.getOcorrenciasAbertas());

        resumo.setTotalUnidades(50);
        assertEquals(50, resumo.getTotalUnidades());
    }

    @Test
    @DisplayName("Garante resiliencia do DashboardService com DAO customizado")
    void testDashboardServiceResiliencia() {
        // Cria DAO simulado
        DashboardDAO mockDao = new DashboardDAO() {
            @Override
            public DashboardResumo obterResumoOperacoes() {
                return new DashboardResumo(10, 25, 3, 1, 2, 1);
            }

            @Override
            public List<Reserva> listarReservasRecentes(int limite) {
                Reserva r = new Reserva();
                r.setId(1);
                r.setAreaComum("Salão de Festas");
                r.setUnidadeFormatada("Bloco A – 101");
                r.setData(LocalDate.now());
                r.setHoraInicio(LocalTime.of(18, 0));
                r.setHoraFim(LocalTime.of(22, 0));
                r.setSituacao("CONFIRMADA");
                return List.of(r);
            }

            @Override
            public List<Ocorrencia> listarOcorrenciasRecentes(int limite) {
                Ocorrencia o = new Ocorrencia();
                o.setId(1);
                o.setTitulo("Vazamento na garagem");
                o.setLocalFormatado("Área Comum");
                o.setDataAbertura(LocalDateTime.now());
                o.setSituacao("ABERTA");
                return List.of(o);
            }
        };

        DashboardService service = new DashboardService(mockDao);

        DashboardResumo resumo = service.carregarResumo();
        assertNotNull(resumo);
        assertEquals(10, resumo.getTotalUnidades());
        assertEquals(25, resumo.getTotalMoradores());
        assertEquals(3, resumo.getTaxasPendentes());
        assertEquals(1, resumo.getManutencoesAbertas());

        List<Reserva> reservas = service.listarReservasRecentes(5);
        assertNotNull(reservas);
        assertEquals(1, reservas.size());
        assertEquals("Salão de Festas", reservas.get(0).getAreaComum());

        List<Ocorrencia> ocorrencias = service.listarOcorrenciasRecentes(5);
        assertNotNull(ocorrencias);
        assertEquals(1, ocorrencias.size());
        assertEquals("Vazamento na garagem", ocorrencias.get(0).getTitulo());
    }

    @Test
    @DisplayName("Garante que falhas no DAO retornam valores seguros sem quebrar o sistema")
    void testDashboardServiceFallbackEmErro() {
        DashboardDAO daoComFalha = new DashboardDAO() {
            @Override
            public DashboardResumo obterResumoOperacoes() {
                throw new RuntimeException("Simulando falha de conexao com banco");
            }

            @Override
            public List<Reserva> listarReservasRecentes(int limite) {
                throw new RuntimeException("Simulando falha de conexao");
            }

            @Override
            public List<Ocorrencia> listarOcorrenciasRecentes(int limite) {
                throw new RuntimeException("Simulando falha de conexao");
            }
        };

        DashboardService service = new DashboardService(daoComFalha);

        DashboardResumo resumo = service.carregarResumo();
        assertNotNull(resumo, "Nao deve retornar null mesmo em caso de falha de conexao.");
        assertEquals(0, resumo.getTotalUnidades());
        assertEquals(0, resumo.getTotalMoradores());

        List<Reserva> reservas = service.listarReservasRecentes(5);
        assertNotNull(reservas);
        assertTrue(reservas.isEmpty());

        List<Ocorrencia> ocorrencias = service.listarOcorrenciasRecentes(5);
        assertNotNull(ocorrencias);
        assertTrue(ocorrencias.isEmpty());
    }
}