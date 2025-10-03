plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.gaurdiancircle"
    compileSdk = 35

    buildFeatures {
        buildConfig = true // ✅ Enable BuildConfig fields
        viewBinding = true
    }

    defaultConfig {
        applicationId = "com.example.gaurdiancircle"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        // ✅ Cohere API Key
        buildConfigField(
            "String",
            "COHERE_API_KEY",
            "\"ZcRlUKmGUGTnMnGJM1njlFsmCBf9fwwb0nZ857fF\""
        )

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // AndroidX + UI
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.gridlayout)
    implementation("com.android.volley:volley:1.2.1")
    implementation(libs.circleimageview)
    implementation("com.google.android.libraries.places:places:3.3.0")
    implementation("org.osmdroid:osmdroid-android:6.1.15")
    implementation("org.osmdroid:osmdroid-wms:6.1.14")



    // Lifecycle
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // Google Play Services
    implementation(libs.play.services.maps.v1820)
    implementation(libs.gms.play.services.location)
    implementation(libs.places)
    implementation(libs.play.services.maps)
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")

    // Networking
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // JSON Parsing
    implementation("com.google.code.gson:gson:2.11.0")

    // OkHttp (for Cohere API)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
