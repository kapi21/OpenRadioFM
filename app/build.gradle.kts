plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.openradiofm"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.openradiofm"
        minSdk = 21
        targetSdk = 35
        versionCode = 22
        versionName = "5.0.10 (Stable)"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // V4.7: Configuraciones de firma recomendadas para distribución en radios chinas.
    // Usar V1 y V2 asegura compatibilidad con Android 4.4 hasta 14.
    signingConfigs {
        create("release") {
            // Para automatizar la firma, rellena estos campos y apunta a tu archivo .jks
            // storeFile = file("ruta/a/tu/llave.jks")
            // storePassword = "password"
            // keyAlias = "alias"
            // keyPassword = "password"
            enableV1Signing = true
            enableV2Signing = true
        }
    }

    buildTypes {
        release {
            // CRÍTICO: Mantener false. El uso de Reflexión para los motores de radio (K706, MT8163, QS6)
            // hará que la app crashee si ProGuard/R8 ofusca los nombres de las clases de hardware.
            isMinifyEnabled = false
            isShrinkResources = false 
            
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            
            // Vincular con la firma si se configura arriba
            // signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        aidl = true
        compose = true
        buildConfig = true
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }

    // Icon packs: empaquetar PNGs de packs como assets (sin mover archivos).
    sourceSets {
        getByName("main") {
            assets.srcDirs("src/main/assets", "../icons_color")
        }
    }
}

// Built-in Kotlin configuration in AGP 9.0+
kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.media)
    implementation(libs.androidx.palette)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    
    // Network & Images
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)
    implementation(libs.glide)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    
    // ExoPlayer (Media3) para streaming robusto
    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.media3:media3-exoplayer-hls:1.3.1")
    implementation("androidx.media3:media3-exoplayer-dash:1.3.1")
    implementation("androidx.media3:media3-exoplayer-rtsp:1.3.1")
    implementation("androidx.media3:media3-datasource-okhttp:1.3.1")
}
