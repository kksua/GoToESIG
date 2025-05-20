package com.example.gotoesig;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class MesTrajetsActivity2 extends AppCompatActivity {

    private FirebaseFirestore db;
    private LinearLayout trajetContainer;
    private TextView noTripsMessage;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mes_trajets2);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Reference to the LinearLayout inside the ScrollView and TextView for no trips message
        trajetContainer = findViewById(R.id.trajetContainer);
        noTripsMessage = findViewById(R.id.noTripsMessage);
        backButton = findViewById(R.id.backButton);

        // Fetch data from Firestore
        fetchTrajetsData();

        // Set back button functionality
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(MesTrajetsActivity2.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void fetchTrajetsData() {
        String currentUserID = getCurrentUserID();

        // Query Firestore for documents matching the current user's ID in "userID" or "joinedUsers"
        db.collection("trajet")
                .whereEqualTo("userID", currentUserID) // Check userID matches
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean isEmpty = true;

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            isEmpty = false;

                            // Inflate a new layout for each "trajet"
                            View trajetView = LayoutInflater.from(this).inflate(R.layout.item_trajet, trajetContainer, false);

                            // Retrieve the Timestamp and format it
                            Timestamp timestamp = document.get(FieldPath.of("date/time"), Timestamp.class);
                            String formattedDate = "--";
                            String formattedTime = "--";
                            if (timestamp != null) {
                                formattedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                        .format(timestamp.toDate());
                                formattedTime = new SimpleDateFormat("HH:mm", Locale.getDefault())
                                        .format(timestamp.toDate());
                            }

                            // Populate the item view with Firestore data
                            ((TextView) trajetView.findViewById(R.id.dateTime)).setText(formattedDate);
                            ((TextView) trajetView.findViewById(R.id.Time)).setText(formattedTime);
                            ((TextView) trajetView.findViewById(R.id.distance))
                                    .setText(document.get("distance") != null ? String.valueOf(document.getLong("distance")) : "Distance non fournie");
                            ((TextView) trajetView.findViewById(R.id.pointDepart))
                                    .setText(document.getString("pointDepart") != null ? document.getString("pointDepart") : "Point de départ non fourni");
                            ((TextView) trajetView.findViewById(R.id.moyenTransport))
                                    .setText(document.getString("moyenTransport") != null ? document.getString("moyenTransport") : "Transport non fourni");
                            ((TextView) trajetView.findViewById(R.id.duree))
                                    .setText(document.get("duree") != null ? String.valueOf(document.getLong("duree")) : "Durée non fournie");

                            // Add the filled item view to the container
                            trajetContainer.addView(trajetView);
                        }

                        // Show or hide the "no trips" message based on data availability
                        if (isEmpty) {
                            noTripsMessage.setVisibility(View.VISIBLE);
                            noTripsMessage.setText("Aucun trajet trouvé.");
                        } else {
                            noTripsMessage.setVisibility(View.GONE);
                        }
                    } else {
                        // Handle failure to fetch data
                        noTripsMessage.setVisibility(View.VISIBLE);
                        noTripsMessage.setText("Erreur lors du chargement des trajets.");
                    }
                })
                .addOnFailureListener(e -> {
                    noTripsMessage.setVisibility(View.VISIBLE);
                    noTripsMessage.setText("Erreur : " + e.getMessage());
                });

        // Second query for joinedUsers array
        db.collection("trajet")
                .whereArrayContains("joinedUsers", currentUserID) // Check joinedUsers array
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        View trajetView = LayoutInflater.from(this).inflate(R.layout.item_trajet, trajetContainer, false);

                        Timestamp timestamp = document.get(FieldPath.of("date/time"), Timestamp.class);
                        String formattedDate = timestamp != null ? new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                .format(timestamp.toDate()) : "--";
                        String formattedTime = timestamp != null ? new SimpleDateFormat("HH:mm", Locale.getDefault())
                                .format(timestamp.toDate()) : "--";

                        ((TextView) trajetView.findViewById(R.id.dateTime)).setText(formattedDate);
                        ((TextView) trajetView.findViewById(R.id.Time)).setText(formattedTime);
                        ((TextView) trajetView.findViewById(R.id.pointDepart)).setText(document.getString("pointDepart"));
                        ((TextView) trajetView.findViewById(R.id.moyenTransport)).setText(document.getString("moyenTransport"));

                        trajetContainer.addView(trajetView);
                    }
                });
    }

    private String getCurrentUserID() {
        // Placeholder: Replace this with actual Firebase Auth logic
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            return FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            return "";
        }
    }
}
