plugins {
    alias(libs.plugins.android.application)
}

import com.android.build.api.variant.ApplicationAndroidComponentsExtension

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

// Este APK existe solo para el ZIP Magisk (overlay en /system/priv-app/...).
// En el dispositivo ya existe com.android.fmradio.ext firmado por el OEM, así que
// Android Studio NO debe intentar instalar el variant debug de este módulo.
extensions.configure<ApplicationAndroidComponentsExtension>("androidComponents") {
    beforeVariants(selector().withBuildType("debug")) { variant ->
        variant.enable = false
    }
}
