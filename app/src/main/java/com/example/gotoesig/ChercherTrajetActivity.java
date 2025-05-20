package com.example.gotoesig;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ChercherTrajetActivity extends AppCompatActivity {

    private EditText inputDepart, inputDate;
    private RecyclerView recyclerTrips;
//    private TripAdapter tripAdapter;
    private List<Trajet> trajets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chercher_trajet);

        // Initialize views
        inputDepart = findViewById(R.id.input_depart);
        inputDate = findViewById(R.id.input_date);
        recyclerTrips = findViewById(R.id.recycler_trips);
        Button buttonSearch = findViewById(R.id.button_search);

        // Initialize RecyclerView
        trajets = new ArrayList<>();
//        tripAdapter = new TripAdapter(trajets, new TripAdapter.OnTripClickListener() {
//            @Override
//            public void onTripClick(Trajet trajet) {
//                // Go to the map activity with selected trip details
//                Intent intent = new Intent(ChercherTrajetActivity.this, MapItineraireActivity.class);
//                intent.putExtra("depart", trajet.getPointDepart());
//                intent.putExtra("destination", trajet.getDestination());
//                startActivity(intent);
//            }
//        });
        recyclerTrips.setLayoutManager(new LinearLayoutManager(this));
        //recyclerTrips.setAdapter(tripAdapter);

        // Set up the search button
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchTrips();
            }
        });
    }

    private void searchTrips() {
        String depart = inputDepart.getText().toString();
        String date = inputDate.getText().toString();

        if (TextUtils.isEmpty(depart) || TextUtils.isEmpty(date)) {
            // Show an error if fields are empty
            return;
        }

        // Query Firebase for trips
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("trajet")
                .whereEqualTo("pointDepart", depart)
                .whereEqualTo("date", date)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    trajets.clear();
                    trajets.addAll(queryDocumentSnapshots.toObjects(Trajet.class));
                   // tripAdapter.notifyDataSetChanged();
                });
    }
}
