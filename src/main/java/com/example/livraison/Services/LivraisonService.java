package com.example.livraison.Services;

import com.example.livraison.Models.Livraison;
import com.example.livraison.utils.QRCodeGenerator;
import com.google.zxing.WriterException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.UUID;

public class LivraisonService {
    private Connection connection;

    public LivraisonService() {
        try {
            String url = "jdbc:mysql://localhost:3307/mydatabase";
            String username = "root";
            String password = "";
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

        String sql = "INSERT INTO livraison " +
                "(transporteur_id, voiture_id, etat_livraison, date_livraison, qr_used, qr_code_data, qr_token) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, livraison.getTransporteurId());
            stmt.setInt(2, livraison.getVoitureId());
            stmt.setString(3, livraison.getEtatLivraison());
            stmt.setDate(4, Date.valueOf(livraison.getDateLivraison()));
            stmt.setBoolean(5, false);

            String qrContent = livraison.generateQRCodeContent();
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

                        String url = "https://<TON_DOMAINE>:8080/api/scan?token=" + token;
                        QRCodeGenerator.generateQRCodeImage(url, qrPath);

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
        String sql = "SELECT l.id, l.transporteur_id, l.voiture_id, l.etat_livraison, " +
                "l.date_livraison, l.qr_used, l.qr_code_data, l.qr_token, " +
                "t.nom AS transporteur_nom, v.model AS voiture_modele " +
                "FROM livraison l " +
                "LEFT JOIN transporteur t ON l.transporteur_id = t.id " +
                "LEFT JOIN voiture v ON l.voiture_id = v.id";
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

    public Livraison getLivraisonById(int id) {
        String sql = "SELECT * FROM livraison WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Livraison l = new Livraison(
                            rs.getInt("id"),
                            rs.getInt("transporteur_id"),
                            rs.getInt("voiture_id"),
                            rs.getString("etat_livraison"),
                            rs.getDate("date_livraison").toLocalDate(),
                            rs.getBoolean("qr_used"),
                            rs.getString("qr_code_data")
                    );
                    l.setNomTransporteur(getNomCompletTransporteur(l.getTransporteurId()));
                    l.setModeleVoiture(getModeleVoiture(l.getVoitureId()));
                    return l;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de la livraison : " + e.getMessage());
        }
        return null;
    }

    public Livraison findByToken(String token) {
        String sql = "SELECT * FROM livraison WHERE qr_token = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Livraison l = new Livraison(
                        rs.getInt("id"),
                        rs.getInt("transporteur_id"),
                        rs.getInt("voiture_id"),
                        rs.getString("etat_livraison"),
                        rs.getDate("date_livraison").toLocalDate(),
                        rs.getBoolean("qr_used"),
                        rs.getString("qr_code_data")
                );
                l.setNomTransporteur(getNomCompletTransporteur(l.getTransporteurId()));
                l.setModeleVoiture(getModeleVoiture(l.getVoitureId()));
                return l;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void markTokenAsUsed(int livraisonId) {
        String sql = "UPDATE livraison SET qr_used = TRUE WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, livraisonId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Livraison handleQrScan(String qrData) {
        String select = "SELECT id, qr_used FROM livraison WHERE qr_code_data = ?";
        try (PreparedStatement stmt = connection.prepareStatement(select)) {
            stmt.setString(1, qrData);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("QR Code invalide");
                    return null;
                }
                int id = rs.getInt("id");
                boolean used = rs.getBoolean("qr_used");
                if (used) {
                    System.out.println("QR Code déjà utilisé");
                    return null;
                }

                try (PreparedStatement upd = connection.prepareStatement(
                        "UPDATE livraison SET qr_used = TRUE WHERE id = ?")) {
                    upd.setInt(1, id);
                    upd.executeUpdate();
                }
                return getLivraisonById(id);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du scan du QR : " + e.getMessage());
        }
        return null;
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
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Livraison mise à jour !");
            } else {
                throw new SQLException("Aucune ligne mise à jour.");
            }
        }
    }

    public void deleteLivraison(int id) throws SQLException {
        String sql = "DELETE FROM livraison WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Livraison supprimée !");
            } else {
                throw new SQLException("Aucune ligne supprimée.");
            }
        }
    }

    public Livraison findByTransporteurNameAndVoitureModel(String transporteurNom, String voitureModele) {
        String query = "SELECT l.id, l.transporteur_id, l.voiture_id, l.etat_livraison, " +
                "l.date_livraison, l.qr_used, l.qr_code_data " +
                "FROM livraison l " +
                "JOIN transporteur t ON l.transporteur_id = t.id " +
                "JOIN voiture v ON l.voiture_id = v.id " +
                "WHERE t.nom = ? AND v.model = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, transporteurNom);
            stmt.setString(2, voitureModele);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Livraison l = new Livraison(
                            rs.getInt("id"),
                            rs.getInt("transporteur_id"),
                            rs.getInt("voiture_id"),
                            rs.getString("etat_livraison"),
                            rs.getDate("date_livraison").toLocalDate(),
                            rs.getBoolean("qr_used"),
                            rs.getString("qr_code_data")
                    );
                    l.setNomTransporteur(getNomCompletTransporteur(l.getTransporteurId()));
                    l.setModeleVoiture(getModeleVoiture(l.getVoitureId()));
                    return l;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche de la livraison : " + e.getMessage());
        }
        return null;
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
            System.err.println("Erreur lors de la récupération du transporteur : " + e.getMessage());
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
            System.err.println("Erreur lors de la récupération de la voiture : " + e.getMessage());
        }
        return "Modèle inconnu";
    }
}















