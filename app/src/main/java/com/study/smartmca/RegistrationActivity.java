package com.study.smartmca;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegistrationActivity extends AppCompatActivity {

    private EditText nameEditText, emailEditText, passwordEditText, mobileEditText, collegeEditText;
    private Spinner stateSpinner;
    private Button registerButton;
    private TextView backToLoginTextView;
    private CheckBox termsCheckBox;
    private ImageView showPasswordImageView;
    private boolean isPasswordVisible = false;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference; // For Realtime Database
    private LottieAnimationView loadingAnimation; // New loading animation view

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize Firebase Database Reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Initialize UI elements
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        mobileEditText = findViewById(R.id.mobileEditText);
        stateSpinner = findViewById(R.id.stateSpinner);
        collegeEditText = findViewById(R.id.collegeEditText);
        registerButton = findViewById(R.id.registerButton);
        backToLoginTextView = findViewById(R.id.backToLoginTextView);
        termsCheckBox = findViewById(R.id.termsCheckBox);
        showPasswordImageView = findViewById(R.id.showPasswordImageView);
        loadingAnimation = findViewById(R.id.loadingAnimation); // Initialize loading animation view

        // Populate state spinner with Indian states
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.indian_states, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stateSpinner.setAdapter(adapter);

        // Set up text watchers and password visibility toggle
        setupTextWatchers();

        // Set register button listener
        registerButton.setOnClickListener(v -> {
            registerUser();
        });

        // Handle Back to Login TextView click
        backToLoginTextView.setOnClickListener(v -> {
            startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
            finish();
        });

        // Set up Terms and Conditions checkbox listener
        termsCheckBox.setOnClickListener(v -> {
            showTermsAndConditionsDialog();
        });
    }

    private void setupTextWatchers() {
        // Real-time validation for email
        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateEmail();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Real-time validation for password
        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePassword();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Real-time validation for mobile number
        mobileEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateMobileNumber();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Toggle password visibility
        showPasswordImageView.setOnClickListener(v -> {
            if (isPasswordVisible) {
                passwordEditText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
                showPasswordImageView.setImageResource(R.drawable.ic_eye_off);
            } else {
                passwordEditText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                showPasswordImageView.setImageResource(R.drawable.ic_eye);
            }
            passwordEditText.setSelection(passwordEditText.getText().length());
            isPasswordVisible = !isPasswordVisible;
        });
    }

    private void registerUser() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String mobile = mobileEditText.getText().toString().trim();
        String state = stateSpinner.getSelectedItem().toString();
        String college = collegeEditText.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || mobile.isEmpty()) {
            Toast.makeText(RegistrationActivity.this, "Please fill all mandatory fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!validateEmail() || !validatePassword() || !validateMobileNumber() || !termsCheckBox.isChecked()) {
            if (!termsCheckBox.isChecked()) {
                Toast.makeText(RegistrationActivity.this, "You must agree to the Terms and Conditions.", Toast.LENGTH_SHORT).show();
                showTermsAndConditionsDialog();
            }
            return;
        }

        // Show loading animation
        setLoading(true);

        // Create Firebase Auth user
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    setLoading(false); // Hide loading animation
                    if (task.isSuccessful()) {
                        // User registered successfully
                        FirebaseUser user = mAuth.getCurrentUser();
                        saveUserToDatabase(user.getUid(), name, email, mobile, state, college);
                        Toast.makeText(RegistrationActivity.this, "Registration successful.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish(); // Finish the current activity
                    } else {
                        // Registration failed
                        Toast.makeText(RegistrationActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserToDatabase(String userId, String name, String email, String mobile, String state, String college) {
        // Create user object
        User user = new User(name, email, mobile, state, college);

        // Save user data in Firebase Realtime Database under "Users" node
        databaseReference.child(userId).setValue(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Successfully stored user data
                        Toast.makeText(RegistrationActivity.this, "User data saved.", Toast.LENGTH_SHORT).show();
                    } else {
                        // Failed to store user data
                        Toast.makeText(RegistrationActivity.this, "Failed to save user data.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean validateEmail() {
        String email = emailEditText.getText().toString().trim();
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Invalid email format.");
            return false;
        }
        return true;
    }

    private boolean validatePassword() {
        String password = passwordEditText.getText().toString().trim();
        if (password.length() < 8) {
            passwordEditText.setError("Password must be at least 8 characters.");
            return false;
        }
        return true;
    }

    private boolean validateMobileNumber() {
        String mobile = mobileEditText.getText().toString().trim();
        if (mobile.isEmpty() || mobile.length() != 10 || !mobile.matches("\\d+")) {
            mobileEditText.setError("Please enter a valid 10-digit mobile number.");
            return false;
        }
        return true;
    }

    // New method to show/hide loading animation
    private void setLoading(boolean isLoading) {
        if (isLoading) {
            loadingAnimation.setVisibility(View.VISIBLE);
            registerButton.setEnabled(false);
        } else {
            loadingAnimation.setVisibility(View.GONE);
            registerButton.setEnabled(true);
        }
    }

    // New method to show Terms and Conditions dialog
    private void showTermsAndConditionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Terms and Conditions");
        builder.setMessage(getString(R.string.terms_and_conditions));
        builder.setPositiveButton("Accept", (dialog, which) -> {
            termsCheckBox.setChecked(true);
        });
        builder.setNegativeButton("Decline", (dialog, which) -> {
            termsCheckBox.setChecked(false);
        });
        builder.show();
    }
}