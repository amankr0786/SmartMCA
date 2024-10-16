plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.study.smartmca"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.study.smartmca"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)

    // Add Google Sign-In dependency
    implementation(libs.google.android.gms.play.services.auth)

    // Firebase BoM (Bill of Materials)
    implementation(platform(libs.firebase.bom))

    // Add Firebase Realtime Database
    implementation(libs.firebase.database)

    // Firebase Authentication (update to latest version)
    implementation("com.google.firebase:firebase-auth:21.0.1")
/*    implementation("com.google.firebase:firebase-auth-ktx:21.0.1")*/

    // Add Firebase Analytics (optional, if you use analytics)
    implementation(libs.firebase.analytics)

    // Lottie for animations (LoadingButton)
    implementation("com.airbnb.android:lottie:5.2.0")

    // Add CardView for UI elements
    implementation("androidx.cardview:cardview:1.0.0")

    // Add CircleImageView
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // Material Design components
    implementation("com.google.android.material:material:1.12.0")

    // DrawerLayout (if used)
    implementation("androidx.drawerlayout:drawerlayout:1.1.1")

    // GridLayout (if used)
    implementation("androidx.gridlayout:gridlayout:1.0.0")

    // Glide for image loading
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Firebase Storage
    implementation("com.google.firebase:firebase-storage:20.1.1")
    implementation(libs.recyclerview)

    // JUnit and Android Test dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
