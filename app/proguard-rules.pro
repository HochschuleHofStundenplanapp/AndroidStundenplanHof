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

#bottombar f�r Hike
-dontwarn com.roughike.bottombar.**
#file picker for import of files
-keep class com.aditya.filebrowser
-keep class android.support.v7.widget.SearchView { *; }


# not needed
#-keepattributes SourceFile,LineNumberTable
#-keep class com.parse.*{ *; }

# Smack specific configuration
# If you use ProGuard, you have to configure it so that no important
# Smack classes are optimized away:
#
#-keep class org.jivesoftware.smack.** { *; }
#-keep class org.jivesoftware.smackx.** { *; }
-keep class org.jivesoftware.smack.XMPPConnection
-keep class org.jivesoftware.smack.XMPPException
-keep class org.jivesoftware.smack.packet.Message
-keep class org.jivesoftware.smack.packet.Message.Type

# Smack specific configuration
-keep class de.measite.smack.AndroidDebugger { *; }
-keep class * implements org.jivesoftware.smack.initializer.SmackInitializer
-keep class * implements org.jivesoftware.smack.provider.IQProvider
-keep class * implements org.jivesoftware.smack.provider.PacketExtensionProvider
-keep class * extends org.jivesoftware.smack.packet.Packet
-keep class org.jivesoftware.smack.CustomSmackConfiguration
-keep class org.jivesoftware.smackx.disco.ServiceDiscoveryManager
-keep class org.jivesoftware.smackx.xhtmlim.XHTMLManager
-keep class org.jivesoftware.smackx.muc.MultiUserChat
-keep class org.jivesoftware.smackx.bytestreams.ibb.InBandBytestreamManager
-keep class org.jivesoftware.smackx.bytestreams.socks5.Socks5BytestreamManager
-keep class org.jivesoftware.smackx.filetransfer.FileTransferManager
-keep class org.jivesoftware.smackx.iqlast.LastActivityManager
-keep class org.jivesoftware.smackx.commands.AdHocCommandManager
-keep class org.jivesoftware.smackx.ping.PingManager
-keep class org.jivesoftware.smackx.privacy.PrivacyListManager
-keep class org.jivesoftware.smackx.time.EntityTimeManager
-keep class org.jivesoftware.smackx.vcardtemp.VCardManager

-keepclasseswithmembers class * extends org.jivesoftware.smack.sasl.SASLMechanism {
    public <init>(org.jivesoftware.smack.SASLAuthentication);
}

#

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
-optimizationpasses 4
-allowaccessmodification
