# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Retrofit
-keepattributes Signature
-keepattributes Exceptions
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembernames class * {
    @retrofit2.http.* <methods>;
}

# OkHttp
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }

# Room
-dontwarn android.database.**
-keep class * extends androidx.room.RoomDatabase
-dontwarn java.lang.invoke.**

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper
-dontwarn dagger.hilt.**

# Kotlin
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# kotlinx.serialization
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn kotlinx.serialization.**
-keep,includedescriptorclasses class kotlinx.serialization.** { *; }
-keep,includedescriptorclasses class kotlinx.serialization.json.** { *; }
