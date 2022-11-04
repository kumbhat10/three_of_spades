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
-keepattributes Signature, SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception  # Optional: Keep custom exceptions.
-keepclassmembers class com.kaalikiteeggi.three_of_spades.** { *; }
# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

-keep public class com.google.android.gms.*
-dontwarn com.google.android.gms.**
-dontwarn com.squareup.picasso.**
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient{
     public *;
}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info{
     public *;
}
# skip the Picasso library classes
-keep class com.squareup.picasso.* {*;}
-dontwarn com.squareup.okhttp.**
# skip Moat classes
-keep class com.moat.* {*;}
-dontwarn com.moat.**
# skip IAB classes
-keep class com.iab.* {*;}
-dontwarn com.iab.**
-dontwarn com.google.firebase.messaging.TopicOperation$TopicOperations
-dontwarn com.google.protobuf.java_com_google_android_gmscore_sdk_target_granule__proguard_group_gtm_N1281923064GeneratedExtensionRegistryLite$Loader
# For communication with AdColony's WebView
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-keepclasseswithmembernames class * {
    native <methods>;
}

-keep class org.webrtc.* { *; }
-keep class com.google.android.gms.* { *; }
-keep public class com.google.firebase.** {*;}
-keep class com.google.android.gms.internal.** {*;}
-keepclasseswithmembers class com.google.firebase.FirebaseException

-dontwarn com.google.android.gms.**
-dontwarn android.media.**
-dontwarn java.lang.instrument.ClassFileTransformer
-dontwarn sun.misc.SignalHandler
-dontwarn java.lang.instrument.Instrumentation
-dontwarn sun.misc.Signal