# StarWeave Android ProGuard Rules

# Keep Retrofit interface
-keep,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Keep Gson data classes
-keep class com.starweave.android.model.** { *; }
-keep class com.starweave.android.api.ApiResponse { *; }

# Keep OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Compose
-dontwarn androidx.compose.**
