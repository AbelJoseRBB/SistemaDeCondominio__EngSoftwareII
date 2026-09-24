package com.condomanager.util;

import com.condomanager.model.Usuario;

/**
 * Gerencia a sessao do usuario autenticado.
 * Mantem os dados do usuario logado e o timestamp da ultima atividade,
 * usado pelo InactivityWatcher para encerrar sessoes ociosas.
 */
public class SessionManager {

    private static Usuario usuarioLogado;
    private static long lastActivityTime;

    private SessionManager() {}

    public static Usuario getUsuarioLogado() {
        return usuarioLogado;
    }

    public static void setUsuarioLogado(Usuario usuario) {
        usuarioLogado = usuario;
        registrarAtividade(); // inicia o contador ao fazer login
    }

    public static void encerrarSessao() {
        usuarioLogado = null;
        lastActivityTime = 0;
    }

    public static boolean isLogado() {
        return usuarioLogado != null;
    }

    /**
     * Atualiza o timestamp da ultima atividade do usuario.
     * Deve ser chamado em eventos de mouse e teclado na tela principal.
     */
    public static void registrarAtividade() {
        lastActivityTime = System.currentTimeMillis();
    }

    /**
     * Verifica se a sessao esta expirada por inatividade.
     *
     * @param timeoutMs tempo limite de inatividade em milissegundos
     * @return true se o tempo de inatividade ultrapassou o limite
     */
    public static boolean isSessionExpirada(long timeoutMs) {
        if (lastActivityTime == 0) return false;
        return (System.currentTimeMillis() - lastActivityTime) > timeoutMs;
    }
}

