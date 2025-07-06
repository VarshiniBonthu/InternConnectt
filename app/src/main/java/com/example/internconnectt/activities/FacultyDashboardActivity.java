package com.example.internconnectt.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.internconnectt.R;
import com.example.internconnectt.adapters.FacultyInternshipAdapter;
import com.example.internconnectt.models.Internship;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class FacultyDashboardActivity extends AppCompatActivity {

    private Spinner facultyBranchSpinner;
    private EditText titleEditText, descriptionEditText, deadlineEditText, linkEditText;
    private CheckBox importantCheckBox;
    private Button submitButton;

    private RecyclerView facultyInternshipRecyclerView;
    private TextView postedInternshipsLabel;

    private DatabaseReference internshipRef;

    private List<Internship> facultyInternshipList = new ArrayList<>();
    private FacultyInternshipAdapter facultyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faculty_dashboard);

        facultyBranchSpinner = findViewById(R.id.facultyBranchSpinner);
        titleEditText = findViewById(R.id.editTextTitle);
        descriptionEditText = findViewById(R.id.editTextDescription);
        deadlineEditText = findViewById(R.id.editTextDeadline);
        linkEditText = findViewById(R.id.editTextLink);
        importantCheckBox = findViewById(R.id.checkBoxImportant);
        submitButton = findViewById(R.id.buttonSubmit);

        postedInternshipsLabel = findViewById(R.id.textViewPostedInternships);
        facultyInternshipRecyclerView = findViewById(R.id.facultyInternshipRecyclerView);

        internshipRef = FirebaseDatabase.getInstance().getReference("internships");

        // Set up RecyclerView
        facultyAdapter = new FacultyInternshipAdapter(facultyInternshipList);
        facultyInternshipRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        facultyInternshipRecyclerView.setAdapter(facultyAdapter);

        // Branch options
        String[] branches = {"Select Branch", "CSE", "AIML", "IT", "CSBS", "AIDS", "ECE", "EEE", "CIVIL", "MECH"};
        ArrayAdapter<String> branchAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, branches);
        branchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        facultyBranchSpinner.setAdapter(branchAdapter);

        // On Submit
        submitButton.setOnClickListener(view -> submitInternship());

        // Load internships when branch is selected
        facultyBranchSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String selectedBranch = parent.getItemAtPosition(pos).toString();
                if (!selectedBranch.equals("Select Branch")) {
                    loadPostedInternships(selectedBranch);
                } else {
                    postedInternshipsLabel.setVisibility(View.GONE);
                    facultyInternshipRecyclerView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void submitInternship() {
        String branch = facultyBranchSpinner.getSelectedItem().toString();
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String deadline = deadlineEditText.getText().toString().trim();
        String link = linkEditText.getText().toString().trim();
        boolean isImportant = importantCheckBox.isChecked();

        if (branch.equals("Select Branch")) {
            Toast.makeText(this, "Please select a branch", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(description) || TextUtils.isEmpty(deadline) || TextUtils.isEmpty(link)) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create internship object
        Internship internship = new Internship(title, description, branch, link, deadline, isImportant);

        // Push to Firebase under branch
        String id = internshipRef.child(branch).push().getKey();
        internshipRef.child(branch).child(id).setValue(internship)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Internship submitted successfully", Toast.LENGTH_SHORT).show();
                    clearForm();
                    loadPostedInternships(branch); // Refresh list
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadPostedInternships(String branch) {
        internshipRef.child(branch).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                facultyInternshipList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        Internship internship = postSnapshot.getValue(Internship.class);
                        if (internship != null) {
                            internship.setId(postSnapshot.getKey());
                            facultyInternshipList.add(internship);
                        }
                    }
                    facultyAdapter.notifyDataSetChanged();
                    postedInternshipsLabel.setVisibility(View.VISIBLE);
                    facultyInternshipRecyclerView.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(FacultyDashboardActivity.this, "No internships found.", Toast.LENGTH_SHORT).show();
                    postedInternshipsLabel.setVisibility(View.GONE);
                    facultyInternshipRecyclerView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(FacultyDashboardActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearForm() {
        titleEditText.setText("");
        descriptionEditText.setText("");
        deadlineEditText.setText("");
        linkEditText.setText("");
        importantCheckBox.setChecked(false);
        // Don't reset branch so user can continue adding
    }
}
