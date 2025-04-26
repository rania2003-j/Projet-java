package com.example.livraison.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDatabase {
    private static final String URL = "jdbc:mysql://localhost:3307/mydatabase";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private static Connection connection;

    private MyDatabase() {
    }

    public static Connection getConnection() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Connexion réussie à la base de données.");
            } catch (SQLException e) {
                System.err.println(" Erreur de connexion : " + e.getMessage());
            }
        }
        return connection;
    }
}
