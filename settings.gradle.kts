pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "Gambit"

//This is temporary as android studio has a bug stopping me from being able to import a module:
//https://stackoverflow.com/questions/68636809/cant-import-module-in-android-studio-arctic-fox

include(":androidApp")
include(":shared")

include(":libs:opencv-android")