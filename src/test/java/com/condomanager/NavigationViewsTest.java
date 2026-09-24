package com.condomanager;

import com.condomanager.model.Usuario;
import com.condomanager.util.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Testes de Integridade dos Arquivos FXML do Sistema")
class NavigationViewsTest {

    private static boolean jfxInitialized = false;

    private static final List<String> TODOS_FXMLS = List.of(
        "/fxml/MainLayout.fxml",
        "/fxml/Dashboard.fxml",
        "/fxml/Login.fxml",
        "/fxml/unidade/UnidadeList.fxml",
        "/fxml/reserva/ReservaList.fxml",
        "/fxml/ocorrencia/OcorrenciaList.fxml",
        "/fxml/morador/MoradorList.fxml",
        "/fxml/taxa/TaxaList.fxml",
        "/fxml/manutencao/ManutencaoList.fxml",
        "/fxml/veiculo/VeiculoList.fxml",
        "/fxml/relatorio/RelatorioList.fxml",
        "/fxml/usuario/UsuarioCadastro.fxml"
    );

    @BeforeAll
    static void initJavaFX() {
        try {
            Platform.startup(() -> {});
            jfxInitialized = true;
        } catch (IllegalStateException e) {
            jfxInitialized = true;
        } catch (Exception e) {
            System.err.println("Aviso: Ambiente grafico headless, pulando inicializacao direta: " + e.getMessage());
        }
    }

    @BeforeEach
    void setUp() {
        // Define usuario autenticado para os testes de layout
        SessionManager.setUsuarioLogado(new Usuario(1, "Administrador", "admin", "hash", "ADMIN"));
    }

    @AfterEach
    void tearDown() {
        SessionManager.encerrarSessao();
    }

    @Test
    @DisplayName("Valida que todos os 11 arquivos FXML do sistema existem e sao XMLs validos")
    void testXMLValidoDeTodosOsFXMLs() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();

        for (String fxmlPath : TODOS_FXMLS) {
            try (InputStream is = getClass().getResourceAsStream(fxmlPath)) {
                assertNotNull(is, "Arquivo FXML nao encontrado no classpath: " + fxmlPath);
                org.w3c.dom.Document doc = builder.parse(is);
                assertNotNull(doc, "Documento XML parseado nao deve ser nulo: " + fxmlPath);
            }
        }
    }

    @Test
    @DisplayName("Valida que o MainLayout.fxml e carregado com sucesso pelo FXMLLoader")
    void testCarregamentoMainLayout() throws Exception {
        if (!jfxInitialized) return;

        URL resource = getClass().getResource("/fxml/MainLayout.fxml");
        assertNotNull(resource);

        CompletableFuture<Object> future = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(resource);
                Object root = loader.load();
                future.complete(root);
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });

        Object root = future.get(10, TimeUnit.SECONDS);
        assertNotNull(root, "MainLayout deve ser instanciado com sucesso");
    }

    @Test
    @DisplayName("Valida que o Dashboard.fxml e carregado com sucesso pelo FXMLLoader")
    void testCarregamentoDashboard() throws Exception {
        if (!jfxInitialized) return;

        URL resource = getClass().getResource("/fxml/Dashboard.fxml");
        assertNotNull(resource);

        CompletableFuture<Object> future = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(resource);
                Object root = loader.load();
                future.complete(root);
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });

        Object root = future.get(10, TimeUnit.SECONDS);
        assertNotNull(root, "Dashboard deve ser instanciado com sucesso");
    }

    @Test
    @DisplayName("Valida que o UsuarioCadastro.fxml e carregado com sucesso pelo FXMLLoader")
    void testCarregamentoUsuarioCadastro() throws Exception {
        if (!jfxInitialized) return;

        URL resource = getClass().getResource("/fxml/usuario/UsuarioCadastro.fxml");
        assertNotNull(resource);

        CompletableFuture<Object> future = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(resource);
                Object root = loader.load();
                future.complete(root);
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });

        Object root = future.get(10, TimeUnit.SECONDS);
        assertNotNull(root, "UsuarioCadastro deve ser instanciado com sucesso");
    }
}