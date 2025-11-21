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

# ========== Activation Security ==========
# Keep security classes but obfuscate their content
-keep class id.stargan.intikasir.data.security.** { *; }
-keep class id.stargan.intikasir.data.model.Activation** { *; }

# Obfuscate public key string (make it harder to extract)
-keepclassmembers class id.stargan.intikasir.data.security.SignatureVerifier {
    private static final java.lang.String PUBLIC_KEY_BASE64;
}

# Keep EncryptedSharedPreferences
-keep class androidx.security.crypto.** { *; }
-keep class com.google.crypto.tink.** { *; }

# Keep Retrofit for activation API
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn org.codehaus.mojo.animal_sniffer.*

