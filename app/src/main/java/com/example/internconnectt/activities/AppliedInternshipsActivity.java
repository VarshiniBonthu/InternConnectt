package com.example.internconnectt.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.internconnectt.R;
import com.example.internconnectt.adapters.InternshipAdapter;
import com.example.internconnectt.models.Internship;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class AppliedInternshipsActivity extends AppCompatActivity {

    private RecyclerView appliedRecyclerView;
    private InternshipAdapter adapter;
    private List<Internship> appliedList = new ArrayList<>();
    private FirebaseUser user;
    private String userEmailKey;
    private BottomNavigationView bottomNavigationView;
    private TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_applied_internships);

        // Views
        appliedRecyclerView = findViewById(R.id.appliedRecyclerView);
        title = findViewById(R.id.appliedTitle);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Setup RecyclerView
        adapter = new InternshipAdapter(this, appliedList);
        appliedRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        appliedRecyclerView.setAdapter(adapter);

        // Firebase
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            userEmailKey = user.getEmail().replace(".", "_");
            fetchAppliedInternships();
        } else {
            Toast.makeText(this, "Not logged in!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        // Set bottom navigation
        bottomNavigationView.setSelectedItemId(R.id.nav_applied);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) {
                startActivity(new Intent(this, StudentDashboardActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_applied) {
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    private void fetchAppliedInternships() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("internships");

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                appliedList.clear();
                for (DataSnapshot branchSnap : snapshot.getChildren()) {
                    for (DataSnapshot internshipSnap : branchSnap.getChildren()) {
                        Internship internship = internshipSnap.getValue(Internship.class);
                        if (internship != null) {
                            internship.setId(internshipSnap.getKey());
                            if (internshipSnap.hasChild("reactions")) {
                                if (internshipSnap.child("reactions").hasChild(userEmailKey)) {
                                    appliedList.add(internship);
                                }
                            }
                        }
                    }
                }

                if (appliedList.isEmpty()) {
                    Toast.makeText(AppliedInternshipsActivity.this, "You haven’t applied to any internships yet.", Toast.LENGTH_SHORT).show();
                }
                adapter.updateList(appliedList);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(AppliedInternshipsActivity.this, "Failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
