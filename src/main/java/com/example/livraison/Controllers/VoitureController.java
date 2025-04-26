package com.example.livraison.Controllers;

import com.example.livraison.Models.Voiture;
import com.example.livraison.Services.VoitureService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;

public class VoitureController {

    @FXML private ListView<Voiture> voitureListView;
    @FXML private TextField id;
    @FXML private TextField modelField;
    @FXML private TextField matriculeField;
    @FXML private TextField capaciteField;
    @FXML private Button updateButton;
    @FXML private Label errorMessage;

    private final ObservableList<Voiture> voitureList = FXCollections.observableArrayList();
    private final VoitureService voitureService = new VoitureService();

    @FXML
    private void initialize() {
        // Assurer que les éléments FXML sont bien liés
        if (id == null || modelField == null || matriculeField == null || capaciteField == null) {
            System.out.println("Un ou plusieurs champs FXML sont nuls !");
        }

        loadVoitures();
        setupListView();
    }

    private void setupListView() {
        voitureListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                fillFieldsWithVoitureData(newValue);
            } else {
                resetFields();
            }
        });

        voitureListView.setCellFactory(new Callback<ListView<Voiture>, ListCell<Voiture>>() {
            @Override
            public ListCell<Voiture> call(ListView<Voiture> param) {
                return new ListCell<Voiture>() {
                    @Override
                    protected void updateItem(Voiture voiture, boolean empty) {
                        super.updateItem(voiture, empty);
                        if (empty || voiture == null) {
                            setText(null);
                            setStyle("");
                        } else {
                            setText(voiture.getModel());

                            if (getIndex() == 0) {
                                setStyle("-fx-background-color: #f0f8ff; -fx-font-weight: bold;");
                            } else {
                                setStyle("");
                            }
                        }
                    }
                };
            }
        });
    }

    private void loadVoitures() {
        voitureList.setAll(voitureService.getAllVoitures());
        voitureListView.setItems(voitureList);
    }

    @FXML
    private void addVoiture() {
        if (!validateFields()) {
            showErrorMessage("Tous les champs sont obligatoires.");
            return;
        }

        try {
            int capacite = Integer.parseInt(capaciteField.getText());
            if (capacite <= 0) throw new NumberFormatException();

            Voiture voiture = new Voiture(0, modelField.getText().trim(), matriculeField.getText().trim(), capacite);
            if (voitureService.addVoiture(voiture)) {
                loadVoitures();
                resetFields();
                showSuccessMessage("Voiture ajoutée avec succès !");
            } else {
                showErrorMessage("Erreur lors de l'ajout de la voiture.");
            }

        } catch (NumberFormatException ex) {
            showErrorMessage("Capacité invalide. Entrez un nombre entier positif.");
        }
    }

    @FXML
    private void modifyVoiture() {
        Voiture selected = voitureListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            fillFieldsWithVoitureData(selected);
            updateButton.setVisible(true);
            showSuccessMessage("Voiture chargée pour modification.");
        } else {
            showErrorMessage("Veuillez sélectionner une voiture dans la liste.");
        }
    }

    @FXML
    private void updateVoiture() {
        if (!validateFields() || id.getText().isEmpty()) {
            showErrorMessage("Champs invalides ou aucune voiture sélectionnée.");
            return;
        }

        try {
            int voitureId = Integer.parseInt(id.getText());
            int capacite = Integer.parseInt(capaciteField.getText());
            if (capacite <= 0) throw new NumberFormatException();

            Voiture voiture = new Voiture(voitureId, modelField.getText().trim(), matriculeField.getText().trim(), capacite);
            if (voitureService.updateVoiture(voiture)) {
                loadVoitures();
                resetFields();
                showSuccessMessage("Voiture mise à jour avec succès !");
            } else {
                showErrorMessage("Erreur lors de la mise à jour de la voiture.");
            }

        } catch (NumberFormatException e) {
            showErrorMessage("ID ou capacité invalide.");
        }
    }

    @FXML
    private void deleteVoiture() {
        Voiture selected = voitureListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showErrorMessage("Veuillez sélectionner une voiture à supprimer.");
            return;
        }

        if (voitureService.deleteVoiture(selected.getId())) {
            loadVoitures();
            resetFields();
            showSuccessMessage("Voiture supprimée avec succès !");
        } else {
            showErrorMessage("Erreur lors de la suppression.");
        }
    }

    @FXML
    private void goToListeVoiture() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/livraison/views/ListeVoiture.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) voitureListView.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorMessage("Impossible d'ouvrir la vue ListeVoitures.");
        }
    }

    private boolean validateFields() {
        return !modelField.getText().trim().isEmpty() &&
                !matriculeField.getText().trim().isEmpty() &&
                !capaciteField.getText().trim().isEmpty();
    }

    private void resetFields() {
        id.clear();
        modelField.clear();
        matriculeField.clear();
        capaciteField.clear();
        voitureListView.getSelectionModel().clearSelection();
        updateButton.setVisible(false);
        errorMessage.setText("");
    }

    private void showErrorMessage(String msg) {
        errorMessage.setText(msg);
        errorMessage.setStyle("-fx-text-fill: red;");
    }

    private void showSuccessMessage(String msg) {
        errorMessage.setText(msg);
        errorMessage.setStyle("-fx-text-fill: green;");
    }

    private void fillFieldsWithVoitureData(Voiture voiture) {
        id.setText(String.valueOf(voiture.getId()));
        modelField.setText(voiture.getModel());
        matriculeField.setText(voiture.getMatricule());
        capaciteField.setText(String.valueOf(voiture.getCapacite()));
    }
}



























