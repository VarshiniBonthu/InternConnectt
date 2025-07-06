package com.example.internconnectt.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.internconnectt.R;
import com.example.internconnectt.models.Internship;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class InternshipAdapter extends RecyclerView.Adapter<InternshipAdapter.InternshipViewHolder> {

    private Context context;
    private List<Internship> internshipList;

    public InternshipAdapter(Context context, List<Internship> internshipList) {
        this.context = context;
        this.internshipList = internshipList;
    }

    @NonNull
    @Override
    public InternshipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_internship, parent, false);
        return new InternshipViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InternshipViewHolder holder, int position) {
        Internship internship = internshipList.get(position);

        holder.titleText.setText(internship.getTitle());
        holder.descText.setText(internship.getDescription());
        holder.branchText.setText("Branch: " + internship.getBranch());
        holder.reactMessage.setText("React with thumbs up if you applied");

        // Format deadline
        String rawDeadline = internship.getDeadline();
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(rawDeadline);
            String formatted = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date);
            holder.deadlineText.setText("Deadline: " + formatted);
        } catch (ParseException e) {
            holder.deadlineText.setText("Deadline: " + rawDeadline);
        }

        // Highlight if important
        if (internship.isImportant()) {
            holder.titleText.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
        } else {
            holder.titleText.setTextColor(context.getResources().getColor(android.R.color.black));
        }

        // Apply Button: opens link
        holder.applyButton.setOnClickListener(view -> {
            String url = internship.getLink();
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "http://" + url;
            }
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(intent);

            // Save to Firebase under appliedStudents
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                String uid = user.getUid();
                String email = user.getEmail();
                String internshipId = internship.getId();
                String branch = internship.getBranch();

                DatabaseReference dbRef = FirebaseDatabase.getInstance()
                        .getReference("internships")
                        .child(branch)
                        .child(internshipId)
                        .child("appliedStudents");

                dbRef.child(uid).setValue(email);
            }
        });

        // Thumbs Up Button
        holder.thumbsUpButton.setOnClickListener(view -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                String email = user.getEmail();
                String safeEmail = email.replace(".", "_");
                String internshipId = internship.getId();
                String branch = internship.getBranch();

                DatabaseReference thumbsRef = FirebaseDatabase.getInstance()
                        .getReference("internships")
                        .child(branch)
                        .child(internshipId)
                        .child("reactions");

                thumbsRef.child(safeEmail).setValue(true)
                        .addOnSuccessListener(aVoid ->
                                Toast.makeText(context, "Reacted successfully!", Toast.LENGTH_SHORT).show()
                        )
                        .addOnFailureListener(e ->
                                Toast.makeText(context, "Failed to react: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
            }
        });
    }

    @Override
    public int getItemCount() {
        return internshipList.size();
    }

    public void updateList(List<Internship> newList) {
        internshipList = newList;
        notifyDataSetChanged();
    }

    public static class InternshipViewHolder extends RecyclerView.ViewHolder {
        TextView titleText, descText, deadlineText, branchText, reactMessage;
        Button applyButton;
        ImageButton thumbsUpButton;

        public InternshipViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.internshipTitle);
            descText = itemView.findViewById(R.id.internshipDescription);
            deadlineText = itemView.findViewById(R.id.textDeadline);
            branchText = itemView.findViewById(R.id.textBranch);
            reactMessage = itemView.findViewById(R.id.textReactMsg);
            applyButton = itemView.findViewById(R.id.buttonApply);
            thumbsUpButton = itemView.findViewById(R.id.thumbsUpButton);
        }
    }
}
