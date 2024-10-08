package com.study.smartmca;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DashboardActivity extends AppCompatActivity {

    private TextView welcomeTextView;
    private Button logoutButton;
    private ImageButton settingsButton; // Add ImageButton reference for settings
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        welcomeTextView = findViewById(R.id.welcomeTextView);
        logoutButton = findViewById(R.id.logoutButton);
        settingsButton = findViewById(R.id.settingsButton); // Initialize the settings button
        mAuth = FirebaseAuth.getInstance();

        // Display welcome message
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            welcomeTextView.setText("Welcome, " + user.getEmail() + "!");
        } else {
            welcomeTextView.setText("Welcome!");
        }

        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // Handle settings button click
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }
}
