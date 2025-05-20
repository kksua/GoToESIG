package com.example.gotoesig;

import java.util.List;

public class RouteRequest {
    private String profile;
    private List<List<Double>> coordinates;

    public RouteRequest(String profile, List<List<Double>> coordinates) {
        this.profile = profile;
        this.coordinates = coordinates;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public List<List<Double>> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<List<Double>> coordinates) {
        this.coordinates = coordinates;
    }
}

