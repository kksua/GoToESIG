package com.example.gotoesig;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.cardview.widget.CardView;

public class HomeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Set up the drawer specifically for this activity
        setupDrawer();

        // Find the card for "Ajouter un trajet"
        CardView cardAddTrip = findViewById(R.id.card_add_trip);

        // Set an onClickListener on the card
        cardAddTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create an intent to navigate to AjouterTrajetActivity
                Intent intent = new Intent(HomeActivity.this, AjouterActivity.class);
                startActivity(intent);
            }
        });

        // Find the card for "Mes trajets"
        CardView cardMyTrips = findViewById(R.id.card_my_trips);

        // Set an onClickListener on the card
        cardMyTrips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create an intent to navigate to MesTrajetsActivity2
                Intent intent = new Intent(HomeActivity.this, MesTrajetsActivity2.class);
                startActivity(intent);
            }
        });

        // Find the card for "Mes trajets"
        CardView cardMyProfile = findViewById(R.id.card_profile);

        // Set an onClickListener on the card
        cardMyProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create an intent to navigate to MesTrajetsActivity2
                Intent intent = new Intent(HomeActivity.this, MonProfilActivity.class);
                startActivity(intent);
            }
        });

        // Find the card for "Mes trajets"
        CardView cardSearchTrip = findViewById(R.id.card_search_trip);

        // Set an onClickListener on the card
        cardSearchTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create an intent to navigate to MesTrajetsActivity2
                Intent intent = new Intent(HomeActivity.this, SearchTrajetsActivity.class);
                startActivity(intent);
            }
        });

        // Find the card for "Mes statistiques"
        CardView cardStatistiques = findViewById(R.id.card_statistics);

        // Set an onClickListener on the card
        cardStatistiques.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create an intent to navigate to MesTrajetsActivity2
                Intent intent = new Intent(HomeActivity.this, StatistiquesActivity.class);
                startActivity(intent);
            }
        });
    }
}
