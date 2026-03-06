# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# V4.7: REGLAS CRÍTICAS PARA OPENRADIOFM
# La app usa Reflexión y AIDL para hablar con el hardware de la radio.
# Estas reglas protegen esas clases de ser borradas o renombradas por error.

# Mantener motores de radio
-keep class com.example.openradiofm.data.source.** { *; }

# Mantener interfaces AIDL generadas
-keep interface com.hcn.autoradio.** { *; }
-keep class com.hcn.autoradio.** { *; }
-keep interface com.nwd.radio.service.** { *; }
-keep class com.nwd.radio.service.** { *; }

# Mantener clases de reflexión del sistema (McuManager, TunerManager, etc.)
-keep class android.carsource.** { *; }
-keep class com.qf.carsink.** { *; }