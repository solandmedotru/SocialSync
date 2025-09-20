plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.hilt.android.gradle)
    alias(libs.plugins.kotlin.serialization) // <-- ДОБАВЛЕН ПЛАГИН
}

android {
    namespace = "ru.devsoland.socialsync"
    compileSdk = 36

    packaging {
        resources {
            excludes += "/META-INF/INDEX.LIST"
            // Вы также можете здесь исключить другие распространенные конфликтующие файлы, если они появятся:
            excludes += "/META-INF/DEPENDENCIES"
            // excludes += "/META-INF/LICENSE"
            // excludes += "/META-INF/LICENSE.txt"
            // excludes += "/META-INF/LICENSE.md"
            // excludes += "/META-INF/NOTICE"
            // excludes += "/META-INF/NOTICE.txt"
            // excludes += "/META-INF/NOTICE.md"
            // excludes += "/META-INF/ASL2.0"
            // excludes += "/META-INF/*.kotlin_module"
        }
    }

    defaultConfig {
        applicationId = "ru.devsoland.socialsync"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "GEMINI_API_KEY",
            "\"${project.findProperty("GEMINI_API_KEY") ?: "your_api_key_here"}\""
        )


    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            buildConfigField(
                "String",
                "GEMINI_API_KEY",
                "\"${project.findProperty("GEMINI_API_KEY") ?: "your_api_key_here"}\""
            )
        }

    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
    }
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
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")

    // Navigation Compose
    implementation(libs.androidx.navigation.compose)

    // Coil - ADDED
    implementation(libs.coil.compose)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.generativeai)
    ksp(libs.androidx.room.compiler)

    // Kotlinx Serialization
    implementation(libs.kotlinx.serialization.json) // <-- ДОБАВЛЕНА ЗАВИСИМОСТЬ

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Calendar
    implementation("com.kizitonwose.calendar:compose:2.8.0")

    implementation("com.google.genai:google-genai:1.17.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

