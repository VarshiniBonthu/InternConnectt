package com.example.internconnectt.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.internconnectt.R;
import com.example.internconnectt.adapters.InternshipAdapter;
import com.example.internconnectt.models.Internship;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.content.Intent;

public class StudentDashboardActivity extends AppCompatActivity {




    private Spinner branchSpinner;
    private RecyclerView internshipRecyclerView;
    private TextView emptyMessage;

    private InternshipAdapter adapter;
    private List<Internship> internshipList = new ArrayList<Internship>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        branchSpinner = findViewById(R.id.branchSpinner);
        internshipRecyclerView = findViewById(R.id.internshipRecyclerView);
        emptyMessage = findViewById(R.id.emptyMessage);
        BottomNavigationView bottomNavigationView= findViewById(R.id.bottomNavigationView);

        // Set up RecyclerView
        adapter = new InternshipAdapter(this,new ArrayList<Internship>());
        internshipRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        internshipRecyclerView.setAdapter(adapter);

        // Highlight the current item (Dashboard)
        bottomNavigationView.setSelectedItemId(R.id.nav_dashboard);

        // Handle item selections
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_dashboard) {
                // Already on Dashboard – do nothing or refresh
                return true;
            } else if (itemId == R.id.nav_applied) {
                // Go to AppliedInternshipsActivity (you can create this later)
                startActivity(new Intent(StudentDashboardActivity.this, AppliedInternshipsActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                // Go to ProfileActivity (you can create this later)
                startActivity(new Intent(StudentDashboardActivity.this, ProfileActivity.class));
                return true;
            }
            return false;
        });
        // Branch list
        String[] branches = {
                "Select Branch", "CSE", "AIML", "IT", "CSBS", "AIDS",
                "ECE", "EEE", "CIVIL", "MECH"
        };

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, branches);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        branchSpinner.setAdapter(spinnerAdapter);

        // Spinner selection listener
        branchSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String selectedBranch = parent.getItemAtPosition(pos).toString();

                if (selectedBranch.equals("Select Branch")) {
                    internshipRecyclerView.setVisibility(View.GONE);
                    emptyMessage.setText("Please select your branch to view internships.");
                    emptyMessage.setVisibility(View.VISIBLE);
                } else {
                    fetchInternshipsFromFirebase(selectedBranch);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

    }
    private void fetchInternshipsFromFirebase(String branch) {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("internships").child(branch);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                internshipList.clear();

                if (snapshot.exists()) {
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        Internship internship = postSnapshot.getValue(Internship.class);
                        if (internship != null) {
                            internship.setId(postSnapshot.getKey()); // ✅ Correct ID set
                            internshipList.add(internship);
                        }
                    }

                    if (internshipList.isEmpty()) {
                        internshipRecyclerView.setVisibility(View.GONE);
                        emptyMessage.setVisibility(View.VISIBLE);
                        emptyMessage.setText("No internships found for this branch.");
                    } else {
                        adapter.updateList(internshipList);
                        internshipRecyclerView.setVisibility(View.VISIBLE);
                        emptyMessage.setVisibility(View.GONE);
                    }
                } else {
                    internshipRecyclerView.setVisibility(View.GONE);
                    emptyMessage.setVisibility(View.VISIBLE);
                    emptyMessage.setText("No internships available for this branch.");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                internshipRecyclerView.setVisibility(View.GONE);
                emptyMessage.setVisibility(View.VISIBLE);
                emptyMessage.setText("Error: " + error.getMessage());
            }
        });
    }
}

