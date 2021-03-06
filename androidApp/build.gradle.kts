plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
}

dependencies {
    implementation(project(":shared"))

    val composeVersion = "1.0.5"
    val activityComposeVersion = "1.3.1"
    val lifecycleViewModelComposeVersion = "2.4.0-rc01"
    val accompanistVersion = "0.20.2"

    implementation("androidx.appcompat:appcompat:1.3.1")

    implementation("androidx.compose.ui:ui:$composeVersion")
    // Tooling support (Previews, etc.)
//    implementation("androidx.compose.ui:ui-tooling:$composeVersion")
    // Foundation (Border, Background, Box, Image, Scroll, shapes, animations, etc.)
    implementation("androidx.compose.foundation:foundation:$composeVersion")
    // Material Design
    implementation("androidx.compose.material:material:$composeVersion")
    // Material design icons
    implementation("androidx.compose.material:material-icons-core:$composeVersion")
    implementation("androidx.compose.material:material-icons-extended:$composeVersion")
    // Animations
    implementation("androidx.compose.animation:animation:$composeVersion")
//    implementation("androidx.compose.animation:animation-graphics:$composeVersion")
    // Integration with activities
    implementation("androidx.activity:activity-compose:$activityComposeVersion")
    // Integration with ViewModels
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleViewModelComposeVersion")
    //Integration between Compose and cameraX

    // Accompanist
    implementation("com.google.accompanist:accompanist-insets:$accompanistVersion")
    implementation("com.google.accompanist:accompanist-systemuicontroller:$accompanistVersion")

    implementation("com.github.skgmn:startactivityx:1.2.0")
    implementation("com.github.skgmn:cameraxx-composable:0.7.1")

    implementation("com.github.h0tk3y.geometry:geometry:v0.1")
}

android {
    compileSdk = 31
    defaultConfig {
        applicationId = "com.wadiyatalkinabeet.gambit.android"
        minSdk = 21
        targetSdk = 31
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.1.0-beta02"
    }

    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_1_8)
        targetCompatibility(JavaVersion.VERSION_1_8)
    }

    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
    }
}