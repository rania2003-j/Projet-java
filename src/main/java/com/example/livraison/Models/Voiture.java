package com.example.livraison.Models;

public class Voiture {
    private int id;
    private String model;
    private String matricule;
    private int capacite;

    public Voiture(int id, String model, String matricule, int capacite) {
        this.id = id;
        this.model = model;
        this.matricule = matricule;
        this.capacite = capacite;
    }

    public int getId() { return id; }
    public String getModel() { return model; }
    public String getMatricule() { return matricule; }
    public int getCapacite() { return capacite; }

    public void setId(int id) { this.id = id; }
    public void setModel() { this.model = model; }
    public void setMatricule(String matricule) { this.matricule = matricule; }
    public void setCapacite(int capacite) { this.capacite = capacite; }

    @Override
    public String toString() {
        return model + " - " + matricule + " - " + capacite + " places";
    }
}


