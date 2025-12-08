import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.plugin.compose")
    id("kotlin-parcelize")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.example.mindlens"
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
    val webGoogleClientID: String =
        localProperties.getProperty("WEB_GOOGLE_CLIENT_ID") ?: error("WEB_GOOGLE_CLIENT_ID not found in local.properties")
    val androidGoogleClientID: String =
        localProperties.getProperty("ANDROID_GOOGLE_CLIENT_ID") ?: error("ANDROID_GOOGLE_CLIENT_ID not found in local.properties")

    buildFeatures {
        viewBinding = true
        buildConfig = true
        resValues = true
    }

    defaultConfig {
        applicationId = "com.example.mindlens"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        val supabaseKey = localProperties.getProperty("SUPABASE_ANON_KEY") ?: ""
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseKey\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("String", "SUPABASE_URL", "$supabaseUrl")
            buildConfigField("String", "POSTGRES_PASSWORD", "$postgresPassword")
            buildConfigField("String", "SUPABASE_ANON_KEY", "$supabaseAnonKey")
            buildConfigField("String", "ANDROID_GOOGLE_CLIENT_ID", "$androidGoogleClientID")
            buildConfigField("String", "WEB_GOOGLE_CLIENT_ID", "$webGoogleClientID")
        }
        debug {
            buildConfigField("String", "SUPABASE_URL", "$supabaseUrl")
            buildConfigField("String", "POSTGRES_PASSWORD", "$postgresPassword")
            buildConfigField("String", "SUPABASE_ANON_KEY", "$supabaseAnonKey")
            buildConfigField("String", "ANDROID_GOOGLE_CLIENT_ID", "$androidGoogleClientID")
            buildConfigField("String", "WEB_GOOGLE_CLIENT_ID", "$webGoogleClientID")

        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
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
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0") // or latest version

    // serializer
    implementation("io.github.jan-tennert.supabase:serializer-moshi:3.0.2")

    // Google Sign-In
    implementation("androidx.credentials:credentials:1.5.0")
    implementation ("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    // for android 13 and below
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0")
    implementation("io.github.jan-tennert.supabase:auth-kt:3.0.2")

    // Supabase
    implementation(platform("io.github.jan-tennert.supabase:bom:3.0.2"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt:3.0.2")
    implementation("io.github.jan-tennert.supabase:auth-kt:3.0.2")
    implementation("io.github.jan-tennert.supabase:realtime-kt:3.0.2")

    // Compose Helpers
    implementation("io.github.jan-tennert.supabase:compose-auth:3.0.2")
    implementation("io.github.jan-tennert.supabase:compose-auth-ui:3.0.2")

    // KTOR
    implementation("io.ktor:ktor-client-android:3.0.1")
    implementation("io.ktor:ktor-client-core:3.0.1")
    implementation("io.ktor:ktor-client-cio:3.0.1")
    implementation("io.ktor:ktor-client-content-negotiation:3.0.1")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.1")

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

    //Coil for images
    implementation("io.coil-kt:coil-compose:2.6.0")    //Youtube player
    implementation("com.pierfrancescosoffritti.androidyoutubeplayer:core:12.1.0")

    //MAPS
    implementation("com.google.maps.android:maps-compose:4.3.0")

    implementation("com.google.android.gms:play-services-maps:18.2.0")

    implementation("org.osmdroid:osmdroid-android:6.1.18")

    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation("com.google.code.gson:gson:2.10.1")
    implementation("io.github.jan-tennert.supabase:auth-kt:3.0.0")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
}

