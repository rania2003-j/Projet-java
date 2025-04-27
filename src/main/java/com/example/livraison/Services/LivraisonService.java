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

    // Méthode pour marquer le QR code comme utilisé
    public boolean markQRCodeAsUsed(int livraisonId) {
        String sql = "UPDATE livraison SET qr_used = TRUE WHERE id = ? AND qr_used = FALSE";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, livraisonId);
            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0; // Retourne true si le QR code a été marqué comme utilisé, sinon false
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du QR code : " + e.getMessage());
            return false;
        }
    }

    // Méthode pour vérifier si le QR code a déjà été utilisé
    public boolean isQRCodeUsed(int livraisonId) {
        String sql = "SELECT qr_used FROM livraison WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, livraisonId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("qr_used");
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification du QR code : " + e.getMessage());
        }
        return false;
    }


    public void scanQRCode(int livraisonId) {
        if (isQRCodeUsed(livraisonId)) {
            System.out.println("QR Code déjà utilisé");

        } else {
            boolean success = markQRCodeAsUsed(livraisonId);
            if (success) {
                System.out.println("QR Code marqué comme utilisé");

            } else {
                System.out.println("Erreur lors de la mise à jour du QR code");
            }
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


            String transporteurName = getNomCompletTransporteur(livraison.getTransporteurId());
            String voitureModel = getModeleVoiture(livraison.getVoitureId());

            String qrContent = "=== Livraison ===\n"
                    + "Transporteur : " + transporteurName + "\n"
                    + "Voiture : " + voitureModel + "\n"
                    + "État : " + livraison.getEtatLivraison() + "\n"
                    + "Date : " + livraison.getDateLivraison();

            stmt.setString(6, qrContent);
            stmt.setString(7, token);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        int newId = keys.getInt(1);
                        File qrDir = new File("livraison/qrcodes");
                        if (!qrDir.exists()) qrDir.mkdirs();
                        String qrPath = "livraison/qrcodes/livraison_" + newId + ".png";


                        QRCodeGenerator.generateQRCodeImage(qrContent, qrPath);
                        System.out.println("Livraison #" + newId + " créée avec QR Code (infos livraison) : " + qrPath);
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
        String sql = "SELECT id, nom, prenom, is_disponible FROM transporteur";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Transporteur t = new Transporteur(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getBoolean("is_disponible")
                );
                list.add(t);
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
        if (!doesTransporteurExist(livraison.getTransporteurId())) {
            throw new SQLException("Transporteur avec l'ID " + livraison.getTransporteurId() + " n'existe pas.");
        }
        if (!doesVoitureExist(livraison.getVoitureId())) {
            throw new SQLException("Voiture avec l'ID " + livraison.getVoitureId() + " n'existe pas.");
        }

        String sql = "UPDATE livraison SET transporteur_id = ?, voiture_id = ?, etat_livraison = ?, date_livraison = ?, qr_code_data = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, livraison.getTransporteurId());
            stmt.setInt(2, livraison.getVoitureId());
            stmt.setString(3, livraison.getEtatLivraison());
            stmt.setDate(4, Date.valueOf(livraison.getDateLivraison()));
            stmt.setString(5, livraison.getQrCodeData());
            stmt.setInt(6, livraison.getId());
            stmt.executeUpdate();
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
            e.printStackTrace();
        }
        return "Nom Inconnu";
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
            e.printStackTrace();
        }
        return "Modèle Inconnu";
    }


    public boolean deleteLivraison(int livraisonId) {
        String sql = "DELETE FROM livraison WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, livraisonId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de la livraison : " + e.getMessage());
            return false;
        }
    }
}

















