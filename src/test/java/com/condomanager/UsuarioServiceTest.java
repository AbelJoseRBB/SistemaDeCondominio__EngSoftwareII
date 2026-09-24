package com.condomanager;

import com.condomanager.dao.UsuarioDAO;
import com.condomanager.model.Usuario;
import com.condomanager.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitarios do UsuarioService")
class UsuarioServiceTest {

    private FakeUsuarioDAO fakeDAO;
    private UsuarioService usuarioService;

    // Fake DAO para isolar o servico sem depender de banco de dados ativo no teste
    static class FakeUsuarioDAO extends UsuarioDAO {
        final Map<String, Usuario> porLogin = new HashMap<>();
        final Map<String, Usuario> porEmail = new HashMap<>();

        @Override
        public Usuario buscarPorLogin(String login) {
            return porLogin.get(login);
        }

        @Override
        public Usuario buscarPorEmail(String email) {
            return porEmail.get(email);
        }

        @Override
        public boolean existeEmail(String email) {
            return porEmail.containsKey(email);
        }

        @Override
        public boolean existeLogin(String login) {
            return porLogin.containsKey(login);
        }

        @Override
        public void salvar(Usuario usuario) {
            porLogin.put(usuario.getLogin(), usuario);
            porEmail.put(usuario.getEmail(), usuario);
        }
    }

    @BeforeEach
    void setUp() {
        fakeDAO = new FakeUsuarioDAO();
        usuarioService = new UsuarioService(fakeDAO);
    }

    @Test
    @DisplayName("Deve cadastrar usuario com sucesso quando dados forem validos")
    void deveCadastrarUsuarioComSucesso() {
        Usuario criado = usuarioService.cadastrar(
            "Carlos Silva",
            "carlos.silva",
            "SenhaForte123",
            "carlos.silva@email.com",
            "OPERADOR"
        );

        assertNotNull(criado);
        assertEquals("Carlos Silva", criado.getNome());
        assertEquals("carlos.silva", criado.getLogin());
        assertEquals("carlos.silva@email.com", criado.getEmail());
        assertEquals("OPERADOR", criado.getPerfil());
        assertTrue(BCrypt.checkpw("SenhaForte123", criado.getSenhaHash()));
        assertTrue(fakeDAO.existeLogin("carlos.silva"));
        assertTrue(fakeDAO.existeEmail("carlos.silva@email.com"));
    }

    @Test
    @DisplayName("Deve falhar ao cadastrar com campos obrigatorios vazios ou nulos")
    void deveFalharQuandoCamposObrigatoriosForemVazios() {
        // Nome vazio
        assertThrows(IllegalArgumentException.class, () ->
            usuarioService.cadastrar("", "user", "senha123", "user@email.com")
        );
        // Login vazio
        assertThrows(IllegalArgumentException.class, () ->
            usuarioService.cadastrar("Nome", "   ", "senha123", "user@email.com")
        );
        // Senha vazia
        assertThrows(IllegalArgumentException.class, () ->
            usuarioService.cadastrar("Nome", "user", "", "user@email.com")
        );
        // Email vazio
        assertThrows(IllegalArgumentException.class, () ->
            usuarioService.cadastrar("Nome", "user", "senha123", "   ")
        );
    }

    @Test
    @DisplayName("Deve falhar ao cadastrar com e-mail em formato invalido")
    void deveFalharQuandoEmailForInvalido() {
        assertThrows(IllegalArgumentException.class, () ->
            usuarioService.cadastrar("Nome", "user", "senha123", "email_sem_arroba")
        );
        assertThrows(IllegalArgumentException.class, () ->
            usuarioService.cadastrar("Nome", "user", "senha123", "email@")
        );
    }

    @Test
    @DisplayName("Deve validar unicidade e bloquear cadastro de e-mail duplicado")
    void deveBloquearCadastroComEmailDuplicado() {
        usuarioService.cadastrar("Usuario Um", "user1", "senha123", "duplicado@email.com");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            usuarioService.cadastrar("Usuario Dois", "user2", "senha456", "duplicado@email.com")
        );

        assertTrue(ex.getMessage().contains("e-mail informado ja esta cadastrado"));
    }

    @Test
    @DisplayName("Deve validar unicidade e bloquear cadastro de login duplicado")
    void deveBloquearCadastroComLoginDuplicado() {
        usuarioService.cadastrar("Usuario Um", "mesmo.login", "senha123", "um@email.com");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            usuarioService.cadastrar("Usuario Dois", "mesmo.login", "senha456", "dois@email.com")
        );

        assertTrue(ex.getMessage().contains("nome de usuario informado ja esta em uso"));
    }

    @Test
    @DisplayName("Deve aplicar perfil default OPERADOR quando perfil for omitido")
    void deveAplicarPerfilDefault() {
        Usuario criado = usuarioService.cadastrar(
            "Maria Oliveira",
            "maria",
            "123456",
            "maria@email.com"
        );

        assertEquals("OPERADOR", criado.getPerfil());
    }
}