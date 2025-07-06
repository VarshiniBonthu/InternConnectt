package com.example.internconnectt.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.internconnectt.R;

import java.util.List;

public class AppliedInternshipTitleAdapter extends RecyclerView.Adapter<AppliedInternshipTitleAdapter.TitleViewHolder> {

    private final Context context;
    private List<String> titles;

    public AppliedInternshipTitleAdapter(Context context, List<String> titles) {
        this.context = context;
        this.titles = titles;
    }

    public void updateList(List<String> newList) {
        this.titles = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TitleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_applied_title, parent, false);
        return new TitleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TitleViewHolder holder, int position) {
        holder.titleText.setText(titles.get(position));
    }

    @Override
    public int getItemCount() {
        return titles.size();
    }

    static class TitleViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;

        public TitleViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.appliedTitleText);
        }
    }
}
