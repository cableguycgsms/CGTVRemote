# Obfuscate aggressively
-dontusemixedcaseclassnames
-dontpreverify
-overloadaggressively
-allowaccessmodification
-renamesourcefileattribute SourceFile
-keepattributes LineNumberTable,SourceFile

# Only keep your SDK's public API
# (adjust this based on what you *want* consumers to access)

-keep public class com.cableguy.remotesdk.Controls {
    public *;
}

-keep public class com.cableguy.remotesdk.RemoteController {
    public *;
}

-keep public class com.cableguy.remotesdk.utils.QRUtils {
    public *;
}

# Hide everything else
# You don't want other internal classes to be readable

# If you have a utils package that should be hidden:
# -keep class com.cableguy.remotesdk.utils.** { *; }

# Optional: Remove debugging metadata
-keepattributes *Annotation*

# Optional: Strip Kotlin metadata (if using Kotlin)
-if class kotlin.Metadata
-keep class kotlin.Metadata { *; }

# Keep all SDK classes so R8 doesn’t think they’re missing
-keep class com.cableguy.remotesdk.** { *; }
