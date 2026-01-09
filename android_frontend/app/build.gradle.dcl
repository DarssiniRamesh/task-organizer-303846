androidApplication {
    namespace = "org.example.app"

    dependencies {
        implementation("org.apache.commons:commons-text:1.11.0")
        implementation(project(":utilities"))

        // UI
        implementation("com.google.android.material:material:1.12.0")
        implementation("androidx.recyclerview:recyclerview:1.3.2")
        implementation("androidx.core:core-ktx:1.13.1")
        implementation("androidx.appcompat:appcompat:1.7.0")
        implementation("androidx.constraintlayout:constraintlayout:2.1.4")

        // Architecture
        implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
        implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")

        // Persistence (Room)
        implementation("androidx.room:room-runtime:2.6.1")
        implementation("androidx.room:room-ktx:2.6.1")

        // Coroutines
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

        // Unit testing (JVM)
        // NOTE: This project uses JUnit 5 via settings.gradle.dcl defaults; tests are written for JUnit Jupiter.
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
        implementation("androidx.arch.core:core-testing:2.2.0")
        implementation("androidx.room:room-testing:2.6.1")
        implementation("org.robolectric:robolectric:4.13")
        implementation("androidx.test:core:1.6.1")

        // Ensure test discovery works even if the Release unit test task isn't configured for JUnit5.
        // JUnit4 + Vintage enables discovery/execution under both JUnit4 and JUnit Platform runners.
        implementation("junit:junit:4.13.2")
        implementation("org.junit.vintage:junit-vintage-engine:5.10.2")
    }
}
