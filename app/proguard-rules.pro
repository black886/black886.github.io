# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep FFmpeg related classes
-keep class com.arthenica.ffmpegkit.** { *; }
-keep class org.opencv.** { *; }

# Keep ML Kit classes
-keep class com.google.mlkit.** { *; }

# Keep ExoPlayer classes
-keep class com.google.android.exoplayer2.** { *; }

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel

# Keep Room entities
-keep class com.cutwa.videoeditor.data.** { *; }

# Keep Gson classes for JSON parsing
-keep class com.google.gson.** { *; }
-keepattributes Signature
-keepattributes *Annotation*