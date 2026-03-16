import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kapt)
    alias(libs.plugins.kotlin.compose)
}

val envProperties = Properties().apply {
    val envFile = rootProject.file(".env")
    if (envFile.exists()) {
        load(FileInputStream(envFile))
    }
}

android {
    namespace = "com.sirius.firegov"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.sirius.firegov"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        manifestPlaceholders["GOOGLE_MAPS_API_KEY"] = envProperties.getProperty("GOOGLE_MAPS_API_KEY") ?: ""
        buildConfigField("String", "NEWSDATA_API_KEY", "\"${envProperties.getProperty("NEWSDATA_API_KEY") ?: ""}\"")
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"${envProperties.getProperty("GOOGLE_WEB_CLIENT_ID") ?: ""}\"")
    }

    signingConfigs {
        create("release") {
            storeFile = file("release.keystore")
            storePassword = "firegov123"
            keyAlias = "firegov_release"
            keyPassword = "firegov123"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
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
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    
    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation(libs.play.services.auth)
    
    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.work)
    kapt(libs.androidx.hilt.compiler)
    
    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    
    // Image Loading
    implementation(libs.coil.compose)
    
    // Navigation
    implementation(libs.navigation.compose)
    
    // WorkManager
    implementation(libs.work.runtime.ktx)
    
    // Browser
    implementation(libs.browser)
    
    // Maps
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(libs.play.services.places)
    implementation(libs.maps.compose)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
