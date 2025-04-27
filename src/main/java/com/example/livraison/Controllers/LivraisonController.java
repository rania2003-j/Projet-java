package com.example.livraison.Controllers;

import com.example.livraison.Models.Livraison;
import com.example.livraison.Models.Transporteur;
import com.example.livraison.Models.Voiture;
import com.example.livraison.Services.LivraisonService;
import com.example.livraison.utils.QRCodeGenerator;
import com.google.zxing.WriterException;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class LivraisonController {

    private final LivraisonService livraisonService = new LivraisonService();
    private Livraison selectedLivraison;

    @FXML private ComboBox<Transporteur> transporteurComboBox;
    @FXML private ComboBox<Voiture>     voitureComboBox;
    @FXML private ComboBox<String>     etatLivraisonComboBox;
    @FXML private DatePicker           dateLivraisonField;
    @FXML private Button               addButton;
    @FXML private Button               updateButton;
    @FXML private Button               deleteButton;
    @FXML private Label                errorMessage;
    @FXML private Label                successLabel;
    @FXML private FlowPane             cardsContainer;
    @FXML private VBox                 livraisonDetailsBox;
    @FXML private Label                lblId;
    @FXML private Label                lblTransporteur;
    @FXML private Label                lblVoiture;
    @FXML private Label                lblEtat;
    @FXML private Label                lblDate;
    @FXML private Label                lblQrStatus;
    @FXML private ImageView            qrImageView;

    @FXML
    private void initialize() {

        etatLivraisonComboBox.setItems(
                FXCollections.observableArrayList("livrée", "non livrée")
        );

        List<Transporteur> transports = livraisonService.getAllTransporteurs();
        transporteurComboBox.setItems(FXCollections.observableArrayList(transports));
        transporteurComboBox.setConverter(new StringConverter<Transporteur>() {
            @Override public String toString(Transporteur t) {
                return t == null
                        ? ""
                        : t.getNom() + " " + t.getPrenom();
            }
            @Override public Transporteur fromString(String s) {
                return null;
            }
        });

        List<Voiture> voitures = livraisonService.getAllVoitures();
        voitureComboBox.setItems(FXCollections.observableArrayList(voitures));
        voitureComboBox.setConverter(new StringConverter<Voiture>() {
            @Override public String toString(Voiture v) {
                return v == null ? "" : v.getId() + " – " + v.getModel();
            }
            @Override public Voiture fromString(String s) { return null; }
        });

        livraisonDetailsBox.setVisible(false);
        loadLivraisons();
    }

    private void loadLivraisons() {
        cardsContainer.getChildren().clear();
        for (Livraison liv : livraisonService.getAllLivraisons()) {
            cardsContainer.getChildren().add(createCard(liv));
        }
    }

    private VBox createCard(Livraison livraison) {
        VBox card = new VBox(10);
        card.setStyle("-fx-border-color: black; -fx-padding: 10px; -fx-background-color: #f1f1f1;");

        Label t = new Label("Transporteur: " + livraison.getNomTransporteur());
        Label v = new Label("Voiture: "     + livraison.getModeleVoiture());
        Label e = new Label("État: "        + livraison.getEtatLivraison());
        Label d = new Label("Date: "        + livraison.getDateLivraison());
        Label q = new Label("QR utilisé ? " + (livraison.isQrUsed() ? "Oui" : "Non"));
        q.setStyle(livraison.isQrUsed() ? "-fx-text-fill: red;" : "-fx-text-fill: green;");

        ImageView qrView = new ImageView();
        qrView.setFitWidth(150);
        qrView.setFitHeight(150);
        qrView.setPreserveRatio(true);
        qrView.setVisible(false);

        card.getChildren().addAll(t, v, e, d, q, qrView);

        card.setOnMouseClicked(evt -> {

            selectedLivraison = livraison;

            transporteurComboBox.getItems().stream()
                    .filter(tr -> tr.getId() == livraison.getTransporteurId())
                    .findFirst().ifPresent(transporteurComboBox::setValue);
            voitureComboBox.getItems().stream()
                    .filter(vo -> vo.getId() == livraison.getVoitureId())
                    .findFirst().ifPresent(voitureComboBox::setValue);
            etatLivraisonComboBox.setValue(livraison.getEtatLivraison());
            dateLivraisonField.setValue(livraison.getDateLivraison());
            showSuccessMessage("Livraison sélectionnée.");

            try {
                String content = livraison.getQRCodeContent();
                String path = "livraison/qrcodes/livraison_" + livraison.getId() + ".png";
                File f = new File(path);
                if (!f.exists()) QRCodeGenerator.generateQRCodeImage(content, path);
                qrView.setImage(new Image(f.toURI().toString()));
                qrView.setVisible(true);
            } catch (IOException | WriterException ex) {
                ex.printStackTrace();
                showErrorMessage("Erreur génération QR : " + ex.getMessage());
            }
        });

        return card;
    }

    @FXML
    private void addLivraison() {
        try {
            if (!validateFields()) throw new IllegalArgumentException("Tous les champs obligatoires doivent être remplis.");
            Transporteur tr = transporteurComboBox.getValue();
            Voiture vo = voitureComboBox.getValue();
            String et = etatLivraisonComboBox.getValue();
            LocalDate da = dateLivraisonField.getValue();
            if (da.isBefore(LocalDate.now())) throw new IllegalArgumentException("Date passée !");
            Livraison liv = new Livraison(0, tr.getId(), vo.getId(), et, da);
            liv.setNomTransporteur(tr.getNom());
            liv.setModeleVoiture(vo.getModel());
            livraisonService.createLivraison(liv);
            loadLivraisons(); resetFields(); showSuccessMessage("Ajouté avec succès !");
        } catch (Exception ex) {
            showErrorMessage(ex.getMessage());
        }
    }

    @FXML
    private void updateLivraison() {
        try {
            if (selectedLivraison == null) throw new IllegalArgumentException("Sélectionnez une livraison.");
            if (!validateFields()) throw new IllegalArgumentException("Tous les champs obligatoires doivent être remplis.");
            Transporteur tr = transporteurComboBox.getValue();
            Voiture vo = voitureComboBox.getValue();
            selectedLivraison.setTransporteurId(tr.getId());
            selectedLivraison.setVoitureId(vo.getId());
            selectedLivraison.setNomTransporteur(tr.getNom());
            selectedLivraison.setModeleVoiture(vo.getModel());
            selectedLivraison.setEtatLivraison(etatLivraisonComboBox.getValue());
            selectedLivraison.setDateLivraison(dateLivraisonField.getValue());
            livraisonService.updateLivraison(selectedLivraison);
            loadLivraisons(); resetFields(); showSuccessMessage("Mis à jour avec succès !");
        } catch (Exception ex) {
            showErrorMessage(ex.getMessage());
        }
    }

    @FXML
    private void deleteLivraison() {
        try {
            if (selectedLivraison == null) throw new IllegalArgumentException("Sélectionnez une livraison.");
            livraisonService.deleteLivraison(selectedLivraison.getId());
            loadLivraisons(); resetFields(); showSuccessMessage("Supprimé avec succès !");
        } catch (Exception ex) {
            showErrorMessage(ex.getMessage());
        }
    }

    private boolean validateFields() {
        return transporteurComboBox.getValue() != null
                && voitureComboBox.getValue() != null
                && etatLivraisonComboBox.getValue() != null
                && dateLivraisonField.getValue() != null;
    }

    private void resetFields() {
        transporteurComboBox.setValue(null);
        voitureComboBox.setValue(null);
        etatLivraisonComboBox.setValue(null);
        dateLivraisonField.setValue(null);
        selectedLivraison = null;
        errorMessage.setVisible(false);
        successLabel.setVisible(false);
        qrImageView.setVisible(false);
    }

    private void showErrorMessage(String msg) {
        errorMessage.setText(msg);
        errorMessage.setVisible(true);
        PauseTransition pt = new PauseTransition(Duration.seconds(3));
        pt.setOnFinished(e -> errorMessage.setVisible(false));
        pt.play();
    }

    private void showSuccessMessage(String msg) {
        successLabel.setText(msg);
        successLabel.setVisible(true);
        PauseTransition pt = new PauseTransition(Duration.seconds(2));
        pt.setOnFinished(e -> successLabel.setVisible(false));
        pt.play();
    }
}