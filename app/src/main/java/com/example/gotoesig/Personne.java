package com.example.gotoesig;

public class Personne {
    private String id;
    private String nom;
    private String prenom;
    private String email;
    private String numeroTelephone;
    private String ville;
    private String photoProfilUrl;

    // Constructeur
    public Personne(String id, String nom, String prenom,String email, String numeroTelephone, String ville, String photoProfilUrl) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.numeroTelephone = numeroTelephone;
        this.ville = ville;
        this.photoProfilUrl = photoProfilUrl;
    }

    //Constructeur
    public Personne(){}

    // Getters et Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
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
    public String getEmail() {
        return nom;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public String getNumeroTelephone() {
        return numeroTelephone;
    }

    public void setNumeroTelephone(String numeroTelephone) {
        this.numeroTelephone = numeroTelephone;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getPhotoProfilUrl() {
        return photoProfilUrl;
    }

    public void setPhotoProfilUrl(String photoProfilUrl) {
        this.photoProfilUrl = photoProfilUrl;
    }

    // Methode pour afficher l'objet Personne
    @Override
    public String toString() {
        return "Personne{" +
                "id='" + id + '\'' +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email = '" + email + '\'' +
                ", numeroTelephone='" + numeroTelephone + '\'' +
                ", ville='" + ville + '\'' +
                ", photoProfilUrl='" + photoProfilUrl + '\'' +
                '}';
    }
}
