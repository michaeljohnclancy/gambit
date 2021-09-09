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
                implementation("org.tensorflow:tensorflow-lite-task-vision:0.1.0")
                implementation("org.tensorflow:tensorflow-lite-support:0.1.0")
//                implementation("io.reactivex.rxjava3:rxkotlin:3.0.1")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(project(":libraries:opencv-android"))

            }
        }
        val androidTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("junit:junit:4.13.2")
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
    }
}