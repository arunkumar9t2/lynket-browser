# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\development\android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}
-keep class org.apache.http.** { *; }
-dontwarn org.apache.http.**
-keep class android.net.http.** { *; }
-dontwarn android.net.http.**

-keepclasseswithmembernames public class * extends com.orm.SugarRecord { *; }
-keepclasseswithmembernames class com.orm.* { *; }
-dontwarn com.google.common.**
-keep public class org.jsoup.** {
    public *;
}
-keepattributes EnclosingMethod

-dontwarn okio.**

-dontwarn sun.misc.Unsafe
-dontwarn org.w3c.dom.bootstrap.DOMImplementationRegistry

-keepattributes *Annotation*
-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**

-keep public class * implements com.bumptech.glide.module.GlideModule