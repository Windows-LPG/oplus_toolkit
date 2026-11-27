# Keep Gson data models and annotations
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class io.github.windowslpg.oplus_toolkit.data.model.** { *; }

# General attributes required for reflection
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-dontobfuscate