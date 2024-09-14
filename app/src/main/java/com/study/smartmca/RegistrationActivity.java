package com.study.smartmca;

import android.app.AlertDialog;
import android.content.DialogInterface;
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

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class RegistrationActivity extends AppCompatActivity {

    private EditText nameEditText, emailEditText, passwordEditText, mobileEditText, collegeEditText;
    private Spinner stateSpinner;
    private Button registerButton;
    private TextView backToLoginTextView;
    private CheckBox termsCheckBox;
    private ImageView showPasswordImageView;
    private boolean isPasswordVisible = false;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mAuth = FirebaseAuth.getInstance();

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

        // Populate state spinner with Indian states
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.indian_states, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stateSpinner.setAdapter(adapter);

        // Real-time validation for email
        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateEmail();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        // Real-time validation for password
        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePassword();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        // Real-time validation for mobile number
        mobileEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateMobileNumber();
            }

            @Override
            public void afterTextChanged(Editable s) { }
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

        registerButton.setOnClickListener(v -> {
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

            // Perform final validation before registration
            if (!validateEmail() || !validatePassword() || !validateMobileNumber() || !termsCheckBox.isChecked()) {
                if (!termsCheckBox.isChecked()) {
                    Toast.makeText(RegistrationActivity.this, "You must agree to the Terms and Conditions.", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(RegistrationActivity.this, "Registration successful.", Toast.LENGTH_SHORT).show();
                            // Redirect to LoginActivity
                            Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish(); // Finish the current activity to prevent going back
                        } else {
                            Toast.makeText(RegistrationActivity.this, "Registration failed.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Handle Back to Login TextView click
        backToLoginTextView.setOnClickListener(v -> {
            Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Finish the current activity to prevent going back
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

    public void showTermsPopup(View view) {
        new AlertDialog.Builder(this)
                .setTitle("Terms and Conditions")
                .setMessage(getString(R.string.terms_and_conditions))
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }
}
