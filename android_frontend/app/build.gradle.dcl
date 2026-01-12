androidApplication {
    namespace = "org.example.app"

    /*
     * Packaging configuration to avoid mergeDebugJavaResource failures due to duplicate
     * META-INF license/notice files coming from transitive dependencies (commonly test engines).
     *
     * Note: This is kept intentionally narrow and safe: only excludes common license/notice
     * artifacts that do not affect runtime behavior.
     */
    packagingOptions {
        resources {
            excludes += setOf(
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/LICENSE.md",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/NOTICE.md",
                "META-INF/DEPENDENCIES",
                "META-INF/AL2.0",
                "META-INF/LGPL2.1"
            )
        }
    }

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
    }
}
