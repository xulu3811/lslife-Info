# kotlinx.serialization
-keepattributes Signature
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class **$$serializer { *; }
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep class com.lianshan.lslife.core.model.** { *; }
-keep class com.lianshan.lslife.core.network.** { *; }

# Retrofit
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
