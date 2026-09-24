package com.condomanager.util;

import com.condomanager.model.Usuario;

/**
 * Gerencia a sessao do usuario autenticado.
 * Mantem os dados do usuario logado durante a execucao do sistema.
 */
public class SessionManager {

    private static Usuario usuarioLogado;

    private SessionManager() {}

    public static Usuario getUsuarioLogado() {
        return usuarioLogado;
    }

    public static void setUsuarioLogado(Usuario usuario) {
        usuarioLogado = usuario;
    }

    public static void encerrarSessao() {
        usuarioLogado = null;
    }

    public static boolean isLogado() {
        return usuarioLogado != null;
    }
}

