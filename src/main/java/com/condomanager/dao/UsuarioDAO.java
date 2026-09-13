package com.condomanager.dao;

import com.condomanager.model.Usuario;

/**
 * DAO para operacoes de banco de dados relacionadas a Usuario.
 */
public class UsuarioDAO {

    public Usuario buscarPorLogin(String login) {
        // TODO: SELECT * FROM usuario WHERE login=?
        return null;
    }

    public void salvar(Usuario usuario) {
        // TODO: INSERT INTO usuario (nome, login, senha_hash, perfil) VALUES (?, ?, ?, ?)
    }

    public void atualizarSenha(int id, String novaSenhaHash) {
        // TODO: UPDATE usuario SET senha_hash=? WHERE id=?
    }
}

