package com.condomanager.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utilitario de conexao com o banco de dados.
 * Utiliza o padrao Singleton para manter uma unica conexao ativa.
 */
public class DBConnection {

    // TODO: Mova estas configuracoes para um arquivo .properties
    private static final String URL      = "jdbc:mysql://localhost:3306/condominio_db";
    private static final String USER     = "root";
    private static final String PASSWORD = "sua_senha_aqui";

    private static Connection instance;

    private DBConnection() {}

    /**
     * Retorna a conexao ativa. Cria uma nova se nao existir ou estiver fechada.
     */
    public static Connection getConnection() throws SQLException {
        if (instance == null || instance.isClosed()) {
            instance = DriverManager.getConnection(URL, USER, PASSWORD);
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

