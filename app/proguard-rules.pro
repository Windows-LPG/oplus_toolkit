# Keep Gson data models and annotations
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class io.github.windowslpg.oplus_toolkit.data.model.** { *; }
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
