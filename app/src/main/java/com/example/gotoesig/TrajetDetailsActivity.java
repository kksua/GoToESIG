package com.example.gotoesig;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TrajetDetailsActivity extends AppCompatActivity {

    private static final String TAG = "TrajetDetailsDebug";
    private static final GeoPoint END_POINT = new GeoPoint(49.395264, 1.088951); // ESIGELEC coordinates
    private static final String OPENROUTE_API_KEY = "5b3ce3597851110001cf6248f8ba093d4cd148fa8cb007926b9d95be";

    private MapView mapView;
    private GeoPoint startPoint;
    private List<GeoPoint> routePoints;
    private TextView distanceTextView, durationTextView;
    private Button selectTripButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().setUserAgentValue(getApplicationContext().getPackageName());
        setContentView(R.layout.activity_trajet_details);

        // Initialize views
        mapView = findViewById(R.id.map);
        mapView.setMultiTouchControls(true);
        distanceTextView = findViewById(R.id.distanceTextView);
        durationTextView = findViewById(R.id.durationTextView);
        selectTripButton = findViewById(R.id.button_select_trip);

        // Retrieve data from Intent
        Intent intent = getIntent();
        double startLat = intent.getDoubleExtra("startLat", 0.0);
        double startLon = intent.getDoubleExtra("startLon", 0.0);
        String documentId = intent.getStringExtra("documentId"); // ID of the selected trip
        Log.d(TAG, "Document ID received: " + documentId);

        Log.d(TAG, "Start point received: Lat = " + startLat + ", Lon = " + startLon);
        startPoint = new GeoPoint(startLat, startLon);
        routePoints = new ArrayList<>();

        // Draw markers and fetch route
        drawMarkers();
        fetchRouteFromOpenRouteService();

        // Select Trip Button functionality
        selectTripButton.setOnClickListener(v -> {
            String currentUserID = getCurrentUserID();
            if (documentId == null || currentUserID == null) {
                Toast.makeText(this, "Erreur: Informations manquantes.", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("trajet").document(documentId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            List<String> joinedUsers = (List<String>) documentSnapshot.get("joinedUsers");
                            Long placesDisponibles = documentSnapshot.getLong("placesDisponibles");

                            if (joinedUsers == null) joinedUsers = new ArrayList<>();

                            // Check if user is already joined
                            if (joinedUsers.contains(currentUserID)) {
                                Toast.makeText(this, "Vous êtes déjà associé à ce trajet.", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            // Check if places are still available
                            if (placesDisponibles != null && joinedUsers.size() >= placesDisponibles) {
                                Toast.makeText(this, "Aucune place disponible.", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            // Add user ID and update Firestore
                            joinedUsers.add(currentUserID);
                            db.collection("trajet").document(documentId)
                                    .update("joinedUsers", joinedUsers)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Trajet sélectionné avec succès!", Toast.LENGTH_SHORT).show();
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        Log.e(TAG, "Firestore Update Error: " + e.getMessage());
                                    });
                        } else {
                            Toast.makeText(this, "Trajet introuvable.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Firestore Fetch Error: " + e.getMessage());
                    });
        });
    }

    private void drawMarkers() {
        // Add start and end markers
        Marker startMarker = new Marker(mapView);
        startMarker.setPosition(startPoint);
        startMarker.setTitle("Point de départ");
        mapView.getOverlays().add(startMarker);

        Marker endMarker = new Marker(mapView);
        endMarker.setPosition(END_POINT);
        endMarker.setTitle("ESIGELEC");
        mapView.getOverlays().add(endMarker);

        mapView.getController().setZoom(12);
        mapView.getController().setCenter(startPoint);
    }

    private void fetchRouteFromOpenRouteService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openrouteservice.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        OpenRouteServiceApi service = retrofit.create(OpenRouteServiceApi.class);

        // Prepare coordinates for API call
        List<List<Double>> coordinates = new ArrayList<>();
        coordinates.add(List.of(startPoint.getLongitude(), startPoint.getLatitude()));
        coordinates.add(List.of(END_POINT.getLongitude(), END_POINT.getLatitude()));

        Log.d(TAG, "Coordinates for route: " + coordinates);

        Call<RouteResponse> call = service.getRoute(
                OPENROUTE_API_KEY,
                new RouteRequest("driving-car", coordinates)
        );

        call.enqueue(new retrofit2.Callback<RouteResponse>() {
            @Override
            public void onResponse(Call<RouteResponse> call, retrofit2.Response<RouteResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    RouteResponse routeResponse = response.body();

                    // Extract distance, duration, and encoded geometry
                    double distance = routeResponse.getRoutes().get(0).getSummary().getDistance();
                    double duration = routeResponse.getRoutes().get(0).getSummary().getDuration();
                    String encodedGeometry = routeResponse.getRoutes().get(0).getGeometry();

                    // Decode the encoded polyline
                    routePoints = PolylineDecoder.decode(encodedGeometry);

                    // Update UI
                    distanceTextView.setText(String.format(Locale.getDefault(), "Distance: %.2f km", distance / 1000));
                    durationTextView.setText(String.format(Locale.getDefault(), "Durée: %.2f min", duration / 60));

                    drawRoute();
                } else {
                    Toast.makeText(TrajetDetailsActivity.this, "Erreur: Impossible de récupérer le trajet.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RouteResponse> call, Throwable t) {
                Log.e(TAG, "API Call Failed: " + t.getMessage());
                Toast.makeText(TrajetDetailsActivity.this, "Erreur de connexion à l'API.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void drawRoute() {
        if (routePoints != null && !routePoints.isEmpty()) {
            Polyline polyline = new Polyline();
            polyline.setPoints(routePoints);
            mapView.getOverlays().add(polyline);
            mapView.invalidate();

            Log.d(TAG, "Route drawn successfully.");
        } else {
            Log.e(TAG, "No route points available to draw.");
        }
    }


    private String getCurrentUserID() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            return FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            return null;
        }
    }
}
