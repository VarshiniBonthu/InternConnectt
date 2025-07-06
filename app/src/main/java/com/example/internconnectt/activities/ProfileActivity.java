package com.example.internconnectt.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.internconnectt.R;
import com.example.internconnectt.adapters.AppliedInternshipTitleAdapter;
import com.example.internconnectt.models.Internship;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    // UI
    private CircleImageView profileImage;
    private TextView emailText;
    private Button logoutButton;
    private RecyclerView appliedRecyclerView;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference userRef;
    private StorageReference storageRef;

    // Recycler
    private AppliedInternshipTitleAdapter adapter;
    private final List<String> appliedTitles = new ArrayList<>();

    // Image picker
    private static final int PICK_IMAGE_REQUEST = 101;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // View Bindings
        profileImage = findViewById(R.id.profileImage);
        emailText = findViewById(R.id.emailText);
        logoutButton = findViewById(R.id.logoutButton);
        appliedRecyclerView = findViewById(R.id.appliedRecyclerView);

        appliedRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppliedInternshipTitleAdapter(this, appliedTitles);
        appliedRecyclerView.setAdapter(adapter);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        storageRef = FirebaseStorage.getInstance().getReference("profile_images");

        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        emailText.setText(currentUser.getEmail());

        userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUser.getUid());

        loadProfileInfo();

        profileImage.setOnClickListener(v -> openImagePicker());

        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            Intent i = new Intent(ProfileActivity.this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.nav_profile); // highlight current

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_dashboard) {
                if (!getClass().equals(StudentDashboardActivity.class)) {
                    startActivity(new Intent(ProfileActivity.this, StudentDashboardActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                }
                return true;
            } else if (id == R.id.nav_applied) {
                if (!getClass().equals(AppliedInternshipsActivity.class)) {
                    startActivity(new Intent(ProfileActivity.this, AppliedInternshipsActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                }
                return true;
            } else if (id == R.id.nav_profile) {
                // Already in ProfileActivity
                return true;
            }

            return false;
        });

    }

    private void loadProfileInfo() {
        // Load profile image
        userRef.child("photoUrl").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                String url = snapshot.getValue(String.class);
                Glide.with(ProfileActivity.this)
                        .load(url)
                        .placeholder(R.drawable.placeholder)
                        .into(profileImage);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });

        // Load applied internships
        String emailKey = currentUser.getEmail().replace(".", "_");
        DatabaseReference internshipsRef = FirebaseDatabase.getInstance().getReference("internships");

        internshipsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                appliedTitles.clear();
                for (DataSnapshot branchSnap : snapshot.getChildren()) {
                    for (DataSnapshot internSnap : branchSnap.getChildren()) {
                        if (internSnap.child("reactions").hasChild(emailKey)) {
                            Internship in = internSnap.getValue(Internship.class);
                            if (in != null && in.getTitle() != null) {
                                appliedTitles.add(in.getTitle());
                            }
                        }
                    }
                }
                adapter.updateList(appliedTitles);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void openImagePicker() {
        Intent i = new Intent(Intent.ACTION_PICK);
        i.setType("image/*");
        startActivityForResult(i, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == PICK_IMAGE_REQUEST && res == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            uploadImage();
        }
    }

    private void uploadImage() {
        if (imageUri == null) return;

        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Uploading...");
        pd.setCancelable(false);
        pd.show();

        StorageReference ref = storageRef.child(UUID.randomUUID().toString());

        ref.putFile(imageUri).addOnSuccessListener(t -> {
            ref.getDownloadUrl().addOnSuccessListener(uri -> {
                userRef.child("photoUrl").setValue(uri.toString());
                Glide.with(this).load(uri).into(profileImage);
                pd.dismiss();
                Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            pd.dismiss();
            Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }
}
