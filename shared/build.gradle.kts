import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType


plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
}

version = "1.0"

kotlin {
    android()

//    val iosTarget: (String, KotlinNativeTarget.() -> Unit) -> KotlinNativeTarget = when {
//        System.getenv("SDK_NAME")?.startsWith("iphoneos") == true -> ::iosArm64
//        System.getenv("NATIVE_ARCH")?.startsWith("arm") == true -> ::iosSimulatorArm64
//        else -> ::iosX64
//    }

//    iosTarget("ios") {}
//
//    cocoapods {
//        ios.deploymentTarget = "14.1"
//        framework {
//            summary = "Gambit shared library"
//            homepage = "https://kt.wadiyatalkinabeet.com/gambit"
//            baseName = "shared"
//            isStatic = false
//            podfile = project.file  ("../iosApp/Podfile")
//        }
//
//        specRepos {
//            url("https://github.com/michaeljohnclancy/PodSpecs.git/")
//        }
//
//        xcodeConfigurationToNativeBuildType["CUSTOM_DEBUG"] = NativeBuildType.DEBUG
//        xcodeConfigurationToNativeBuildType["CUSTOM_RELEASE"] = NativeBuildType.RELEASE
//
//        pod("LegoCV")
//    }

    sourceSets {
        all {
            languageSettings.optIn("org.mylibrary.OptInAnnotation")
        }

        val commonMain by getting {
            dependencies {
                implementation("com.github.h0tk3y.geometry:geometry:v0.1")
                implementation("com.github.h0tk3y.geometry:algorithms:v0.1")
                implementation("com.google.android.play:core-ktx:1.8.1")

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")

                implementation("org.tensorflow:tensorflow-lite-support:0.2.0")
                implementation("org.tensorflow:tensorflow-lite-metadata:0.1.0")
                implementation("org.tensorflow:tensorflow-lite-gpu:2.3.0")

//                implementation("org.jetbrains.kotlinx:multik-api:0.0.1")
//                implementation("org.jetbrains.kotlinx:multik-default:0.0.1")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))

                implementation("org.robolectric:robolectric:4.4")
            }
        }
        val androidMain by getting {
            dependencies {

                val cameraxVersion = "1.0.1"

                implementation("com.quickbirdstudios:opencv:4.5.3.0")

                implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.0-rc01")

                implementation("androidx.camera:camera-core:$cameraxVersion")
                implementation("androidx.camera:camera-camera2:$cameraxVersion")
                implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
                implementation("androidx.camera:camera-view:1.0.0-alpha29")

                implementation("com.github.skgmn:cameraxx:0.6.0")
                implementation("androidx.test:core-ktx:1.4.0")

            }
        }

        val androidTest by getting {
            dependencies {
                implementation(kotlin("test"))

                implementation("androidx.test:core-ktx:1.4.0")
                implementation("androidx.test.ext:junit-ktx:1.1.3")

                implementation("androidx.test:runner:1.4.0")
                implementation("androidx.test:rules:1.4.0")

                implementation("org.junit.jupiter:junit-jupiter-engine:5.8.0")
            }

            tasks.withType<Test> {
                useJUnitPlatform()
                }
        }
//        val iosMain by getting
//        val iosTest by getting
    }
}

android {
    compileSdk = 31
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].mlModels.srcDir("src/androidMain/ml")

    defaultConfig {
        minSdk = 21
        targetSdk = 31
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    }
    testOptions {
        //Tmp as log isnt mocked, mock log instead of isReturnDefaultValues
        unitTests.isReturnDefaultValues = true
        unitTests.isIncludeAndroidResources = true
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }

    buildFeatures {
        mlModelBinding = true
    }

    packagingOptions {
        resources.excludes.add("META-INF/*")
    }
}