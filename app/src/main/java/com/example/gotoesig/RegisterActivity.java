package com.example.gotoesig;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText nomEditText, prenomEditText, emailEditText, passwordEditText, villeEditText, telephoneEditText;
    private Button registerButton;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Bind UI elements
        nomEditText = findViewById(R.id.nomEditText);
        prenomEditText = findViewById(R.id.prenomEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        villeEditText = findViewById(R.id.villeEditText);
        telephoneEditText = findViewById(R.id.telephoneEditText);
        registerButton = findViewById(R.id.registerButton);

        // Register Button Listener
        registerButton.setOnClickListener(v -> registerUser());

        // Navigate to LoginActivity
        TextView loginTextView = findViewById(R.id.textView3);
        loginTextView.setOnClickListener(v -> startActivity(new Intent(RegisterActivity.this, LoginActivity.class)));
    }

    private void registerUser() {
        String nom = nomEditText.getText().toString().trim();
        String prenom = prenomEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String ville = villeEditText.getText().toString().trim();
        String telephone = telephoneEditText.getText().toString().trim();

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || password.isEmpty() || ville.isEmpty() || telephone.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Le mot de passe doit contenir au moins 6 caractères.", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();

                            Map<String, Object> userData = new HashMap<>();
                            userData.put("id", userId);
                            userData.put("nom", nom);
                            userData.put("prenom", prenom);
                            userData.put("email", email);
                            userData.put("ville", ville);
                            userData.put("ntele", telephone);

                            db.collection("users").document(userId).set(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Enregistrement réussi.", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(this, "Erreur : " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        Toast.makeText(this, "Échec : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
