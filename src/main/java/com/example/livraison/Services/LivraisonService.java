package com.example.livraison.Services;

import com.example.livraison.Models.Livraison;
import com.example.livraison.Models.Transporteur;
import com.example.livraison.Models.Voiture;
import com.example.livraison.utils.QRCodeGenerator;
import com.google.zxing.WriterException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LivraisonService {
    private Connection connection;

    public LivraisonService() {
        String url = "jdbc:mysql://localhost:3307/mydatabase";
        String username = "root";
        String password = "";
        try {
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("Connexion à la base de données réussie !");
        } catch (SQLException e) {
            System.err.println("Erreur de connexion : " + e.getMessage());
        }
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Connexion fermée avec succès.");
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture de la connexion : " + e.getMessage());
            }
        }
    }

    public boolean doesTransporteurExist(int id) {
        String sql = "SELECT id FROM transporteur WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification du transporteur : " + e.getMessage());
            return false;
        }
    }

    public boolean doesVoitureExist(int id) {
        String sql = "SELECT id FROM voiture WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification de la voiture : " + e.getMessage());
            return false;
        }
    }

    public void createLivraison(Livraison livraison) {
        String token = UUID.randomUUID().toString();
        String sql = "INSERT INTO livraison (transporteur_id, voiture_id, etat_livraison, date_livraison, qr_used, qr_code_data, qr_token) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, livraison.getTransporteurId());
            stmt.setInt(2, livraison.getVoitureId());
            stmt.setString(3, livraison.getEtatLivraison());
            stmt.setDate(4, Date.valueOf(livraison.getDateLivraison()));
            stmt.setBoolean(5, false);
            stmt.setString(6, livraison.generateQRCodeContent());
            stmt.setString(7, token);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        int newId = keys.getInt(1);
                        File qrDir = new File("livraison/qrcodes");
                        if (!qrDir.exists()) qrDir.mkdirs();
                        String qrPath = "livraison/qrcodes/livraison_" + newId + ".png";

                        String host;
                        try {
                            host = InetAddress.getLocalHost().getHostAddress();
                        } catch (UnknownHostException e) {
                            host = "localhost";
                        }
                        String urlScan = "http://" + host + ":8080/api/scan?token=" + token;
                        QRCodeGenerator.generateQRCodeImage(urlScan, qrPath);
                        System.out.println("Livraison #" + newId + " créée, QR Code généré : " + qrPath);
                    }
                }
            }
        } catch (SQLException | IOException | WriterException e) {
            e.printStackTrace();
        }
    }

    public ObservableList<Livraison> getAllLivraisons() {
        ObservableList<Livraison> list = FXCollections.observableArrayList();
        String sql = "SELECT l.id, l.transporteur_id, l.voiture_id, l.etat_livraison, l.date_livraison, l.qr_used, l.qr_code_data, l.qr_token, t.nom AS transporteur_nom, v.model AS voiture_modele "
                + "FROM livraison l "
                + "LEFT JOIN transporteur t ON l.transporteur_id = t.id "
                + "LEFT JOIN voiture v ON l.voiture_id = v.id";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Livraison l = new Livraison(
                        rs.getInt("id"),
                        rs.getInt("transporteur_id"),
                        rs.getInt("voiture_id"),
                        rs.getString("etat_livraison"),
                        rs.getDate("date_livraison").toLocalDate(),
                        rs.getBoolean("qr_used"),
                        rs.getString("qr_code_data")
                );
                l.setNomTransporteur(rs.getString("transporteur_nom"));
                l.setModeleVoiture(rs.getString("voiture_modele"));
                l.setQrCodeData(rs.getString("qr_token"));
                list.add(l);
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération livraisons : " + e.getMessage());
        }
        return list;
    }

    public List<Transporteur> getAllTransporteurs() {
        List<Transporteur> list = new ArrayList<>();
        String sql = "SELECT id, nom, prenom, isDisponible FROM transporteur";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new Transporteur(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getBoolean("isDisponible")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Voiture> getAllVoitures() {
        List<Voiture> list = new ArrayList<>();
        String sql = "SELECT id, model, matricule, capacite FROM voiture";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new Voiture(
                        rs.getInt("id"),
                        rs.getString("model"),
                        rs.getString("matricule"),
                        rs.getInt("capacite")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void updateLivraison(Livraison livraison) throws SQLException {
        if (!doesTransporteurExist(livraison.getTransporteurId()))
            throw new SQLException("Transporteur ID invalide !");
        if (!doesVoitureExist(livraison.getVoitureId()))
            throw new SQLException("Voiture ID invalide !");
        String sql = "UPDATE livraison SET transporteur_id = ?, voiture_id = ?, etat_livraison = ?, date_livraison = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, livraison.getTransporteurId());
            stmt.setInt(2, livraison.getVoitureId());
            stmt.setString(3, livraison.getEtatLivraison());
            stmt.setDate(4, Date.valueOf(livraison.getDateLivraison()));
            stmt.setInt(5, livraison.getId());
            if (stmt.executeUpdate() == 0) throw new SQLException("Aucune ligne mise à jour.");
        }
    }

    public void deleteLivraison(int id) throws SQLException {
        String sql = "DELETE FROM livraison WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            if (stmt.executeUpdate() == 0) throw new SQLException("Aucune ligne supprimée.");
        }
    }

    public String getNomCompletTransporteur(int transporteurId) {
        String sql = "SELECT nom, prenom FROM transporteur WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, transporteurId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("nom") + " " + rs.getString("prenom");
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération transporteur : " + e.getMessage());
        }
        return "Transporteur inconnu";
    }

    public String getModeleVoiture(int voitureId) {
        String sql = "SELECT model FROM voiture WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, voitureId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("model");
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération voiture : " + e.getMessage());
        }
        return "Modèle inconnu";
    }
}

















