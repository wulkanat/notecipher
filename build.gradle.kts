// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.1.0-alpha01")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.32")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.3.5")
        // classpath("com.google.dagger:hilt-android-gradle-plugin:2.33-beta")
    }

    // NOTE: Do not place your application dependencies here; they belong
    // in the individual module build.gradle files
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
