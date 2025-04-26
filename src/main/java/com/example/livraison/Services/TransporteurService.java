package com.example.livraison.Services;

import com.example.livraison.Models.Transporteur;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransporteurService {

    private static final String URL = "jdbc:mysql://localhost:3307/mydatabase";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private Connection connection;

    public TransporteurService() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addTransporteur(Transporteur transporteur) {
        String query = "INSERT INTO transporteur (id, nom, prenom, is_disponible) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, transporteur.getId());
            stmt.setString(2, transporteur.getNom());
            stmt.setString(3, transporteur.getPrenom());
            stmt.setBoolean(4, transporteur.is_disponible());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isIdExist(int id) {
        String query = "SELECT COUNT(*) FROM transporteur WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                return count > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Transporteur> getAllTransporteurs() {
        List<Transporteur> transporteurs = new ArrayList<>();
        String query = "SELECT * FROM transporteur";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String nom = rs.getString("nom");
                String prenom = rs.getString("prenom");
                boolean isDisponible = rs.getBoolean("is_disponible");
                transporteurs.add(new Transporteur(id, nom, prenom, isDisponible));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transporteurs;
    }

    public void updateTransporteur(Transporteur transporteur) {
        String query = "UPDATE transporteur SET nom = ?, prenom = ?, is_disponible = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, transporteur.getNom());
            stmt.setString(2, transporteur.getPrenom());
            stmt.setBoolean(3, transporteur.is_disponible());
            stmt.setInt(4, transporteur.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteTransporteur(int id) {
        String query = "DELETE FROM transporteur WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Transporteur getTransporteurById(int id) {
        String query = "SELECT * FROM transporteur WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String nom = rs.getString("nom");
                String prenom = rs.getString("prenom");
                boolean isDisponible = rs.getBoolean("is_disponible");
                return new Transporteur(id, nom, prenom, isDisponible);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}






