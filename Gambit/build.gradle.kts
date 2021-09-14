plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
}

dependencies {
    implementation(project(":sharedGambit"))

    val kotlinVersion = "1.5.30"
    val coreVersion = "1.6.0"
    val cameraxVersion = "1.0.1"
    val tensorflowVersion = "0.2.0"

    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("androidx.core:core-ktx:$coreVersion")

    implementation("com.google.android.material:material:1.4.0")
    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.0")

    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:1.0.0-alpha28")

    //TODO: Try move opencv code and tensorflow code out of this area and into the shared modules.
    implementation("org.tensorflow:tensorflow-lite-task-vision:$tensorflowVersion")
    implementation("org.tensorflow:tensorflow-lite-support:$tensorflowVersion")

    implementation(project(":libraries:opencv-android"))

}

android {
    compileSdk = 31
    defaultConfig {
        applicationId = "com.wadiyatalkinabeet.gambit.android"
        minSdk = 21
        targetSdk = 31
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"

        externalNativeBuild {
            cmake {
                cppFlags += "-std=c++11 -frtti -fexceptions"
                arguments += "-DOpenCV_DIR=../libraries/opencv-android/sdk/native/jni"
            }
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_1_8)
        targetCompatibility(JavaVersion.VERSION_1_8)
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        dataBinding = true
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.10.2"
        }
    }
}