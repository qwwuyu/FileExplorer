#代码混淆压缩比 默认为5 一般不需要改
-optimizationpasses 5
#混淆后的类名为小写
-dontusemixedcaseclassnames
#混淆第三方 库  加上此句后 可再后面配置某些库不混淆
-dontskipnonpubliclibraryclasses
#混淆前认证，可去掉加快混淆速度
-dontpreverify
-ignorewarnings
#混淆的log 帮助排错
-verbose
#代码混淆采用的算法，一般不改变，用谷歌推荐算即可
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
#取消警告
-dontwarn
#指定不去忽略包可见的库类的成员
-dontskipnonpubliclibraryclassmembers

# 不优化输入的类文件,例如日志Log
# -dontoptimize

#保护不能被混淆的类
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends java.net.URLDecoder
-keep public class com.android.vending.licensing.ILicensingService

#保护序列化接口
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
#保护native方法
-keepclasseswithmembernames class * {
    native <methods>;
}
#保护自定义View
-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
#保护枚举类
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

#默认保护类
-keepattributes Signature
-keepattributes Exceptions,InnerClasses,Signature,SourceFile,LineNumberTable
-keepattributes *Annotation*
-keepattributes InnerClasses -dontoptimize
-keep public class com.google.vending.licensing.ILicensingService
-keep public class com.android.vending.licensing.ILicensingService

-keepclassmembers public class * extends android.view.View {
   void set*(***);
   *** get*();
}
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}
-keepclassmembers class **.R$* {
    public static <fields>;
}

# support todo
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**
-dontnote com.google.android.material.**
-dontwarn androidx.**
-keep class androidx.** { *; }
-keep interface androidx.** { *; }
-keep class androidx.annotation.Keep
-keep @androidx.annotation.Keep class * {*;}
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <methods>;
}
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <fields>;
}
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <init>(...);
}
################### 以上固定 package:com.qwwuyu.file
# package
-keep class com.qwwuyu.file.entity.** { <fields>; }