package com.study.smartmca;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText emailEditText;
    private Button resetPasswordButton;
    private TextView backToLoginTextView, timerTextView;
    private FirebaseAuth mAuth;
    private Handler handler = new Handler();
    private Runnable timerRunnable;
    private int timerSeconds = 30; // Timer duration in seconds
    private boolean isResendAllowed = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Find views by ID
        emailEditText = findViewById(R.id.emailEditText);
        resetPasswordButton = findViewById(R.id.resetPasswordButton);
        backToLoginTextView = findViewById(R.id.backToLoginTextView);
        timerTextView = findViewById(R.id.timerTextView);

        // Set up the reset password button click listener
        resetPasswordButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            if (email.isEmpty()) {
                emailEditText.setError("Email is required.");
                return;
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailEditText.setError("Invalid email format.");
                return;
            }
            if (isResendAllowed) {
                resetPassword(email);
            } else {
                Toast.makeText(ForgotPasswordActivity.this, "Please wait before requesting again.", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up the back to login text view click listener
        backToLoginTextView.setOnClickListener(v -> {
            Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Finish the current activity to prevent going back
        });

        // Initialize the timer text view
        timerTextView.setText("You can resend now.");
    }

    private void resetPassword(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ForgotPasswordActivity.this, "Password reset email sent.", Toast.LENGTH_SHORT).show();
                        startResendTimer();
                    } else {
                        Toast.makeText(ForgotPasswordActivity.this, "Failed to send password reset email.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void startResendTimer() {
        isResendAllowed = false;
        timerSeconds = 30; // Reset the timer duration
        updateTimerText();

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (timerSeconds > 0) {
                    timerSeconds--;
                    updateTimerText();
                    handler.postDelayed(this, 1000); // Update every second
                } else {
                    isResendAllowed = true;
                    timerTextView.setText("You can resend now.");
                }
            }
        };
        handler.post(timerRunnable);
    }

    private void updateTimerText() {
        String timerText = "Wait to Resend link in " + timerSeconds + "s";
        timerTextView.setText(timerText);
    }
}
