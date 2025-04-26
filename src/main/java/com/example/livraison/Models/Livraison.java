package com.example.livraison.Models;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class Livraison {
    private int id;
    private int transporteurId;
    private int voitureId;
    private String nomTransporteur;
    private String modeleVoiture;
    private String etatLivraison;
    private LocalDate dateLivraison;
    private boolean qrUsed;
    private String qrCodeData;


    public Livraison(int id,
                     int transporteurId,
                     int voitureId,
                     String etatLivraison,
                     LocalDate dateLivraison,
                     boolean qrUsed,
                     String qrCodeData) {
        this.id = id;
        this.transporteurId = transporteurId;
        this.voitureId = voitureId;
        this.etatLivraison = etatLivraison;
        this.dateLivraison = dateLivraison;
        this.qrUsed = qrUsed;
        this.qrCodeData = qrCodeData;
    }
    public Livraison(int id,
                     int transporteurId,
                     int voitureId,
                     String etatLivraison,
                     LocalDate dateLivraison) {
        this(id, transporteurId, voitureId, etatLivraison, dateLivraison, false, null);
    }

    public boolean isQrUsed() {
        return qrUsed;
    }
    public void setQrUsed(boolean qrUsed) {
        this.qrUsed = qrUsed;
    }

    // ← getters/setters pour qrCodeData
    public String getQrCodeData() {
        return qrCodeData;
    }
    public void setQrCodeData(String qrCodeData) {
        this.qrCodeData = qrCodeData;
    }

    public int getId() {

        return id;
    }
    public void setId(int id) {

        this.id = id;
    }

    public int getTransporteurId() {

        return transporteurId;
    }
    public void setTransporteurId(int transporteurId) {

        this.transporteurId = transporteurId;
    }

    public int getVoitureId() {

        return voitureId;
    }
    public void setVoitureId(int voitureId) {

        this.voitureId = voitureId;
    }

    public String getEtatLivraison() {

        return etatLivraison;
    }
    public void setEtatLivraison(String etatLivraison) {
        this.etatLivraison = etatLivraison;
    }

    public LocalDate getDateLivraison() {
        return dateLivraison;
    }


    public void setDateLivraison(LocalDate dateLivraison) {
        if (dateLivraison.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La date de livraison ne peut pas être dans le passé !");
        }
        this.dateLivraison = dateLivraison;
    }

    public String getNomTransporteur() {
        return nomTransporteur;
    }
    public void setNomTransporteur(String nomTransporteur) {
        this.nomTransporteur = nomTransporteur;
    }

    public String getModeleVoiture() {
        return modeleVoiture;
    }
    public void setModeleVoiture(String modeleVoiture) {
        this.modeleVoiture = modeleVoiture;
    }



    public String generateQRCodeContent() {
        return "Livraison ID: " + id + "\n"
                + "Transporteur: " + (nomTransporteur!=null?nomTransporteur:"ID "+transporteurId) + "\n"
                + "Voiture: "     + (modeleVoiture!=null?modeleVoiture:"ID "+voitureId) + "\n"
                + "État: "        + etatLivraison + "\n"
                + "Date: "        + dateLivraison + "\n"
                + "QR Code utilisé: " + (qrUsed ? "Oui" : "Non");
    }
    public String getQRCodeContent() {
        return generateQRCodeContent();
    }


    @Override
    public String toString() {
        return "Livraison{id=" + id +
                ", transporteurId=" + transporteurId +
                ", voitureId=" + voitureId +
                ", nomTransporteur='" + nomTransporteur + '\'' +
                ", modeleVoiture='" + modeleVoiture + '\'' +
                ", etatLivraison='" + etatLivraison + '\'' +
                ", dateLivraison='" + dateLivraison + '\'' +
                ", qrUsed=" + qrUsed +
                '}';
    }
}

