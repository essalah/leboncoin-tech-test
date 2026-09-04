plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "fr.leboncoin.data"
    compileSdk = 37

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // `api`, not `implementation`: :core-network and :core-database each contribute their own
    // Hilt @Module, and Hilt aggregates every module's bindings into the single component it
    // generates in :app. `implementation` would hide these two from :app's compile classpath
    // and Hilt's KSP step would fail to find their bindings.
    api(project(":core-network"))
    api(project(":core-database"))

    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    // See the matching comment in core-network/build.gradle.kts: needed so
    // `connectedAndroidTest` doesn't crash instantiating the runner for this module's
    // instrumented-test-less test APK.
    androidTestImplementation(libs.androidx.test.runner)
}
