package com.study.smartmca;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SubjectDetailActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_detail);

        // Get data from intent
        String subjectCode = getIntent().getStringExtra("subjectCode");
        String universityName = getIntent().getStringExtra("universityName");

        // Initialize TextViews
        TextView subjectCodeTextView = findViewById(R.id.subjectCodeTextView);
        TextView subjectNameTextView = findViewById(R.id.subjectNameTextView);
        TextView subjectDescriptionTextView = findViewById(R.id.subjectDescriptionTextView);
        TextView subjectCreditsTextView = findViewById(R.id.subjectCreditsTextView);

        // Set subject code to TextView
        if (subjectCode != null) {
            subjectCodeTextView.setText(subjectCode);
        } else {
            subjectCodeTextView.setText("N/A");
        }

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("universities").child(universityName).child("subjects").child(subjectCode);

        // Fetch subject details from Firebase
        fetchSubjectDetails(subjectNameTextView, subjectDescriptionTextView, subjectCreditsTextView);
    }

    private void fetchSubjectDetails(TextView subjectNameTextView,
                                     TextView subjectDescriptionTextView,
                                     TextView subjectCreditsTextView) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Retrieve subject details
                    String subjectName = dataSnapshot.child("name").getValue(String.class);
                    String subjectDescription = dataSnapshot.child("description").getValue(String.class);
                    Integer subjectCredits = dataSnapshot.child("credits").getValue(Integer.class);

                    // Set data to TextViews, default to "N/A" if null
                    subjectNameTextView.setText(subjectName != null ? subjectName : "N/A");
                    subjectDescriptionTextView.setText(subjectDescription != null ? subjectDescription : "N/A");
                    subjectCreditsTextView.setText(subjectCredits != null ? String.valueOf(subjectCredits) : "N/A");
                } else {
                    Toast.makeText(SubjectDetailActivity.this, "Subject not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("SubjectDetailActivity", "Error fetching subject details: " + databaseError.getMessage());
                Toast.makeText(SubjectDetailActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
