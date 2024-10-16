package com.study.smartmca;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private EditText nameEditText, emailEditText, mobileEditText, collegeEditText;
    private Spinner stateSpinner;
    private Button saveNameButton, saveEmailButton, saveMobileButton, saveCollegeButton, saveStateButton, updateProfileButton;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private LottieAnimationView loadingAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase Auth and Database Reference
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Initialize UI elements
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        mobileEditText = findViewById(R.id.mobileEditText);
        stateSpinner = findViewById(R.id.stateSpinner);
        collegeEditText = findViewById(R.id.collegeEditText);
        saveNameButton = findViewById(R.id.saveNameButton);
        saveEmailButton = findViewById(R.id.saveEmailButton);
        saveMobileButton = findViewById(R.id.saveMobileButton);
        saveCollegeButton = findViewById(R.id.saveCollegeButton);
        saveStateButton = findViewById(R.id.saveStateButton);
        updateProfileButton = findViewById(R.id.updateProfileButton);
        loadingAnimation = findViewById(R.id.loadingAnimation);

        // Hide save buttons initially
        hideSaveButtons();

        // Populate state spinner with Indian states
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.indian_states, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stateSpinner.setAdapter(adapter);

        // Set up text watchers for showing save buttons when changes occur
        setupTextWatchers();

        // Load current user data
        loadUserData();

        // Set up button click listeners
        saveNameButton.setOnClickListener(this::saveName);
        saveEmailButton.setOnClickListener(this::saveEmail);
        saveMobileButton.setOnClickListener(this::saveMobile);
        saveCollegeButton.setOnClickListener(this::saveCollege);
        saveStateButton.setOnClickListener(this::saveState);
        updateProfileButton.setOnClickListener(this::updateProfile);
    }

    private void setupTextWatchers() {
        nameEditText.addTextChangedListener(createTextWatcher(saveNameButton));
        emailEditText.addTextChangedListener(createTextWatcher(saveEmailButton));
        mobileEditText.addTextChangedListener(createTextWatcher(saveMobileButton));
        collegeEditText.addTextChangedListener(createTextWatcher(saveCollegeButton));
    }

    private TextWatcher createTextWatcher(Button saveButton) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                saveButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };
    }

    private void hideSaveButtons() {
        saveNameButton.setVisibility(View.GONE);
        saveEmailButton.setVisibility(View.GONE);
        saveMobileButton.setVisibility(View.GONE);
        saveCollegeButton.setVisibility(View.GONE);
        saveStateButton.setVisibility(View.GONE);
    }

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            nameEditText.setText(user.getDisplayName());
            emailEditText.setText(user.getEmail());

            databaseReference.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String mobile = snapshot.child("mobile").getValue(String.class);
                        String college = snapshot.child("college").getValue(String.class);
                        String state = snapshot.child("state").getValue(String.class);

                        mobileEditText.setText(mobile);
                        collegeEditText.setText(college);
                        if (state != null) {
                            ArrayAdapter adapter = (ArrayAdapter) stateSpinner.getAdapter();
                            int position = adapter.getPosition(state);
                            stateSpinner.setSelection(position);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ProfileActivity.this, "Failed to load data.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void saveName(View view) {
        String name = nameEditText.getText().toString().trim();
        if (name.isEmpty()) {
            nameEditText.setError("Name is required.");
            return;
        }

        setLoading(true);
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(name).build();
            user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
                setLoading(false);
                if (task.isSuccessful()) {
                    databaseReference.child(user.getUid()).child("name").setValue(name);
                    Toast.makeText(ProfileActivity.this, "Name updated", Toast.LENGTH_SHORT).show();
                    saveNameButton.setVisibility(View.GONE);
                }
            });
        }
    }

    public void saveEmail(View view) {
        String email = emailEditText.getText().toString().trim();
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Invalid email format.");
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), "user_password"); // Replace "user_password" with the actual password
            user.reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    setLoading(true);
                    user.updateEmail(email).addOnCompleteListener(task1 -> {
                        setLoading(false);
                        if (task1.isSuccessful()) {
                            databaseReference.child(user.getUid()).child("email").setValue(email);
                            Toast.makeText(ProfileActivity.this, "Email updated", Toast.LENGTH_SHORT).show();
                            saveEmailButton.setVisibility(View.GONE);
                        } else {
                            Toast.makeText(ProfileActivity.this, "Failed to update email", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(ProfileActivity.this, "Re-authentication failed.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void saveMobile(View view) {
        String mobile = mobileEditText.getText().toString().trim();
        if (mobile.isEmpty() || mobile.length() != 10) {
            mobileEditText.setError("Invalid mobile number.");
            return;
        }

        setLoading(true);
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            databaseReference.child(user.getUid()).child("mobile").setValue(mobile).addOnCompleteListener(task -> {
                setLoading(false);
                if (task.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, "Mobile number updated", Toast.LENGTH_SHORT).show();
                    saveMobileButton.setVisibility(View.GONE);
                }
            });
        }
    }

    public void saveCollege(View view) {
        String college = collegeEditText.getText().toString().trim();
        if (college.isEmpty()) {
            collegeEditText.setError("College name is required.");
            return;
        }

        setLoading(true);
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            databaseReference.child(user.getUid()).child("college").setValue(college).addOnCompleteListener(task -> {
                setLoading(false);
                if (task.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, "College updated", Toast.LENGTH_SHORT).show();
                    saveCollegeButton.setVisibility(View.GONE);
                }
            });
        }
    }

    public void saveState(View view) {
        String state = stateSpinner.getSelectedItem().toString();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            databaseReference.child(user.getUid()).child("state").setValue(state).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, "State updated", Toast.LENGTH_SHORT).show();
                    saveStateButton.setVisibility(View.GONE);
                }
            });
        }
    }

    public void updateProfile(View view) {
        saveName(null);
        saveEmail(null);
        saveMobile(null);
        saveCollege(null);
        saveState(null);

        // Wait for all updates to complete, then navigate to dashboard
        // In a real application, you'd want to handle this with a callback or status check
        Toast.makeText(ProfileActivity.this, "Profile updating...", Toast.LENGTH_SHORT).show();
        navigateToDashboard();
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(ProfileActivity.this, DashboardActivity.class);
        startActivity(intent);
        finish(); // Optionally finish this activity
    }

    private void setLoading(boolean isLoading) {
        loadingAnimation.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }
}
