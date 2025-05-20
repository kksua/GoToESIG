package com.example.gotoesig;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText;
    private Button loginButton;
    private TextView forgotPasswordText, createAccountText;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance();

        // Bind UI elements to variables
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);
        createAccountText = findViewById(R.id.textView3);

        // Login Button Click Listener
        loginButton.setOnClickListener(v -> loginUser());

        // Navigate to Password Reset Activity
        forgotPasswordText.setOnClickListener(v -> resetPassword());

        // Navigate to RegisterActivity
        createAccountText.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String email = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Input validation
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Firebase Authentication to log in user
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Connexion réussie.", Toast.LENGTH_SHORT).show();

                        // Navigate to HomeActivity after successful login
                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish(); // Close LoginActivity
                    } else {
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Erreur inconnue";
                        Toast.makeText(this, "Échec de la connexion : " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void resetPassword() {
        String email = usernameEditText.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Veuillez entrer votre adresse e-mail.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Firebase Password Reset
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "E-mail de réinitialisation envoyé.", Toast.LENGTH_SHORT).show();
                    } else {
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Erreur inconnue";
                        Toast.makeText(this, "Erreur lors de la réinitialisation : " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }
}
