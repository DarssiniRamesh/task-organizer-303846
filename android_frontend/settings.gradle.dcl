pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.experimental.android-ecosystem").version("0.1.43")
}

rootProject.name = "example-android-app"

include("app")
include("list")
include("utilities")

defaults {
    androidApplication {
        jdkVersion = 17
        compileSdk = 34
        minSdk = 30

        versionCode = 1
        versionName = "0.1"
        applicationId = "org.gradle.experimental.android.app"

        testing {
            dependencies {
                // JUnit Jupiter only (no Vintage engine). Vintage is only needed for JUnit4 tests.
                implementation("org.junit.jupiter:junit-jupiter:5.10.2")

                // Launcher is for IDE/build tool integration; keep it strictly in testing runtime.
                runtimeOnly("org.junit.platform:junit-platform-launcher")
            }
        }
    }

    androidLibrary {
        jdkVersion = 17
        compileSdk = 34
        minSdk = 30

        testing {
            dependencies {
                // JUnit Jupiter only (no Vintage engine).
                implementation("org.junit.jupiter:junit-jupiter:5.10.2")
                runtimeOnly("org.junit.platform:junit-platform-launcher")
            }
        }
    }
}
