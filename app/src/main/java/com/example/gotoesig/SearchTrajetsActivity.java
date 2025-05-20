package com.example.gotoesig;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SearchTrajetsActivity extends AppCompatActivity {

    private static final String API_KEY = "5b3ce3597851110001cf6248f8ba093d4cd148fa8cb007926b9d95be"; // Replace with your API key
    private static final String TAG = "SearchTrajetsDebug";

    private EditText inputDepart, inputDate;
    private Button searchButton;
    private RecyclerView recyclerView;
    private TrajetAdapter adapter;
    private List<Trajet> trajets;
    private FirebaseFirestore db;
    private OpenRouteServiceApi service;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_trajets);

        // Initialize views
        inputDepart = findViewById(R.id.input_depart);
        inputDate = findViewById(R.id.input_date);
        searchButton = findViewById(R.id.button_search);
        recyclerView = findViewById(R.id.recycler_trips);
        backButton = findViewById(R.id.backButton);
        trajets = new ArrayList<>();
        adapter = new TrajetAdapter(trajets);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();

        // Set up Retrofit for OpenRouteService
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openrouteservice.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(OpenRouteServiceApi.class);

        // Set up date picker for the date input field
        inputDate.setOnClickListener(v -> showDatePicker());

        // Set up the search button functionality
        searchButton.setOnClickListener(v -> searchTrajets());

        // Back button functionality
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(SearchTrajetsActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void showDatePicker() {
        Locale locale = new Locale("fr");
        Locale.setDefault(locale);
        Calendar calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String formattedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                    inputDate.setText(formattedDate);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void searchTrajets() {
        String pointDepartText = inputDepart.getText().toString().trim();
        String dateString = inputDate.getText().toString().trim();

        if (dateString.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir la date.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert dateString to Timestamp
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            Date date = dateFormat.parse(dateString);
            if (date == null) {
                Toast.makeText(this, "Date invalide.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create startOfDay Timestamp for comparison
            long startMillis = date.getTime();
            Timestamp startOfDay = new Timestamp(startMillis / 1000, 0);

            Log.d(TAG, "Timestamp Start: " + startOfDay.toDate());

            if (!pointDepartText.isEmpty()) {
                geocodePointDepartAndCompare(pointDepartText, startOfDay);
            } else {
                fetchTripsByExactDate(startOfDay);
            }
        } catch (ParseException e) {
            Log.e(TAG, "Format de date invalide", e);
            Toast.makeText(this, "Format de date invalide.", Toast.LENGTH_SHORT).show();
        }
    }


    private void geocodePointDepartAndCompare(String pointDepartText, Timestamp startOfDay) {
        Call<JsonObject> call = service.geocodeAddress(API_KEY, pointDepartText);
        call.enqueue(new retrofit2.Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, retrofit2.Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject responseBody = response.body();
                    JsonObject features = responseBody.getAsJsonArray("features").get(0).getAsJsonObject();
                    JsonObject geometry = features.getAsJsonObject("geometry");
                    JsonArray coordinates = geometry.getAsJsonArray("coordinates");

                    double lon = coordinates.get(0).getAsDouble();
                    double lat = coordinates.get(1).getAsDouble();

                    Log.d(TAG, "Geocoded Latitude: " + lat);
                    Log.d(TAG, "Geocoded Longitude: " + lon);

                    filterTripsByDateAndLocation(lat, lon, startOfDay);
                } else {
                    Log.e(TAG, "Failed to fetch geocode: " + response.errorBody());
                    Toast.makeText(SearchTrajetsActivity.this, "Erreur lors de la géocodage.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e(TAG, "Geocode API call failed", t);
                Toast.makeText(SearchTrajetsActivity.this, "Échec de la géocodage.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchTripsByExactDate(Timestamp startOfDay) {
        db.collection("trajet")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        trajets.clear();
                        Log.d(TAG, "Firestore Query Successful: " + task.getResult().size() + " documents fetched.");

                        String startOfDayString = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                .format(startOfDay.toDate()).trim();
                        Log.d(TAG, "startOfDayString: " + startOfDayString);

                        for (DocumentSnapshot document : task.getResult()) {
                            Log.d(TAG, "Document ID: " + document.getId());
                            Timestamp tripTimestamp = document.get(FieldPath.of("date/time"), Timestamp.class);
                            if (tripTimestamp != null) {
                                String tripDateString = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                        .format(tripTimestamp.toDate()).trim();

                                Log.d(TAG, "Comparing Dates: Firestore Date = " + tripDateString + ", Input Date = " + startOfDayString);

                                if (tripDateString.equals(startOfDayString)) {
                                    Log.d(TAG, "Match Found: " + tripDateString);
                                    Trajet trajet = document.toObject(Trajet.class);
                                    if (trajet != null) {
                                        trajet.setDateAndHeureFromTimestamp(tripTimestamp); // Splits Timestamp into date and heure
                                        trajets.add(trajet);
                                    }
                                } else {
                                    Log.d(TAG, "No Match: " + tripDateString + " != " + startOfDayString);
                                }
                            } else {
                                Log.d(TAG, "Trip Timestamp Missing or Invalid in Firestore");
                            }
                        }

                        if (trajets.isEmpty()) {
                            Toast.makeText(this, "Aucun trajet trouvé pour cette date.", Toast.LENGTH_SHORT).show();
                        } else {
                            adapter.notifyDataSetChanged();
                            Toast.makeText(this, trajets.size() + " trajets trouvés.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Erreur lors de la récupération des trajets", task.getException());
                        Toast.makeText(this, "Erreur lors de la récupération des trajets.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void filterTripsByDateAndLocation(double inputLat, double inputLon, Timestamp startOfDay) {
        db.collection("trajet").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                trajets.clear();
                Log.d(TAG, "Firestore Query Successful: " + task.getResult().size() + " documents fetched.");

                String startOfDayString = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        .format(startOfDay.toDate()).trim();

                for (DocumentSnapshot document : task.getResult()) {
                    Log.d(TAG, "Document ID: " + document.getId());

                    Timestamp tripTimestamp = document.get(FieldPath.of("date/time"), Timestamp.class);
                    if (tripTimestamp != null) {
                        String tripDateString = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                .format(tripTimestamp.toDate()).trim();

                        Object geoPointObject = document.get("geoPoint");
                        if (geoPointObject instanceof List<?>) {
                            List<?> geoPointArray = (List<?>) geoPointObject;

                            if (geoPointArray.size() == 2 && geoPointArray.get(0) instanceof Double && geoPointArray.get(1) instanceof Double) {
                                double dbLat = (Double) geoPointArray.get(0);
                                double dbLon = (Double) geoPointArray.get(1);

                                float[] results = new float[1];
                                Location.distanceBetween(inputLat, inputLon, dbLat, dbLon, results);
                                float distance = results[0];

                                Log.d(TAG, "Distance to GeoPoint: " + distance + " meters");

                                if (distance <= 500 && tripDateString.equals(startOfDayString)) {
                                    Log.d(TAG, "Matched GeoPoint and Date: Distance = " + distance + ", Date = " + tripDateString);
                                    Trajet trajet = document.toObject(Trajet.class);

                                    trajet.setLatitude(dbLat);
                                    trajet.setLongitude(dbLon);
                                    trajet.setdocumentId(document.getId());

                                    trajets.add(trajet);
                                } else {
                                    Log.d(TAG, "No Match for GeoPoint or Date. Distance: " + distance + ", Date: " + tripDateString);
                                }
                            } else {
                                Log.d(TAG, "Invalid GeoPoint in Firestore Document");
                            }
                        } else {
                            Log.d(TAG, "GeoPoint Missing or Invalid");
                        }
                    } else {
                        Log.d(TAG, "Trip Timestamp Missing or Invalid 1");
                    }
                }

                if (trajets.isEmpty()) {
                    Toast.makeText(this, "Aucun trajet trouvé pour les critères donnés.", Toast.LENGTH_SHORT).show();
                } else {
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, trajets.size() + " trajets trouvés.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(TAG, "Erreur lors de la récupération des trajets", task.getException());
                Toast.makeText(this, "Erreur lors de la récupération des trajets.", Toast.LENGTH_SHORT).show();
            }
        });
    }

}


