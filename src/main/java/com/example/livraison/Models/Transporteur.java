package com.example.livraison.Models;

public class Transporteur {
    private int id;
    private String nom;
    private String prenom;
    private boolean is_disponible;


    public Transporteur(int id, String nom, String prenom, boolean is_disponible) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.is_disponible = is_disponible;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public boolean is_disponible() {
        return is_disponible;
    }

    public void setIs_disponible(boolean is_disponible) {
        this.is_disponible = is_disponible;
    }
    @Override
    public String toString() {
        return id +" "+ nom + " " + prenom +" "+ (is_disponible ? " (Disponible)" : " (Indisponible)");
    }
}
