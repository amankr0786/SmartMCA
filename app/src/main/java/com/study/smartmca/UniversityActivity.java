package com.study.smartmca;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UniversityActivity extends AppCompatActivity implements SubjectsAdapter.OnSubjectClickListener {

    private RecyclerView recyclerViewSubjects;
    private CardView cardSubjects;
    private SubjectsAdapter subjectsAdapter;
    private List<Subject> subjectsList;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_university);

        // Find the TextView by its ID
        TextView universityTitle = findViewById(R.id.textViewUniversityTitle);

        // Get university name from intent
        String universityNameFromIntent = getIntent().getStringExtra("universityName");

        // Set the university name to the title TextView
        if (universityNameFromIntent != null && !universityNameFromIntent.isEmpty()) {
            universityTitle.setText(universityNameFromIntent);
        } else {
            universityTitle.setText("No University Selected");
        }

        // Initialize RecyclerView
        recyclerViewSubjects = findViewById(R.id.recyclerViewSubjects);
        ProgressBar progressBar = findViewById(R.id.progressBar);

        // Set up GridLayoutManager for 2 columns
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerViewSubjects.setLayoutManager(gridLayoutManager);

        // Initialize subjects list and adapter
        subjectsList = new ArrayList<>();
        subjectsAdapter = new SubjectsAdapter(subjectsList, this, this);  // Pass both 'this' (for listener) and 'this' (for context)
        recyclerViewSubjects.setAdapter(subjectsAdapter);


        // Check if university name is valid
        if (universityNameFromIntent == null || universityNameFromIntent.isEmpty()) {
            Toast.makeText(this, "No university selected!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("universities");

        // Show ProgressBar before fetching data
        progressBar.setVisibility(View.VISIBLE);

        fetchSubjectsForUniversity(universityNameFromIntent, progressBar);
    }

    // Method to fetch subjects from Firebase
    private void fetchSubjectsForUniversity(String universityName, ProgressBar progressBar) {
        if (universityName != null && !universityName.isEmpty()) {
            databaseReference.child(universityName).child("subjects").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d("FirebaseData", "Subjects fetched: " + dataSnapshot.getValue());
                    subjectsList.clear();
                    for (DataSnapshot subjectSnapshot : dataSnapshot.getChildren()) {
                        Subject subject = subjectSnapshot.getValue(Subject.class);
                        if (subject != null) {
                            subjectsList.add(subject);
                        }
                    }
                    subjectsAdapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("FirebaseError", databaseError.getMessage());
                    Toast.makeText(UniversityActivity.this, "Error fetching subjects", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
    }

    // Handle subject clicks from the RecyclerView
    @Override
    public void onSubjectClick(String subjectName) {
        Toast.makeText(this, "Clicked on subject: " + subjectName, Toast.LENGTH_SHORT).show();
    }
}
