package com.example.livraison.Services;

import com.example.livraison.Models.Voiture;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VoitureService {

    private Connection connection;

    public VoitureService() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3307/mydatabase", "root", "");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Erreur de connexion à la base de données : " + e.getMessage());
        }
    }

    public List<Voiture> getAllVoitures() {
        List<Voiture> voitures = new ArrayList<>();
        String query = "SELECT * FROM voiture";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Voiture voiture = new Voiture(
                        rs.getInt("id"),
                        rs.getString("model"),
                        rs.getString("matricule"),
                        rs.getInt("capacite")
                );
                voitures.add(voiture);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Erreur lors de la récupération des voitures : " + e.getMessage());
        }
        return voitures;
    }

    public boolean addVoiture(Voiture voiture) {
        String sql = "INSERT INTO voiture (model, matricule, capacite) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, voiture.getModel());
            ps.setString(2, voiture.getMatricule());
            ps.setInt(3, voiture.getCapacite());

            int rowsInserted = ps.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Erreur lors de l'ajout de la voiture : " + e.getMessage());
        }
        return false;
    }

    public boolean updateVoiture(Voiture voiture) {
        String sql = "UPDATE voiture SET model=?, matricule=?, capacite=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, voiture.getModel());
            ps.setString(2, voiture.getMatricule());
            ps.setInt(3, voiture.getCapacite());
            ps.setInt(4, voiture.getId());

            int rowsUpdated = ps.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Erreur lors de la mise à jour de la voiture : " + e.getMessage());
        }
        return false;
    }

    public boolean deleteVoiture(int id) {
        String sql = "DELETE FROM voiture WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);

            int rowsDeleted = ps.executeUpdate();
            return rowsDeleted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Erreur lors de la suppression de la voiture : " + e.getMessage());
        }
        return false;
    }
}



