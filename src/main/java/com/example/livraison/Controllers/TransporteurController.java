package com.example.livraison.Controllers;

import com.example.livraison.Services.TransporteurService;
import com.example.livraison.Models.Transporteur;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class TransporteurController {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField idField;
    @FXML private ListView<Transporteur> listViewTransporteurs;
    @FXML private Button btnDisponible;
    @FXML private Button btnNonDisponible;
    @FXML private Button btnDelete;
    @FXML private Button btnAjouter;
    @FXML private Button btnEnregistrerModifs;


    private Transporteur transporteurToUpdate = null;
    private boolean is_disponible = false;
    private TransporteurService transporteurService;

    public TransporteurController() {
        transporteurService = new TransporteurService();
    }

    @FXML
    public void initialize() {
        updateButtonStyles();
        if (btnEnregistrerModifs != null) {
            btnEnregistrerModifs.setDisable(true);
        }

    }

    public void setTransporteurToUpdate(Transporteur transporteur) {
        this.transporteurToUpdate = transporteur;
        idField.setText(String.valueOf(transporteur.getId()));
        idField.setDisable(true);
        nomField.setText(transporteur.getNom());
        prenomField.setText(transporteur.getPrenom());
        is_disponible = transporteur.is_disponible();
        updateButtonStyles();

        btnEnregistrerModifs.setDisable(false);
        btnAjouter.setDisable(true);
    }

    @FXML
    public void handleDisponible() {
        is_disponible = true;
        updateButtonStyles();
    }

    @FXML
    public void handleNonDisponible() {
        is_disponible = false;
        updateButtonStyles();
    }

    private void updateButtonStyles() {
        if (btnDisponible == null || btnNonDisponible == null) return;

        if (is_disponible) {
            btnDisponible.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
            btnNonDisponible.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: black;");
        } else {
            btnNonDisponible.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
            btnDisponible.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: black;");
        }
    }

    @FXML
    public void addTransporteur() {
        String nom = nomField.getText();
        String prenom = prenomField.getText();
        String idText = idField.getText();

        if (nom.length() < 3 || prenom.length() < 3) {
            showAlert("Erreur", "Le nom et le prénom doivent avoir au moins 3 caractères", Alert.AlertType.ERROR);
            return;
        }

        if (idText.isEmpty() || nom.isEmpty() || prenom.isEmpty()) {
            showAlert("Erreur", "Tous les champs (ID, nom, prénom) sont obligatoires", Alert.AlertType.ERROR);
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idText);
        } catch (NumberFormatException e) {
            showAlert("Erreur", "L'ID doit être un nombre valide", Alert.AlertType.ERROR);
            return;
        }

        if (!nom.matches("[a-zA-Z]+") || !prenom.matches("[a-zA-Z]+")) {
            showAlert("Erreur", "Le nom et le prénom doivent contenir uniquement des lettres", Alert.AlertType.ERROR);
            return;
        }


        if (transporteurToUpdate != null) {
            Transporteur updated = new Transporteur(id, nom, prenom, is_disponible);
            transporteurService.updateTransporteur(updated);
            showAlert("Succès", "Transporteur modifié avec succès", Alert.AlertType.INFORMATION);
            ouvrirListeTransporteurs();
            closeCurrentWindow();
            return;
        }


        if (transporteurService.isIdExist(id)) {
            showAlert("Erreur", "L'ID existe déjà dans la base de données", Alert.AlertType.ERROR);
            return;
        }

        Transporteur transporteur = new Transporteur(id, nom, prenom, is_disponible);
        transporteurService.addTransporteur(transporteur);
        showAlert("Succès", "Transporteur ajouté avec succès", Alert.AlertType.INFORMATION);
        ouvrirListeTransporteurs();
        closeCurrentWindow();
    }

    @FXML
    public void enregistrerModifications() {
        if (transporteurToUpdate == null) {
            showAlert("Erreur", "Aucun transporteur sélectionné pour la modification", Alert.AlertType.ERROR);
            return;
        }

        String nom = nomField.getText();
        String prenom = prenomField.getText();

        if (nom.length() < 3 || prenom.length() < 3) {
            showAlert("Erreur", "Le nom et le prénom doivent avoir au moins 3 caractères", Alert.AlertType.ERROR);
            return;
        }

        if (!nom.matches("[a-zA-Z]+") || !prenom.matches("[a-zA-Z]+")) {
            showAlert("Erreur", "Le nom et le prénom doivent contenir uniquement des lettres", Alert.AlertType.ERROR);
            return;
        }

        transporteurToUpdate.setNom(nom);
        transporteurToUpdate.setPrenom(prenom);
        transporteurToUpdate.setIs_disponible(is_disponible);

        transporteurService.updateTransporteur(transporteurToUpdate);

        showAlert("Succès", "Transporteur modifié avec succès", Alert.AlertType.INFORMATION);

        btnEnregistrerModifs.setDisable(true);
        btnAjouter.setDisable(false);
        transporteurToUpdate = null;
        ouvrirListeTransporteurs();
        closeCurrentWindow();
    }



    private void closeCurrentWindow() {
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void deleteTransporteur() {
        Transporteur selectedTransporteur = listViewTransporteurs.getSelectionModel().getSelectedItem();
        if (selectedTransporteur != null) {
            transporteurService.deleteTransporteur(selectedTransporteur.getId());
            showAlert("Succès", "Transporteur supprimé avec succès", Alert.AlertType.INFORMATION);
            afficherListeTransporteurs();
        } else {
            showAlert("Erreur", "Veuillez sélectionner un transporteur à supprimer", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void afficherListeTransporteurs() {
        ObservableList<Transporteur> transporteurs = FXCollections.observableArrayList(transporteurService.getAllTransporteurs());
        listViewTransporteurs.setItems(transporteurs);

        listViewTransporteurs.setCellFactory(lv -> new ListCell<Transporteur>() {
            @Override
            protected void updateItem(Transporteur transporteur, boolean empty) {
                super.updateItem(transporteur, empty);
                if (empty || transporteur == null) {
                    setText(null);
                } else {
                    setText("ID: " + transporteur.getId() + " | " + transporteur.getNom() + " " + transporteur.getPrenom() + " (" +
                            (transporteur.is_disponible() ? "Disponible" : "Non Disponible") + ")");
                }
            }
        });
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void ouvrirListeTransporteurs() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/livraison/views/ajouter-transporteur-list.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle("Liste des Transporteurs");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


