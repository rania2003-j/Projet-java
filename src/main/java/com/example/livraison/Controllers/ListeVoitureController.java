package com.example.livraison.Controllers;

import com.example.livraison.Models.Voiture;
import com.example.livraison.Services.VoitureService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class ListeVoitureController {

    @FXML
    private ListView<Voiture> voitureListView;

    private VoitureService voitureService = new VoitureService();

    @FXML
    private void initialize() {


        List<Voiture> voitures = voitureService.getAllVoitures();


        ObservableList<Voiture> voitureObservableList = FXCollections.observableArrayList(voitures);

        voitureListView.setItems(voitureObservableList);


        voitureListView.setCellFactory(lv -> new javafx.scene.control.ListCell<Voiture>() {
            @Override
            protected void updateItem(Voiture voiture, boolean empty) {
                super.updateItem(voiture, empty);
                if (empty || voiture == null) {
                    setText(null);
                } else {
                    setText(voiture.toString());
                }
            }
        });
    }


    @FXML
    private void retourAccueil() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/livraison/voiture.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) voitureListView.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showErrorAlert("Erreur de chargement", "La scène n'a pas pu être chargée.");
            e.printStackTrace();
        }
    }


    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

