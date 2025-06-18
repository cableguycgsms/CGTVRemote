# Keep public API
-keep class com.example.mysdk.** { public *; }

# Obfuscate everything else
-dontwarn
-dontnote

# Rename source files
-renamesourcefileattribute "Encrypted"

# Remove debug logs
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

# Aggressive obfuscation
-overloadaggressively
-allowaccessmodification
