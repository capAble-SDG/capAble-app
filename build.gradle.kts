// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google() // Make sure Google's Maven repository is included
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.0") // Specify the correct Gradle Plugin version
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:VERSION") // Specify the correct Kotlin version
    classpath("com.google.gms:google-services:4.4.0") // Add Google services plugin (for Google Analytics)
    }
}

allprojects {
    repositories {
        google() // Make sure Google's Maven repository is included
        mavenCentral()
    }
}




plugins {
    id("com.android.application") version "8.1.4" apply false
    id("org.jetbrains.kotlin.android") version "1.8.10" apply false
    id("com.google.gms.google-services") version "4.4.0" apply false
}