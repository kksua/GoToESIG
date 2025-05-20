package com.example.gotoesig;

import android.util.Log;

import com.google.firebase.Timestamp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Trajet {
    private String d;
    private String pointDepart;
    private double latitude;
    private double longitude;
    private Date date;
    private String heure;
    private int placesDisponibles;
    private float distance;
    private float duree;
    private String moyenTransport;
    private float contribution;
    private List<String> joinedUsers;

    //Constructeur pour la saisie manuelle de l'emplacement
    public Trajet(String documentId, String pointDepart, Date date, String heure, int placesDisponibles, String moyenTransport) {
        this.documentId = documentId;
        this.pointDepart = pointDepart;
        this.date = date;
        this.heure = heure;
        this.placesDisponibles = placesDisponibles;
        this.moyenTransport = moyenTransport;
        this.contribution = 0;
    }

    //Constructeur pour une entrée basée sur la géolocalisation
    public Trajet(String documentId, double latitude, double longitude, Date date, String heure, int placesDisponibles, String moyenTransport, float contribution) {
        this.documentId = documentId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.date = date;
        this.heure = heure;
        this.placesDisponibles = placesDisponibles;
        this.moyenTransport = moyenTransport;
        this.contribution = contribution;
    }

    public Timestamp getFirestoreTimestamp() {
        if (date != null && heure != null) {
            try {
                String dateTimeString = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date) + " " + heure;
                SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                Date combinedDateTime = dateTimeFormat.parse(dateTimeString);
                return new Timestamp(combinedDateTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    // Split Firestore Timestamp into date and heure
    public void setDateAndHeureFromTimestamp(Timestamp timestamp) {
        if (timestamp != null) {
            Date fullDate = timestamp.toDate();
            this.date = fullDate; // Extract the date part
            this.heure = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(fullDate); // Extract the time part
        }
    }

    public Trajet() {}
    // Getters et Setters
    public String getdocumentId() {
        return documentId;
    }

    public void setdocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getPointDepart() {
        return pointDepart;
    }

    public void setPointDepart(String pointDepart) {
        this.pointDepart = pointDepart;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getHeure() {
        return heure;
    }

    public void setHeure(String heure) {
        this.heure = heure;
    }

    public int getPlacesDisponibles() {
        return placesDisponibles;
    }

    public void setPlacesDisponibles(int placesDisponibles) {
        this.placesDisponibles = placesDisponibles;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public float getDuree() {
        return duree;
    }

    public void setDuree(float duree) {
        this.duree = duree;
    }

    public String getMoyenTransport() {
        return moyenTransport;
    }

    public void setMoyenTransport(String moyenTransport) {
        this.moyenTransport = moyenTransport;
    }

    public float getContribution() {
        return contribution;
    }

    public void setContribution(float contribution) {
        this.contribution = contribution;
    }

    // Methode pour afficher les details des trajets
    @Override
    public String toString() {
        return "Trajet{" +
                "id='" + documentId + '\'' +
                ", pointDepart='" + pointDepart + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", isGeolocationUsed=" + isGeolocationUsed +
                ", date=" + date +
                ", heure='" + heure + '\'' +
                ", placesDisponibles=" + placesDisponibles +
                ", distance=" + distance +
                ", duree=" + duree +
                ", moyenTransport='" + moyenTransport + '\'' +
                ", contribution=" + contribution +
                '}';
    }
}


