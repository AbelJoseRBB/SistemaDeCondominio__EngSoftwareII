package com.condomanager.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Utilitario de conexao com o banco de dados.
 * Utiliza o padrao Singleton para manter uma unica conexao ativa.
 * Suporta configuracao via arquivo database.properties (ignorado pelo git)
 * ou variaveis de ambiente, com fallback seguro para os valores padrao.
 */
public class DBConnection {

    private static String url      = "jdbc:mysql://localhost:3306/condominio_db";
    private static String user     = "root";
    private static String password = "sua_senha_aqui";

    private static Connection instance;

    static {
        carregarConfiguracoes();
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
     * Retorna a conexao ativa. Cria uma nova se nao existir ou estiver fechada.
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
}