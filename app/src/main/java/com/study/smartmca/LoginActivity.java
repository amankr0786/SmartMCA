package com.study.smartmca;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Random;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 9001;

    private EditText emailEditText, passwordEditText, captchaEditText;
    private Button loginButton, refreshCaptchaButton;
    private TextView registerTextView, forgotPasswordTextView, captchaTextView;
    private ImageView showPasswordImageView;
    private ImageButton googleSignInButton;
    private CheckBox rememberMeCheckBox;
    private LottieAnimationView loadingAnimation;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    private boolean isPasswordVisible = false;
    private String currentCaptcha;

    private static final String PREFS_NAME = "SmartMCA_Prefs";
    private static final String PREF_LOGGED_IN = "isLoggedIn";
    private static final String PREF_REMEMBER_ME = "rememberMe";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize UI elements
        initializeViews();
        setupListeners();
        refreshCaptcha();

        // Load "Remember Me" state
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean rememberMe = preferences.getBoolean(PREF_REMEMBER_ME, false);
        rememberMeCheckBox.setChecked(rememberMe);

        // Real-time Email Validation
        setupEmailValidation();
    }

    private void initializeViews() {
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        captchaEditText = findViewById(R.id.captchaEditText);
        loginButton = findViewById(R.id.loginButton);
        refreshCaptchaButton = findViewById(R.id.refreshCaptchaButton);
        registerTextView = findViewById(R.id.registerTextView);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordButton);
        showPasswordImageView = findViewById(R.id.showPasswordImageView);
        rememberMeCheckBox = findViewById(R.id.rememberMeCheckBox);
        captchaTextView = findViewById(R.id.captchaTextView);
        googleSignInButton = findViewById(R.id.googleSignInButton);
        loadingAnimation = findViewById(R.id.loadingAnimation);
    }

    private void setupListeners() {
        showPasswordImageView.setOnClickListener(v -> togglePasswordVisibility());
        loginButton.setOnClickListener(v -> attemptLogin());
        registerTextView.setOnClickListener(v -> navigateToRegister());
        forgotPasswordTextView.setOnClickListener(v -> navigateToForgotPassword());
        refreshCaptchaButton.setOnClickListener(v -> refreshCaptcha());
        googleSignInButton.setOnClickListener(v -> signIn());
    }

    private void setupEmailValidation() {
        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                String email = charSequence.toString().trim();
                if (!isValidEmail(email)) {
                    emailEditText.setError("Invalid email format.");
                } else {
                    emailEditText.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            passwordEditText.setInputType(129); // InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
            showPasswordImageView.setImageResource(R.drawable.ic_eye); // Eye icon
        } else {
            passwordEditText.setInputType(1); // InputType.TYPE_CLASS_TEXT
            showPasswordImageView.setImageResource(R.drawable.ic_eye_off); // Eye-off icon
        }
        passwordEditText.setSelection(passwordEditText.getText().length()); // Move cursor to end
        isPasswordVisible = !isPasswordVisible;
    }

    private void attemptLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String enteredCaptcha = captchaEditText.getText().toString().trim();

        if (!isValidEmail(email)) {
            Toast.makeText(LoginActivity.this, "Invalid email format.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Password cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!enteredCaptcha.equals(currentCaptcha)) {
            Toast.makeText(LoginActivity.this, "Incorrect captcha.", Toast.LENGTH_SHORT).show();
            refreshCaptcha();
            return;
        }

        setLoading(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        setLoading(false);
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            Toast.makeText(LoginActivity.this, "Wrong Email or Password.Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private void navigateToRegister() {
        Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
        startActivity(intent);
    }

    private void navigateToForgotPassword() {
        Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
        startActivity(intent);
    }

    private void refreshCaptcha() {
        currentCaptcha = generateCaptcha();
        captchaTextView.setText(currentCaptcha);
        captchaEditText.setText("");
    }

    private String generateCaptcha() {
        Random random = new Random();
        int length = 6;
        StringBuilder captcha = new StringBuilder();
        for (int i = 0; i < length; i++) {
            captcha.append(random.nextInt(10));
        }
        return captcha.toString();
    }

    private boolean isValidEmail(CharSequence email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void signIn() {
        setLoading(true);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                setLoading(false);
                Log.w(TAG, "Google sign in failed", e);
                Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        setLoading(false);
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(PREF_LOGGED_IN, true);
            editor.putBoolean(PREF_REMEMBER_ME, rememberMeCheckBox.isChecked());
            editor.apply();
            Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            loadingAnimation.setVisibility(View.VISIBLE);
            loginButton.setEnabled(false);
            googleSignInButton.setEnabled(false);
        } else {
            loadingAnimation.setVisibility(View.GONE);
            loginButton.setEnabled(true);
            googleSignInButton.setEnabled(true);
        }
    }
}