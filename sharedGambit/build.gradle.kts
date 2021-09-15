import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
}

version = "1.0"

kotlin {
    android()

    val iosTarget: (String, KotlinNativeTarget.() -> Unit) -> KotlinNativeTarget = when {
        System.getenv("SDK_NAME")?.startsWith("iphoneos") == true -> ::iosArm64
        System.getenv("NATIVE_ARCH")?.startsWith("arm") == true -> ::iosSimulatorArm64
        else -> ::iosX64
    }

    iosTarget("ios") {}

    cocoapods {
        summary = "Gambit shared library"
        homepage = "https://kt.wadiyatalkinabeet.com/gambit"
        ios.deploymentTarget = "14.1"
        framework {
            baseName = "sharedGambit"
        }
        podfile = project.file("../Gambit/Podfile")
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("com.github.h0tk3y.geometry:geometry:v0.1")
                implementation("com.github.h0tk3y.geometry:algorithms:v0.1")
                implementation("com.google.android.play:core-ktx:1.8.1")

                implementation("org.tensorflow:tensorflow-lite-support:0.2.0")
                implementation("org.tensorflow:tensorflow-lite-metadata:0.1.0")
                implementation("org.tensorflow:tensorflow-lite-gpu:2.3.0")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))

                implementation("androidx.test:core-ktx:1.4.0")
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(project(":libraries:opencv-android"))
            }
        }

        val androidTest by getting {
            dependencies {
                implementation(kotlin("test"))

                implementation("androidx.test:runner:1.4.0")
                implementation("androidx.test:rules:1.4.0")
                implementation("org.junit.jupiter:junit-jupiter-engine:5.8.0")

            }

            tasks.withType<Test> {
                systemProperty("java.library.path", "src/commonTest/jniLibs/")
                useJUnitPlatform()
                }
        }
        val iosMain by getting
        val iosTest by getting
    }
}

android {
    compileSdk = 31
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

    defaultConfig {
        minSdk = 21
        targetSdk = 31
        externalNativeBuild {
            cmake {
                cppFlags += "-std=c++11 -frtti -fexceptions"
                arguments += "-DOpenCV_DIR=../libraries/opencv-android/sdk/native/jni"
                abiFilters.addAll(listOf("x86", "x86_64", "armeabi-v7a", "arm64-v8a"))
            }
        }
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    }
    testOptions {
        //Tmp as log isnt mocked, mock log!
        unitTests.isReturnDefaultValues = true
        unitTests.isIncludeAndroidResources = true
    }
    externalNativeBuild {
        cmake {
            path = file("src/androidMain/cpp/CMakeLists.txt")
            version = "3.10.2"
        }
    }
    buildFeatures {
        mlModelBinding = true
    }

    packagingOptions {
        resources.excludes.add("META-INF/*")
    }
}