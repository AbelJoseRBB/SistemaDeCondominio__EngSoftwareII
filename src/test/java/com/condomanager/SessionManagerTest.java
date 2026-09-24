package com.condomanager;

import com.condomanager.model.Usuario;
import com.condomanager.util.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes de Seguranca e Gerenciamento de Sessao")
class SessionManagerTest {

    @BeforeEach
    void setUp() {
        SessionManager.encerrarSessao();
    }

    @Test
    @DisplayName("Garante que a sessao inicia desautenticada")
    void testSessaoInicialNula() {
        assertFalse(SessionManager.isLogado(), "O sistema nao deve iniciar logado.");
        assertNull(SessionManager.getUsuarioLogado(), "Usuario logado deve ser null inicialmente.");
    }

    @Test
    @DisplayName("Autentica usuario e valida presenca na sessao")
    void testAutenticacaoUsuario() {
        Usuario usuario = new Usuario(1, "Carlos Síndico", "carlos", "hash_senha", "ADMIN");
        SessionManager.setUsuarioLogado(usuario);

        assertTrue(SessionManager.isLogado(), "A sessao deve indicar que o usuario esta logado.");
        assertNotNull(SessionManager.getUsuarioLogado());
        assertEquals("Carlos Síndico", SessionManager.getUsuarioLogado().getNome());
        assertEquals("ADMIN", SessionManager.getUsuarioLogado().getPerfil());
    }

    @Test
    @DisplayName("Encerra a sessao e remove informacoes do usuario")
    void testEncerramentoSessao() {
        Usuario usuario = new Usuario(2, "Porteiro Noturno", "porteiro", "hash_senha", "OPERADOR");
        SessionManager.setUsuarioLogado(usuario);
        assertTrue(SessionManager.isLogado());

        SessionManager.encerrarSessao();

        assertFalse(SessionManager.isLogado(), "Apos encerramento, a sessao deve ser desautenticada.");
        assertNull(SessionManager.getUsuarioLogado(), "Usuario logado deve ser null apos encerramento.");
    }

    @Test
    @DisplayName("Valida deteccao de inatividade e expiracao da sessao")
    void testInatividadeSessao() throws InterruptedException {
        Usuario usuario = new Usuario(3, "Operador Teste", "operador", "hash_senha", "OPERADOR");
        SessionManager.setUsuarioLogado(usuario);

        // Sessao acabou de ser criada, nao pode estar expirada para timeout de 1000ms
        assertFalse(SessionManager.isSessionExpirada(1000L));

        // Aguarda 60ms e testa timeout minusculo de 20ms
        Thread.sleep(60);
        assertTrue(SessionManager.isSessionExpirada(20L), "Deve detectar expiracao quando inatividade exceder o limite.");

        // Ao registrar atividade, o timestamp e renovado
        SessionManager.registrarAtividade();
        assertFalse(SessionManager.isSessionExpirada(1000L), "Apos registrar atividade, nao deve estar expirada.");
    }
}