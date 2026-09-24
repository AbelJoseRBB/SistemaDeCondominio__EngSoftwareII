package com.condomanager.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Utilitario de conexao com o banco de dados.
 * Utiliza o padrao Singleton para manter uma unica conexao ativa.
 * As credenciais sao lidas do arquivo db.properties (fora do controle de versao).
 */
public class DBConnection {

    private static final String PROPERTIES_FILE = "/db.properties";

    private static String url;
    private static String user;
    private static String password;

    private static Connection instance;

    static {
        carregarPropriedades();
    }

    private DBConnection() {}

    private static void carregarConfiguracoes() {
        try (InputStream is = DBConnection.class.getResourceAsStream("/database.properties")) {
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                if (props.getProperty("db.url") != null && !props.getProperty("db.url").isBlank()) {
                    url = props.getProperty("db.url").trim();
                }
                if (props.getProperty("db.user") != null && !props.getProperty("db.user").isBlank()) {
                    user = props.getProperty("db.user").trim();
                }
                if (props.getProperty("db.password") != null) {
                    password = props.getProperty("db.password").trim();
                }
            }
        } catch (Exception e) {
            System.err.println("Aviso: Nao foi possivel carregar database.properties: " + e.getMessage());
        }

        String envPass = System.getenv("DB_PASSWORD");
        if (envPass != null && !envPass.isBlank()) {
            password = envPass;
        }
    }

    /**
     * Retorna a conexao ativa com o banco. Cria uma nova se nao existir ou estiver fechada.
     *
     * @throws RuntimeException se o db.properties nao for encontrado ou a conexao falhar
     */
    public static Connection getConnection() throws SQLException {
        if (instance == null || instance.isClosed()) {
            instance = DriverManager.getConnection(url, user, password);
        }
        return instance;
    }

    /**
     * Fecha a conexao com o banco.
     */
    public static void closeConnection() {
        try {
            if (instance != null && !instance.isClosed()) {
                instance.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Carrega as propriedades de conexao do arquivo db.properties.
     * O arquivo deve estar em src/main/resources/db.properties.
     * Copie db.properties.example e preencha com suas credenciais locais.
     */
    private static void carregarPropriedades() {
        Properties props = new Properties();

        InputStream input = DBConnection.class.getResourceAsStream(PROPERTIES_FILE);
        if (input == null) {
            input = DBConnection.class.getResourceAsStream("/database.properties");
        }

        if (input != null) {
            try (InputStream is = input) {
                props.load(is);
                url      = props.getProperty("db.url", "jdbc:mysql://localhost:3306/condominio_db");
                user     = props.getProperty("db.user", "root");
                password = props.getProperty("db.password", "root");
            } catch (IOException e) {
                throw new RuntimeException("Erro ao carregar propriedades de conexao: " + e.getMessage(), e);
            }
        } else {
            url      = "jdbc:mysql://localhost:3306/condominio_db";
            user     = "root";
            password = "root";
        }

        String envUrl = System.getenv("DB_URL");
        if (envUrl != null && !envUrl.isBlank()) {
            url = envUrl.trim();
        } else {
            String envHost = System.getenv("DB_HOST");
            if (envHost != null && !envHost.isBlank()) {
                String envPort = System.getenv("DB_PORT");
                String envName = System.getenv("DB_NAME");
                String port = (envPort != null && !envPort.isBlank()) ? envPort.trim() : "3306";
                String name = (envName != null && !envName.isBlank()) ? envName.trim() : "condominio_db";
                url = "jdbc:mysql://" + envHost.trim() + ":" + port + "/" + name + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            }
        }

        String envUser = System.getenv("DB_USER");
        if (envUser != null && !envUser.isBlank()) {
            user = envUser.trim();
        }
        String envPass = System.getenv("DB_PASSWORD");
        if (envPass != null && !envPass.isBlank()) {
            password = envPass.trim();
        }
    }
}