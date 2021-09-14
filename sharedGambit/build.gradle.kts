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
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation("androidx.test.ext:junit-ktx:1.1.3")

                implementation("androidx.test:runner:1.4.0")
                implementation("androidx.test:rules:1.4.0")
                // Optional -- Hamcrest library
                implementation("org.hamcrest:hamcrest-library:1.3")
                // Optional -- UI testing with Espresso
                implementation("androidx.test.espresso:espresso-core:3.4.0")
                // Optional -- UI testing with UI Automator
                implementation("androidx.test.uiautomator:uiautomator:2.2.0")

            }
        }
        val androidMain by getting {
            dependencies {
                implementation(project(":libraries:opencv-android"))
//                implementation(project(mapOf("path" to ":libraries:opencv-android")))
            }

        }
        val androidTest by getting {
            dependencies {
                implementation(kotlin("test"))

//                implementation(project(mapOf("path" to ":libraries:opencv-android")))
            }

            tasks.withType<Test> {
                systemProperty("java.library.path", "src/androidTest/jniLibs/")
                }
        }
        val iosMain by getting
        val iosTest by getting
    }

}

android {
    compileSdk = 31
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].jniLibs.srcDir("src/androidMain/jniLibs")

    defaultConfig {
        minSdk = 21
        targetSdk = 31
        externalNativeBuild {
            cmake {
                cppFlags += "-std=c++11 -frtti -fexceptions"
                arguments += "-DOpenCV_DIR=../libraries/opencv-android/sdk/native/jni"
            }
        }
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
    externalNativeBuild {
        cmake {
            path = file("src/androidMain/cpp/CMakeLists.txt")
            version = "3.10.2"
        }
    }
}