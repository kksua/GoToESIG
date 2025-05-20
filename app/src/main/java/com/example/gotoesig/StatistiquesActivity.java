package com.example.gotoesig;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class StatistiquesActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private TextView tripsCountTextView, totalAmountTextView;
    private ImageView backButton;

    private Button backButton1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistiques);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Bind UI elements
        tripsCountTextView = findViewById(R.id.tripsCountTextView);
        totalAmountTextView = findViewById(R.id.totalAmountTextView);
        backButton = findViewById(R.id.backButton);
        backButton1 = findViewById(R.id.backButton1);

        // Fetch statistics for the current user
        fetchStatistics();

        // Back button functionality
        // Back button functionality (for both buttons)
        View.OnClickListener backButtonClickListener = v -> {
            Intent intent = new Intent(StatistiquesActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        };

        // Set click listeners for both back buttons
        backButton.setOnClickListener(backButtonClickListener);
        backButton1.setOnClickListener(backButtonClickListener);
    }

    private void fetchStatistics() {
        String userId = auth.getCurrentUser().getUid();
        if (userId == null) {
            Toast.makeText(this, "Utilisateur non authentifié !", Toast.LENGTH_SHORT).show();
            return;
        }

        // Query Firestore for trips proposed by the user
        db.collection("trajet")
                .whereEqualTo("userID", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        int tripCount = 0;
                        double totalAmount = 0.0;

                        // Iterate through the user's trips
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            tripCount++; // Increment trip count

                            // Add the amount collected from the trip (if available)
                            Double amount = document.getDouble("montantEncaisse");
                            if (amount != null) {
                                totalAmount += amount;
                            }
                        }

                        // Update UI with statistics
                        tripsCountTextView.setText("Nombre de trajets proposés : " + tripCount);
                        totalAmountTextView.setText("Total encaissé : " + String.format("%.2f", totalAmount) + " €");
                    } else {
                        Toast.makeText(this, "Erreur lors du chargement des statistiques.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
