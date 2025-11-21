import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.example.projectwithcompose"
    compileSdk = 36

    val localProperties = Properties()
    val localPropertiesFile = File(rootDir, "local.properties")
    if (localPropertiesFile.exists() && localPropertiesFile.isFile) {
        localPropertiesFile.inputStream().use {
            localProperties.load(it)
        }
    }

    val supabaseUrl: String =
        localProperties.getProperty("SUPABASE_URL") ?: error("SUPABASE_URL not found in local.properties")
    val postgresPassword: String =
        localProperties.getProperty("POSTGRES_PASSWORD") ?: error("POSTGRES_PASSWORD not found in local.properties")
    val supabaseAnonKey: String =
        localProperties.getProperty("SUPABASE_ANON_KEY") ?: error("SUPABASE_ANON_KEY not found in local.properties")

    buildFeatures {
        viewBinding = true
        buildConfig = true
        resValues = true
    }

    defaultConfig {
        applicationId = "com.example.projectwithcompose"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("String", "SUPABASE_URL", "$supabaseUrl")
            buildConfigField("String", "POSTGRES_PASSWORD", "$postgresPassword")
            buildConfigField("String", "SUPABASE_ANON_KEY", "$supabaseAnonKey")
        }
        debug {
            buildConfigField("String", "SUPABASE_URL", "$supabaseUrl")
            buildConfigField("String", "POSTGRES_PASSWORD", "$postgresPassword")
            buildConfigField("String", "SUPABASE_ANON_KEY", "$supabaseAnonKey")

        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // General Compose
    val composeBom = platform("androidx.compose:compose-bom:2025.11.01")
    implementation(composeBom)
    implementation("androidx.compose.material3:material3:1.4.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.9.5")
    debugImplementation("androidx.compose.ui:ui-tooling:1.9.5")
    implementation("androidx.activity:activity-compose:1.12.0")


    // serializer
    implementation("io.github.jan-tennert.supabase:serializer-moshi:3.2.6")
    // Google Sign-In
    implementation("io.github.jan-tennert.supabase:auth-kt:3.2.6")
    // Supabase
    implementation(platform("io.github.jan-tennert.supabase:bom:3.2.6"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt:1.4.7")
    implementation("io.github.jan-tennert.supabase:gotrue-kt:1.4.7")
    implementation("io.github.jan-tennert.supabase:realtime-kt:1.4.7")

    // Ktor for HTTP requests
    implementation("io.ktor:ktor-client-android:2.3.7")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.7")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.9.6")

    // Google Maps Compose
    implementation("com.google.maps.android:maps-compose:6.12.0")

    // CameraX (For your ML Feature)
    val cameraxVersion = "1.5.1"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")

    // Icons
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
}