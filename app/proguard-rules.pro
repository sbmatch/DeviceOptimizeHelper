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
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-dontpreverify
#-keep class ma.DeviceOptimizeHelper.**{*;}
-keep class com.sbmatch.deviceopt.R$* { *; }
-keep class com.sbmatch.deviceopt.Service{ *; }
-keep interface *
-keep class com.rosan.dhizuku.**{ *; }
-keep class com.kongzue.dialogx.** { *; }
-dontwarn com.kongzue.dialogx.**
# 若启用模糊效果，请增加如下配置：
-dontwarn androidx.renderscript.**
-keep class android.** { *; }
-keep class androidx.** { *; }
-keep class com.android.** { *; }
-keep class * extends androidx.fragment.app.Fragment { <init>(); }

# 指定混淆是采用的算法，后面的参数是一个过滤器
# 这个过滤器是谷歌推荐的算法，一般不做更改
-optimizations !code/simplification/cast,!field/*,!class/merging/*

# 抛出异常时保留代码行号,保持源文件以及行号
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception  # Optional: Keep custom exceptions.

#-----------------------------6.默认保留区-----------------------
# 保持 native 方法不被混淆
-keepclasseswithmembernames class * {
    native <methods>;
}

-optimizationpasses 5
-printmapping mapping.txt

# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
-dontwarn android.media.LoudnessCodecController$OnLoudnessCodecUpdateListener
-dontwarn android.media.LoudnessCodecController
