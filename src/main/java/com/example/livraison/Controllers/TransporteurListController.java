package com.example.livraison.Controllers;

import com.example.livraison.Models.Transporteur;
import com.example.livraison.Services.TransporteurService;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class TransporteurListController {

    @FXML
    private ListView<Transporteur> listViewTransporteurs;

    private TransporteurService transporteurService;

    @FXML
    private Label feedbackLabel;


    public TransporteurListController() {

        transporteurService = new TransporteurService();
    }

    @FXML
    public void initialize() {

        afficherListeTransporteurs();
    }


    @FXML
    public void handleUpdateTransporteur() {
        Transporteur selectedTransporteur = listViewTransporteurs.getSelectionModel().getSelectedItem();
        if (selectedTransporteur != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/livraison/views/ajouter-transporteur.fxml"));
                Scene scene = new Scene(loader.load());
                TransporteurController controller = loader.getController();
                controller.setTransporteurToUpdate(selectedTransporteur);

                Stage stage = new Stage();
                stage.setTitle("Modifier Transporteur");
                stage.setScene(scene);
                stage.initModality(Modality.APPLICATION_MODAL);

                stage.setOnHiding(event -> afficherListeTransporteurs());
                stage.showAndWait();

            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Erreur", "Impossible de charger le formulaire.", Alert.AlertType.ERROR);

            }
        } else {
            showAlert("Avertissement", "Veuillez sélectionner un transporteur à modifier.", Alert.AlertType.WARNING);

        }
    }

    @FXML
    private void handleDeleteTransporteur() {
        Transporteur selected = listViewTransporteurs.getSelectionModel().getSelectedItem();

        if (selected != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmation de suppression");
            confirm.setHeaderText(null);
            confirm.setContentText("Voulez-vous vraiment supprimer ce transporteur ?");

            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    transporteurService.deleteTransporteur(selected.getId());

                    feedbackLabel.setText("Transporteur supprimé avec succès !");
                    feedbackLabel.setStyle("-fx-text-fill: green;");
                    feedbackLabel.setVisible(true);
                    FadeTransition fadeOut = new FadeTransition(Duration.seconds(3), feedbackLabel);
                    fadeOut.setFromValue(1.0);
                    fadeOut.setToValue(0.0);
                    fadeOut.setCycleCount(1);
                    fadeOut.setAutoReverse(false);
                    fadeOut.play();


                    afficherListeTransporteurs();
                }
            });

        } else {
            showAlert("Erreur", "Veuillez sélectionner un transporteur à supprimer.", Alert.AlertType.ERROR);
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
                    setGraphic(null);
                    setText(null);
                } else {

                    VBox card = new VBox();
                    card.setSpacing(10);
                    card.setStyle("-fx-padding: 15px; -fx-background-color: #fff; -fx-border-radius: 10px; -fx-border-color: #ccc; -fx-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);");


                    Label nameLabel = new Label(transporteur.getNom() + " " + transporteur.getPrenom());
                    nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");


                    Label statusLabel = new Label(transporteur.is_disponible() ? "Disponible" : "Non Disponible");
                    statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: " + (transporteur.is_disponible() ? "green" : "red") + ";");


                    Button detailsButton = new Button("Détails");
                    detailsButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 5px 10px;");
                    detailsButton.setOnAction(event -> handleDetails(transporteur));


                    card.getChildren().addAll(nameLabel, statusLabel, detailsButton);
                    setGraphic(card);
                }
            }
        });
    }

    @FXML
    private void handleDetails(Transporteur transporteur) {
        if (transporteur != null) {

            showAlert("Détails", "Nom: " + transporteur.getNom() + "\nPrénom: " + transporteur.getPrenom(), Alert.AlertType.INFORMATION);
        } else {
            showAlert("Avertissement", "Veuillez sélectionner un transporteur.", Alert.AlertType.WARNING);
        }
    }




    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}


