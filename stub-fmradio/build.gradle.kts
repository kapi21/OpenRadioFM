plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.android.fmradio.ext"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.android.fmradio.ext"
        minSdk = 21
        targetSdk = 35
        versionCode = 1
        versionName = "0-orf-stub"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
        }
        debug {
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
