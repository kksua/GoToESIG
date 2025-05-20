package com.example.gotoesig;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.JsonObject;

import org.osmdroid.util.GeoPoint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AjouterActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private ArrayList<String> transportOptions;
    private Retrofit retrofit;

    private static final String OPEN_ROUTE_SERVICE_BASE_URL = "https://api.openrouteservice.org/";
    private static final String API_KEY = "5b3ce3597851110001cf6248f8ba093d4cd148fa8cb007926b9d95be";

    private static final GeoPoint END_POINT = new GeoPoint(49.395264, 1.088951); // ESIGELEC coordinates

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_ajouter);

            // Initialize Firebase Firestore and Retrofit
            db = FirebaseFirestore.getInstance();
            transportOptions = new ArrayList<>();
            retrofit = new Retrofit.Builder()
                    .baseUrl(OPEN_ROUTE_SERVICE_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            // Handle Edge-to-Edge UI padding
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });

            // Find views
            EditText dateField = findViewById(R.id.dateField);
            EditText timeField = findViewById(R.id.heureField);
            Spinner moyenTransportSpinner = findViewById(R.id.moyenTransport);
            EditText pointDepart = findViewById(R.id.pointDepart);
            ImageView backButton = findViewById(R.id.backButton);
            Button ajouterTrajetButton = findViewById(R.id.ajouterTrajetButton);
            TextView contributionTextView = findViewById(R.id.contribution);
            EditText retardEditText = findViewById(R.id.enEuro);

            contributionTextView.setVisibility(View.INVISIBLE);
            retardEditText.setVisibility(View.INVISIBLE);

            moyenTransportSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedTransport = parent.getItemAtPosition(position).toString();
                    if (selectedTransport.equalsIgnoreCase("véhicule")) {
                        contributionTextView.setVisibility(View.VISIBLE);
                        retardEditText.setVisibility(View.VISIBLE);
                    } else {
                        contributionTextView.setVisibility(View.INVISIBLE);
                        retardEditText.setVisibility(View.INVISIBLE);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });

            dateField.setOnClickListener(v -> showDatePicker(dateField));
            timeField.setOnClickListener(v -> showTimePicker(timeField));

            backButton.setOnClickListener(v -> {
                startActivity(new Intent(AjouterActivity.this, HomeActivity.class));
                finish();
            });

            fetchTransportOptions(moyenTransportSpinner);

            ajouterTrajetButton.setOnClickListener(v -> {
                String date = dateField.getText().toString();
                String time = timeField.getText().toString();
                String pointDepartText = pointDepart.getText().toString();
                String moyenTransportText = moyenTransportSpinner.getSelectedItem().toString();

                // Initialize contribution as a string value
                String contributionText = null;

                // Check if the selected transport requires contribution
                if (moyenTransportText.equalsIgnoreCase("véhicule")) {
                    contributionText = retardEditText.getText().toString();
                    if (contributionText.isEmpty()) {
                        Toast.makeText(this, "Veuillez saisir une contribution pour le véhicule.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                if (date.isEmpty() || time.isEmpty() || pointDepartText.isEmpty() || moyenTransportText.isEmpty()) {
                    Toast.makeText(this, "Veuillez remplir tous les champs.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                if (userId == null) {
                    Toast.makeText(this, "Utilisateur non authentifié!", Toast.LENGTH_SHORT).show();
                    return;
                }

                String finalContributionText = contributionText; // Final string to pass into the dialog
                OpenRouteServiceApi service = retrofit.create(OpenRouteServiceApi.class);
                service.geocodeAddress(API_KEY, pointDepartText).enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            JsonObject location = response.body()
                                    .getAsJsonArray("features").get(0)
                                    .getAsJsonObject().getAsJsonObject("geometry");

                            double latitude = location.getAsJsonArray("coordinates").get(1).getAsDouble();
                            double longitude = location.getAsJsonArray("coordinates").get(0).getAsDouble();

                            calculateRouteAndShowDialog(service, latitude, longitude, userId, date, time, pointDepartText, moyenTransportText, finalContributionText);
                        } else {
                            Toast.makeText(AjouterActivity.this, "Erreur de géocodage.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        Toast.makeText(AjouterActivity.this, "Erreur de connexion.", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        } catch (Exception e) {
            Log.e("AjouterActivity", "Error initializing the activity", e);
        }
    }

    private void calculateRouteAndShowDialog(OpenRouteServiceApi service, double lat, double lon, String userId,
                                             String date, String time, String pointDepart, String moyenTransport, String contribution) {
        List<List<Double>> coordinates = new ArrayList<>();
        coordinates.add(List.of(lon, lat));
        coordinates.add(List.of(END_POINT.getLongitude(), END_POINT.getLatitude()));

        service.getRoute(API_KEY, new RouteRequest("driving-car", coordinates))
                .enqueue(new Callback<RouteResponse>() {
                    @Override
                    public void onResponse(Call<RouteResponse> call, Response<RouteResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            RouteResponse routeResponse = response.body();
                            double distance = routeResponse.getRoutes().get(0).getSummary().getDistance() / 1000; // km
                            double duration = routeResponse.getRoutes().get(0).getSummary().getDuration() / 60; // min

                            showConfirmationDialog(lat, lon, userId, date, time, pointDepart, moyenTransport, distance, duration, contribution);
                        } else {
                            Toast.makeText(AjouterActivity.this, "Erreur de calcul d'itinéraire.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<RouteResponse> call, Throwable t) {
                        Toast.makeText(AjouterActivity.this, "Erreur de connexion API.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showConfirmationDialog(double lat, double lon, String userId, String date, String time,
                                        String pointDepart, String moyenTransport, double distance, double duration, String contribution) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmation du trajet");

        String message = String.format(Locale.getDefault(),
                "Point de départ: %s\nDistance: %.2f km\nDurée: %.2f minutes\nContribution: %s €\n\nVoulez-vous confirmer ce trajet ?",
                pointDepart, distance, duration, contribution != null ? contribution : "Non spécifiée");

        builder.setMessage(message);
        builder.setPositiveButton("Confirmer", (dialog, which) ->
                saveTripToFirestore(lat, lon, userId, date, time, pointDepart, moyenTransport, distance, duration, contribution));
        builder.setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void saveTripToFirestore(double lat, double lon, String userId, String date, String time,
                                     String pointDepart, String moyenTransport, double distance, double duration, String contribution) {
        Map<String, Object> tripData = new HashMap<>();
        try {
            Date parsedDate = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).parse(date + " " + time);
            tripData.put("date/time", new Timestamp(parsedDate));
        } catch (ParseException e) {
            Toast.makeText(this, "Erreur de formatage de la date/heure.", Toast.LENGTH_SHORT).show();
            return;
        }

        tripData.put("geoPoint", List.of(lat, lon));
        tripData.put("userID", userId);
        tripData.put("pointDepart", pointDepart);
        tripData.put("moyenTransport", moyenTransport);
        tripData.put("distance", distance);
        tripData.put("duree", duration);

        // Add contribution if provided
        if (contribution != null) {
            tripData.put("contribution", contribution);
        }

        db.collection("trajet").add(tripData)
                .addOnSuccessListener(doc -> Toast.makeText(this, "Trajet ajouté avec succès!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Erreur d'ajout du trajet.", Toast.LENGTH_SHORT).show());
    }

    private void showDatePicker(EditText dateField) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            dateField.setText(String.format(Locale.getDefault(), "%02d/%02d/%d", day, month + 1, year));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker(EditText timeField) {
        Calendar calendar = Calendar.getInstance();
        new TimePickerDialog(this, (view, hour, minute) -> {
            timeField.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
    }

    private void fetchTransportOptions(Spinner spinner) {
        db.collection("moyenDeTransport").get().addOnSuccessListener(query -> {
            transportOptions.clear();
            for (QueryDocumentSnapshot doc : query) {
                transportOptions.add(doc.getString("nom"));
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, transportOptions);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
        });
    }
}
