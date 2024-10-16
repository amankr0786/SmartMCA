package com.study.smartmca;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue; // Import for setting text size
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.SearchView;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference userRef;
    private SearchView searchView;
    private Spinner universitySpinner;

    private TextView userNameTextView, userEmailTextView;
    private ImageView profileImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        searchView = findViewById(R.id.searchView);
        searchView.setIconifiedByDefault(false);
        searchView.setQueryHint("Search for Study Materials PDFs, Videos, Docs, etc.");

        // Programmatically adjust the query hint text size
        TextView searchHint = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        if (searchHint != null) {
            searchHint.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15); // Set desired text size
        }

        universitySpinner = findViewById(R.id.universitySpinner);

        // Setup Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Setup ActionBarDrawerToggle
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Dynamically update the navigation drawer header with user data
        View headerView = navigationView.getHeaderView(0);
        userNameTextView = headerView.findViewById(R.id.user_name);
        userEmailTextView = headerView.findViewById(R.id.user_email);
        profileImageView = headerView.findViewById(R.id.profile_image);

        // Get user data from Firebase Auth and Firebase Realtime Database
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();
            userEmailTextView.setText(userEmail != null ? userEmail : "No Email");

            // Fetch user name and profile image
            fetchUserDetailsFromDatabase(currentUser.getUid());

            // Load user profile image if available
            if (currentUser.getPhotoUrl() != null) {
                String userPhotoUrl = currentUser.getPhotoUrl().toString();
                Glide.with(this).load(userPhotoUrl).into(profileImageView);
            } else {
                profileImageView.setImageResource(R.drawable.ic_user_profile); // Default image
            }
        }

        // Populate the university spinner with a custom layout
        setupUniversitySpinner();

        // Handle navigation drawer item clicks
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            int id = menuItem.getItemId();
            if (id == R.id.menu_profile) {
                startActivity(new Intent(DashboardActivity.this, ProfileActivity.class));
            } else if (id == R.id.menu_settings) {
                startActivity(new Intent(DashboardActivity.this, SettingsActivity.class));
            } else if (id == R.id.menu_logout) {
                logoutUser();
            }
            drawerLayout.closeDrawers();
            return true;
        });

        // Handle search functionality
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // You can provide live filtering here if needed
                return false;
            }
        });
    }

    private void setupUniversitySpinner() {
        // Fetch the array of universities from strings.xml
        String[] universities = getResources().getStringArray(R.array.universities_array);

        // Create an ArrayAdapter using the custom spinner item layout and dropdown layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, universities);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        universitySpinner.setAdapter(adapter);

        // Handle university selection
        universitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedUniversity = parent.getItemAtPosition(position).toString();
                if (!selectedUniversity.equals(getString(R.string.choose_university))) {
                    // Redirect to UniversityActivity with selected university
                    Intent intent = new Intent(DashboardActivity.this, UniversityActivity.class);
                    intent.putExtra("universityName", selectedUniversity);
                    startActivity(intent);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void performSearch(String query) {
        Toast.makeText(DashboardActivity.this, "Searching for: " + query, Toast.LENGTH_SHORT).show();
        // Implement actual search logic here
    }

    private void fetchUserDetailsFromDatabase(String userId) {
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String userName = snapshot.child("name").getValue(String.class);
                    if (userName != null) {
                        userNameTextView.setText(userName);
                    } else {
                        userNameTextView.setText("Guest");
                    }
                } else {
                    Log.d("DashboardActivity", "User data not found in database.");
                    userNameTextView.setText("Guest");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("DashboardActivity", "Failed to read user data.", error.toException());
                Toast.makeText(DashboardActivity.this, "Failed to load user data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logoutUser() {
        mAuth.signOut();

        // Clear the Remember Me preferences
        SharedPreferences preferences = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(LoginActivity.PREF_REMEMBER_ME);
        editor.remove("email");
        editor.remove("password");
        editor.apply();

        Toast.makeText(DashboardActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // Close current activity
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}