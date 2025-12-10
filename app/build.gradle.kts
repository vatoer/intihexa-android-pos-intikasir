plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.services)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
}

android {
    namespace = "id.stargan.intikasir"
    compileSdk = 36

    defaultConfig {
        applicationId = "id.stargan.intikasir"
        minSdk = 29
        targetSdk = 36
        versionCode = 10001
        versionName = "1.0.1-beta"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // 1. Mengaktifkan R8 (Pengecilan ukuran & Obfuscation)
            // Ini akan menghilangkan Warning "Deobfuscation file"
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // 2. Ini untuk memperbaiki Warning "Native code / debug symbols"
            ndk {
                debugSymbolLevel = "FULL"
            }

            // 3. Generate native debug symbols
            isDebuggable = false
        }

        debug {
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
    }

    // Ensure native debug symbols are packaged
    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        // remove deprecated jvmTarget usage handled below
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }
    buildFeatures {
        compose = true
    }
}

configurations.all {
    exclude(group = "com.intellij", module = "annotations")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui.text)


    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.engage.core)
    ksp(libs.androidx.room.compiler)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Gson
    implementation(libs.gson)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Coil
    implementation(libs.coil.compose)

    // Security (EncryptedSharedPreferences)
    implementation(libs.androidx.security.crypto)

    // Image cropping
    implementation(libs.ucrop)

    // Apache POI for XLSX export
    implementation(libs.poi.ooxml)


    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    // Use root project config file
    config.setFrom(files(rootProject.file("detekt.yml")))
    autoCorrect = true
    // Do not fail the build on detekt findings during CI/local build verification
    ignoreFailures = true
}

ktlint {
    android.set(true)
    outputColorName.set("RED")
    ignoreFailures.set(false)
    reporters { reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN) }
}

tasks.register("quality") {
    group = "verification"
    description = "Run code quality checks (detekt, ktlint, unit tests)."
    dependsOn("detekt", "ktlintCheck", "test")
}
