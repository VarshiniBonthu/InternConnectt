package com.example.internconnectt.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.internconnectt.R;
import com.example.internconnectt.models.Internship;

import java.util.List;
import java.util.Map;

public class FacultyInternshipAdapter extends RecyclerView.Adapter<FacultyInternshipAdapter.FacultyViewHolder> {

    private List<Internship> internshipList;

    public FacultyInternshipAdapter(List<Internship> list) {
        this.internshipList = list;
    }

    @NonNull
    @Override
    public FacultyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_faculty_internship, parent, false);
        return new FacultyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FacultyViewHolder holder, int position) {
        Internship internship = internshipList.get(position);

        holder.title.setText(internship.getTitle());
        holder.deadline.setText("Deadline: " + internship.getDeadline());

        if (internship.getAppliedStudents() != null) {
            int count = internship.getAppliedStudents().size();
            holder.appliedCount.setText("Applied Students: " + count);

            StringBuilder list = new StringBuilder();
            for (Map.Entry<String, String> entry : internship.getAppliedStudents().entrySet()) {
                list.append("- ").append(entry.getValue()).append("\n");
            }
            holder.appliedList.setText(list.toString());
        } else {
            holder.appliedCount.setText("No applications yet.");
            holder.appliedList.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return internshipList.size();
    }

    public static class FacultyViewHolder extends RecyclerView.ViewHolder {
        TextView title, deadline, appliedCount, appliedList;

        public FacultyViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.facultyInternshipTitle);
            deadline = itemView.findViewById(R.id.facultyInternshipDeadline);
            appliedCount = itemView.findViewById(R.id.appliedCountText);
            appliedList = itemView.findViewById(R.id.appliedListText);
        }
    }
}
