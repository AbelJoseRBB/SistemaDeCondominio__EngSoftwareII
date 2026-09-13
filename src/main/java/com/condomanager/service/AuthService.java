package com.condomanager.service;

import com.condomanager.dao.UsuarioDAO;
import com.condomanager.model.Usuario;
import com.condomanager.util.SessionManager;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Servico de autenticacao.
 * Gerencia login/logout e verificacao de senhas com BCrypt.
 */
public class AuthService {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    /**
     * Autentica o usuario. Retorna true se o login for bem-sucedido.
     */
    public boolean autenticar(String login, String senha) {
        Usuario usuario = usuarioDAO.buscarPorLogin(login);
        if (usuario == null) {
            return false;
        }
        // Verifica a senha usando BCrypt
        if (BCrypt.checkpw(senha, usuario.getSenhaHash())) {
            SessionManager.setUsuarioLogado(usuario);
            return true;
        }
        return false;
    }

    public void logout() {
        SessionManager.encerrarSessao();
    }

    /**
     * Gera o hash BCrypt de uma senha em texto plano.
     * Use isto ao cadastrar ou alterar senhas.
     */
    public String gerarHash(String senha) {
        return BCrypt.hashpw(senha, BCrypt.gensalt());
    }
}

