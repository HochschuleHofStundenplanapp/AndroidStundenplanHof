# To enable ProGuard in your project, edit project.properties
# to define the proguard.config property as described in that file.
#
# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in ${sdk.dir}/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the ProGuard
# include property in project.properties.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
-keepclassmembers class fqcn.of.javascript.interface.for.webview {
   public *;
}


# do not warn on Picasso, async loading image library
-dontwarn com.squareup.okhttp.**
# do not warn on this stupid fucking library of open source developers
#-dontwarn org.apache.http.**

# not needed
#-keepattributes SourceFile,LineNumberTable
#-keep class com.parse.*{ *; }
#-libraryjars /libs/Parse-1.5.1.jar
#-libraryjars /libs/crashlytics.jar
#-libraryjars /libs/picasso-2.3.2.jar
#-dontwarn com.parse.**
#-dontwarn com.squareup.picasso.**
#-keepclasseswithmembernames class * {
#    native <methods>;
#}

#-dontshrink
#-dontoptimize
#-dontusemixedcaseclassnames
#-dontskipnonpubliclibraryclasses
#-dontpreverify
-verbose
