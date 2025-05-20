package com.example.gotoesig;

import java.util.List;

public class RouteResponse {
    private List<Route> routes;

    public List<Route> getRoutes() {
        return routes;
    }

    public static class Route {
        private Summary summary;
        private String geometry;

        public Summary getSummary() {
            return summary;
        }

        public String getGeometry() {
            return geometry;
        }
    }

    public static class Summary {
        private double distance;
        private double duration;

        public double getDistance() {
            return distance;
        }

        public double getDuration() {
            return duration;
        }
    }
}


