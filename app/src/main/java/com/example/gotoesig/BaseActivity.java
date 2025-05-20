package com.example.gotoesig;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public abstract class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // This method is called in activities extending BaseActivity to ensure menu is set up.
        setupDrawer();
    }

    protected void setupDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);

        if (drawerLayout != null && navigationView != null) {
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this,
                    drawerLayout,
                    toolbar,
                    R.string.navigation_drawer_open,
                    R.string.navigation_drawer_close
            );

            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();

            navigationView.setNavigationItemSelectedListener(this);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        Intent intent = null; // Declare an intent to handle navigation
        if (itemId == R.id.nav_profile) {
            intent = new Intent(this, MonProfilActivity.class);
        } else if (itemId == R.id.nav_add_trip) {
            intent = new Intent(this, AjouterActivity.class);
        } else if (itemId == R.id.nav_my_trips) {
            intent = new Intent(this, MesTrajetsActivity2.class);
        } else if (itemId == R.id.nav_chercher_trajet) {
            intent = new Intent(this, SearchTrajetsActivity.class);
        } else if (itemId == R.id.nav_statistics) {
            intent = new Intent(this, StatistiquesActivity.class);
        } else if (itemId == R.id.nav_logout) {
            logoutUser();
            return true;
        } else {
            Toast.makeText(this, "Option non gérée : " + item.getTitle(), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (intent != null) {
            startActivity(intent);
        }

        if (drawerLayout != null) {
            drawerLayout.closeDrawers(); // Close the drawer after selection
        }
        return true;
    }



    private void logoutUser() {
        getSharedPreferences("user_session", MODE_PRIVATE)
                .edit()
                .clear()
                .apply();

        Toast.makeText(this, "Vous êtes déconnecté.", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
