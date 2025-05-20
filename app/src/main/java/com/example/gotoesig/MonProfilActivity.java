package com.example.gotoesig;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MonProfilActivity extends AppCompatActivity {

    private ImageView profileImage, backButton;
    private ImageButton editImageButton;
    private TextView modifyTextView;

    private static final int PICK_IMAGE_REQUEST = 1;

    private final String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
            FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mon_profil);

        // Initialize views
        profileImage = findViewById(R.id.profile_image);
        backButton = findViewById(R.id.backButton); // Reference to your back button ImageView
        editImageButton = findViewById(R.id.edit_image_button);
        modifyTextView = findViewById(R.id.textView8);

        // Load user information from Firestore
        if (userId != null) {
            loadUserData();
        } else {
            Toast.makeText(this, "Utilisateur non connecté.", Toast.LENGTH_SHORT).show();
            finish(); // Close activity if no user is logged in
        }

        // Set listeners for updating the profile image
        editImageButton.setOnClickListener(view -> openImageChooser());
        modifyTextView.setOnClickListener(view -> openImageChooser());

        // Set listener for back button
        backButton.setOnClickListener(view -> {
            Intent intent = new Intent(MonProfilActivity.this, HomeActivity.class);
            startActivity(intent);
            finish(); // Close MonProfilActivity to prevent unnecessary back stack
        });
    }

    private void loadUserData() {
        // Reference to the user document in Firestore
        db.collection("users").document(userId).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        // Retrieve user data
                        String username = document.getString("username");
                        String nom = document.getString("nom");
                        String prenom = document.getString("prenom");
                        String phone = document.getString("ntele");
                        String city = document.getString("ville");
                        String base64Image = document.getString("image");

                        // Update the UI
                        String displayName;
                        if (nom != null && prenom != null) {
                            displayName = nom + " " + prenom; // Combine nom and prenom
                        } else if (username != null) {
                            displayName = username; // Use username if nom and prenom are not available
                        } else {
                            displayName = "Nom non disponible"; // Fallback message
                        }
                        ((TextView) findViewById(R.id.name_text)).setText(displayName);                        ((TextView) findViewById(R.id.phone_text)).setText(phone != null ? phone : "Non fourni");
                        ((TextView) findViewById(R.id.city_text)).setText(city != null ? city : "Non fourni");

                        // Decode and display the Base64 image
                        if (base64Image != null && !base64Image.isEmpty()) {
                            Bitmap bitmap = decodeBase64ToImage(base64Image);
                            profileImage.setImageBitmap(bitmap);
                        } else {
                            profileImage.setImageResource(R.drawable.profile); // Default profile image
                        }
                    } else {
                        Toast.makeText(this, "Utilisateur introuvable.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Erreur : " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void openImageChooser() {
        // Open image picker
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Sélectionnez une image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();

            try {
                // Convert the selected image to a Bitmap
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                profileImage.setImageBitmap(bitmap); // Display selected image
                uploadImageToFirestore(bitmap); // Upload Base64 image to Firestore
            } catch (IOException e) {
                Toast.makeText(this, "Erreur lors de la sélection de l'image : " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadImageToFirestore(Bitmap bitmap) {
        // Convert the Bitmap to a Base64 string
        String base64Image = encodeImageToBase64(bitmap);

        // Save the Base64 image string to Firestore
        db.collection("users").document(userId)
                .update("image", base64Image)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Image enregistrée avec succès.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erreur lors de l'enregistrement de l'image : " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private String encodeImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos); // Compress the bitmap to reduce size
        byte[] byteArray = baos.toByteArray(); // Convert to byte array
        return android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT); // Encode to Base64 string
    }

    private Bitmap decodeBase64ToImage(String base64Image) {
        byte[] decodedString = android.util.Base64.decode(base64Image, android.util.Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }
}
