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


    // Add Firebase Analytics (optional, if you use analytics)
    implementation(libs.firebase.analytics)

    // Add LoadingButton library (if needed)
    implementation ("com.airbnb.android:lottie:5.2.0")
    implementation ("androidx.cardview:cardview:1.0.0")
    implementation ("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.google.android.material:material:1.12.0")

    implementation ("androidx.drawerlayout:drawerlayout:1.1.1")   // For DrawerLayout
    implementation ("androidx.gridlayout:gridlayout:1.0.0")        // For GridLayout if used


    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
