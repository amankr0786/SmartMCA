package com.study.smartmca;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.study.smartmca.R;

import java.util.List;

public class SubjectsAdapter extends RecyclerView.Adapter<SubjectsAdapter.SubjectViewHolder> {

    private List<Subject> subjects;
    private OnSubjectClickListener listener;
    private Context context;

    // Constructor
    public SubjectsAdapter(List<Subject> subjects, OnSubjectClickListener listener, Context context) {
        this.subjects = subjects;
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subject_card, parent, false);
        return new SubjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        Subject subject = subjects.get(position);
        holder.subjectNameTextView.setText(subject.getSubjectName());
        holder.subjectCodeTextView.setText("Course Code: " + subject.getSubjectCode());

        // Load the subject icon using Glide, with a default image if URL is null or empty
        if (subject.getIconUrl() != null && !subject.getIconUrl().isEmpty()) {
            Glide.with(context)
                    .load(subject.getIconUrl()) // Load image from URL
                    .placeholder(R.drawable.ic_subjects) // Default placeholder image
                    .into(holder.subjectIconImageView); // Correctly bind ImageView
        } else {
            // Set default image if URL is not present
            holder.subjectIconImageView.setImageResource(R.drawable.ic_subjects);
        }

        holder.itemView.setOnClickListener(v -> listener.onSubjectClick(subject.getSubjectName()));
    }

    @Override
    public int getItemCount() {
        return subjects.size();
    }

    static class SubjectViewHolder extends RecyclerView.ViewHolder {
        TextView subjectNameTextView;
        TextView subjectCodeTextView;
        ImageView subjectIconImageView; // ImageView reference

        public SubjectViewHolder(@NonNull View itemView) {
            super(itemView);
            subjectNameTextView = itemView.findViewById(R.id.subjectNameTextView);
            subjectCodeTextView = itemView.findViewById(R.id.subjectCodeTextView);
            subjectIconImageView = itemView.findViewById(R.id.subjectIconImageView); // Correct ID for ImageView
        }
    }

    public interface OnSubjectClickListener {
        void onSubjectClick(String subjectName);
    }
}
