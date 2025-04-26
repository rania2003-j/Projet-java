package com.example.livraison.Controllers;

import com.example.livraison.Models.Livraison;
import com.example.livraison.Services.LivraisonService;
import com.example.livraison.utils.QRCodeGenerator;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.File;
import java.time.LocalDate;

public class LivraisonController {

    private final LivraisonService livraisonService = new LivraisonService();
    private Livraison selectedLivraison = null;

    @FXML
    private FlowPane cardsContainer;

    @FXML
    private TextField transporteurIdField;

    @FXML
    private TextField voitureIdField;

    @FXML
    private ComboBox<String> etatLivraisonComboBox;

    @FXML
    private DatePicker dateLivraisonField;

    @FXML
    private Label errorMessage;

    @FXML
    private Label successLabel;

    @FXML
    private Button addButton;

    @FXML
    private StackPane videoView;

    @FXML
    private ImageView qrImageView;

    @FXML
    private TextField transporteurNameField;

    @FXML
    private TextField voitureModelField;

    @FXML
    private Button searchLivraisonButton;

    @FXML
    private TextField qrCodeInputField;

    @FXML
    private Button scanButton;

    @FXML
    private VBox livraisonDetailsBox;

    @FXML
    private Label lblId;
    @FXML
    private Label lblTransporteur;
    @FXML
    private Label lblVoiture;
    @FXML
    private Label lblEtat;
    @FXML
    private Label lblDate;

    @FXML
    private void initialize() {
        etatLivraisonComboBox.setItems(FXCollections.observableArrayList("livrée", "non livrée"));
        loadLivraisons();
        searchLivraisonButton.setOnAction(event -> onSearchLivraison());
        scanButton.setOnAction(event -> scanQRCode());
    }

    private void loadLivraisons() {
        cardsContainer.getChildren().clear();
        for (Livraison livraison : livraisonService.getAllLivraisons()) {
            VBox card = createCard(livraison);
            cardsContainer.getChildren().add(card);
        }
    }

    private VBox createCard(Livraison livraison) {
        VBox card = new VBox();
        card.setStyle("-fx-border-color: black; -fx-padding: 10px; -fx-background-color: #f1f1f1; -fx-spacing: 10px;");
        card.getStyleClass().add("compact-card");

        Label transporteurLabel = new Label("Transporteur N°: " + livraison.getTransporteurId());
        Label voitureLabel = new Label("Voiture N°: " + livraison.getVoitureId());
        Label etatLabel = new Label("État: " + livraison.getEtatLivraison());
        Label dateLabel = new Label("Date: " + livraison.getDateLivraison());

        card.getChildren().addAll(transporteurLabel, voitureLabel, etatLabel, dateLabel);

        card.setOnMouseClicked(event -> {
            selectedLivraison = livraison;
            transporteurIdField.setText(String.valueOf(livraison.getTransporteurId()));
            voitureIdField.setText(String.valueOf(livraison.getVoitureId()));
            etatLivraisonComboBox.setValue(livraison.getEtatLivraison());
            dateLivraisonField.setValue(livraison.getDateLivraison());
            showSuccessMessage("Livraison sélectionnée pour modification/suppression.");
            loadQRCode(livraison.getId());
        });

        return card;
    }

    @FXML
    private void addLivraison() {
        try {
            if (!validateFields()) {
                throw new IllegalArgumentException("Tous les champs doivent être remplis.");
            }

            int transporteurId = Integer.parseInt(transporteurIdField.getText());
            int voitureId = Integer.parseInt(voitureIdField.getText());
            String etatLivraison = etatLivraisonComboBox.getValue();
            LocalDate dateLivraison = dateLivraisonField.getValue();

            if (dateLivraison == null) {
                throw new IllegalArgumentException("La date de livraison est obligatoire.");
            }

            boolean transporteurExists = livraisonService.doesTransporteurExist(transporteurId);
            if (!transporteurExists) {
                showErrorMessage("Le transporteur avec cet ID n'existe pas !");
                return;
            }

            Livraison livraison = new Livraison(0, transporteurId, voitureId, etatLivraison, dateLivraison);
            livraisonService.createLivraison(livraison);
            loadLivraisons();
            resetFields();
            showSuccessMessage("Livraison ajoutée avec succès !");
            loadQRCode(livraison.getId());

        } catch (NumberFormatException e) {
            showErrorMessage("Veuillez entrer des valeurs numériques pour les IDs.");
        } catch (IllegalArgumentException e) {
            showErrorMessage(e.getMessage());
        } catch (Exception e) {
            showErrorMessage("Erreur : " + e.getMessage());
        }
    }

    @FXML
    private void updateLivraison() {
        try {
            if (selectedLivraison == null)
                throw new IllegalArgumentException("Veuillez sélectionner une livraison à mettre à jour.");
            if (!validateFields()) throw new IllegalArgumentException("Tous les champs doivent être remplis.");

            int transporteurId = Integer.parseInt(transporteurIdField.getText());
            int voitureId = Integer.parseInt(voitureIdField.getText());
            String etatLivraison = etatLivraisonComboBox.getValue();
            LocalDate dateLivraison = dateLivraisonField.getValue();

            boolean transporteurExists = livraisonService.doesTransporteurExist(transporteurId);
            if (!transporteurExists) {
                showErrorMessage("Le transporteur avec cet ID n'existe pas !");
                return;
            }

            Livraison updated = new Livraison(selectedLivraison.getId(), transporteurId, voitureId, etatLivraison, dateLivraison);
            livraisonService.updateLivraison(updated);

            loadLivraisons();
            resetFields();
            showSuccessMessage("Livraison mise à jour avec succès !");
            loadQRCode(updated.getId());

        } catch (NumberFormatException e) {
            showErrorMessage("Veuillez entrer des valeurs numériques pour les IDs.");
        } catch (IllegalArgumentException e) {
            showErrorMessage(e.getMessage());
        } catch (Exception e) {
            showErrorMessage("Erreur : " + e.getMessage());
        }
    }

    @FXML
    private void deleteLivraison() {
        try {
            if (selectedLivraison == null)
                throw new IllegalArgumentException("Veuillez sélectionner une livraison à supprimer.");

            livraisonService.deleteLivraison(selectedLivraison.getId());

            loadLivraisons();
            resetFields();
            showSuccessMessage("Livraison supprimée avec succès !");

        } catch (Exception e) {
            showErrorMessage("Erreur : " + e.getMessage());
        }
    }

    private void loadQRCode(int livraisonId) {
        try {
            String qrCodePath = "qrcodes/livraison_" + livraisonId + ".png";
            File qrCodeFile = new File(qrCodePath);

            if (!qrCodeFile.exists()) {
                String qrContent = selectedLivraison.getQRCodeContent();
                QRCodeGenerator.generateQRCodeImage(qrContent, qrCodePath);
            }

            Image qrImage = new Image(qrCodeFile.toURI().toString());
            qrImageView.setImage(qrImage);

        } catch (Exception e) {
            showErrorMessage("Erreur lors du chargement/génération du QR Code : " + e.getMessage());
        }
    }

    @FXML
    private void onSearchLivraison() {
        String transporteurNom = transporteurNameField.getText();
        String voitureModele = voitureModelField.getText();

        Livraison livraison = livraisonService.findByTransporteurNameAndVoitureModel(transporteurNom, voitureModele);

        if (livraison != null) {
            VBox card = createCard(livraison);
            cardsContainer.getChildren().clear();
            cardsContainer.getChildren().add(card);

            card.setStyle("-fx-background-color: #d4edda; -fx-border-color: #155724; -fx-border-radius: 10; -fx-background-radius: 10;");
            PauseTransition highlightPause = new PauseTransition(Duration.seconds(2));
            highlightPause.setOnFinished(e -> card.setStyle(""));
            highlightPause.play();

            showSuccessMessage("✅ Livraison trouvée avec succès !");
        } else {
            showErrorMessage("Livraison introuvable.");
        }
    }

    private boolean validateFields() {
        return !transporteurIdField.getText().isEmpty() &&
                !voitureIdField.getText().isEmpty() &&
                etatLivraisonComboBox.getValue() != null &&
                dateLivraisonField.getValue() != null;
    }

    private void resetFields() {
        transporteurIdField.clear();
        voitureIdField.clear();
        etatLivraisonComboBox.setValue(null);
        dateLivraisonField.setValue(null);
        transporteurNameField.clear();
        voitureModelField.clear();
        selectedLivraison = null;
    }

    private void showErrorMessage(String message) {
        errorMessage.setText(message);
        errorMessage.setStyle("-fx-text-fill: red;");
    }

    private void showSuccessMessage(String message) {
        successLabel.setText(message);
        successLabel.setVisible(true);

        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(event -> successLabel.setVisible(false));
        pause.play();
    }

    @FXML
    private void scanQRCode() {
        String qrData = qrCodeInputField.getText().trim();

        if (qrData.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champ vide", "Veuillez coller les données du QR code.");
            return;
        }

        Livraison livraison = livraisonService.handleQrScan(qrData);
        if (livraison == null) {
            showAlert(Alert.AlertType.ERROR, "QR Code", "QR Code invalide ou déjà utilisé !");
            livraisonDetailsBox.setVisible(false);
        } else {

            lblId.setText("ID: " + livraison.getId());
            lblTransporteur.setText("Transporteur ID: " + livraison.getTransporteurId());
            lblVoiture.setText("Voiture ID: " + livraison.getVoitureId());
            lblEtat.setText("État: " + livraison.getEtatLivraison());
            lblDate.setText("Date: " + livraison.getDateLivraison().toString());

            livraisonDetailsBox.setVisible(true);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "QR Code valide. Livraison trouvée !");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}















