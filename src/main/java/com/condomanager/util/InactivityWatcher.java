package com.condomanager.util;

import com.condomanager.service.AuthService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.util.Duration;

/**
 * Monitora a inatividade do usuario e encerra a sessao automaticamente
 * apos o tempo configurado sem interacao.
 *
 * Uso:
 *   InactivityWatcher watcher = new InactivityWatcher();
 *   watcher.iniciar(() -> NavigationUtil.navegar(...));
 *
 * Registrar atividade nos listeners da cena:
 *   scene.setOnMouseMoved(e  -> SessionManager.registrarAtividade());
 *   scene.setOnKeyPressed(e  -> SessionManager.registrarAtividade());
 */
public class InactivityWatcher {

    /** Timeout padrao: 15 minutos em milissegundos. */
    public static final long TIMEOUT_PADRAO_MS = 15 * 60 * 1000L;

    /** Intervalo de checagem: 1 minuto. */
    private static final double INTERVALO_CHECAGEM_SEGUNDOS = 60.0;

    private final long timeoutMs;
    private final AuthService authService = new AuthService();
    private Timeline timeline;

    public InactivityWatcher() {
        this(TIMEOUT_PADRAO_MS);
    }

    public InactivityWatcher(long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    /**
     * Inicia o monitoramento de inatividade.
     *
     * @param aoExpirar Runnable executado na thread JavaFX quando a sessao expirar.
     *                  Use para redirecionar para a tela de Login.
     */
    public void iniciar(Runnable aoExpirar) {
        timeline = new Timeline(new KeyFrame(
            Duration.seconds(INTERVALO_CHECAGEM_SEGUNDOS),
            event -> checarInatividade(aoExpirar)
        ));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    /**
     * Para o monitoramento (chamar ao fazer logout manual ou ao destruir a tela).
     */
    public void parar() {
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
    }

    private void checarInatividade(Runnable aoExpirar) {
        if (!SessionManager.isLogado()) {
            parar();
            return;
        }

        if (SessionManager.isSessionExpirada(timeoutMs)) {
            parar();
            authService.logout();
            // Garante execucao na thread do JavaFX
            Platform.runLater(aoExpirar);
        }
    }
}
