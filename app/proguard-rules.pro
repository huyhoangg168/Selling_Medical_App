# ═══════════════════════════════════════════════════════════════
# PROGUARD RULES - SELLING MEDICAL APP
# Mục đích: Obfuscate code, bảo vệ logic và API endpoints
# ═══════════════════════════════════════════════════════════════

# ───────────────────────────────────────────────────────────────
# 1. CƠ BẢN - Giữ thông tin debug (có thể tắt khi production)
# ───────────────────────────────────────────────────────────────
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ───────────────────────────────────────────────────────────────
# 2. ANDROID COMPONENTS - Giữ các component quan trọng
# ───────────────────────────────────────────────────────────────
# Keep Activities, Services, Receivers
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.Application

# Keep View constructors (cho layout inflation)
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Keep onClick methods từ XML
-keepclassmembers class * extends android.app.Activity {
    public void *(android.view.View);
}

# ───────────────────────────────────────────────────────────────
# 3. FIREBASE - Keep các class Firebase
# ───────────────────────────────────────────────────────────────
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Keep Firebase Messaging Service
-keep class * extends com.google.firebase.messaging.FirebaseMessagingService {
    *;
}

# ───────────────────────────────────────────────────────────────
# 4. RETROFIT + GSON - Keep model classes và API interfaces
# ───────────────────────────────────────────────────────────────
# Retrofit
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*

-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keepclassmembernames interface * {
    @retrofit2.http.* <methods>;
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Gson - Keep các model classes (QUAN TRỌNG)
-keep class com.google.gson.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep Gson TypeToken (FIX: Missing type parameter error)
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken { *; }
-keepclassmembers class * extends com.google.gson.reflect.TypeToken {
    <init>(...);
}

# Keep generic signature (CRITICAL for TypeToken)
-keepattributes Signature
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# Keep tất cả model classes (để JSON serialization hoạt động)
-keep class com.example.clientsellingmedicine.models.** { *; }
-keep class com.example.clientsellingmedicine.DTO.** { *; }

# Keep API interfaces
-keep interface com.example.clientsellingmedicine.api.** { *; }

# ───────────────────────────────────────────────────────────────
# 5. SERIALIZATION - Keep Serializable và Parcelable
# ───────────────────────────────────────────────────────────────
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

# ───────────────────────────────────────────────────────────────
# 6. LOMBOK - Keep annotations và generated code
# ───────────────────────────────────────────────────────────────
-dontwarn lombok.**
-keep class lombok.** { *; }
-keepclassmembers class * {
    @lombok.* <fields>;
    @lombok.* <methods>;
}

# ───────────────────────────────────────────────────────────────
# 7. GLIDE - Image loading library
# ───────────────────────────────────────────────────────────────
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
    <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}
-dontwarn com.bumptech.glide.**

# ───────────────────────────────────────────────────────────────
# 8. CLOUDINARY
# ───────────────────────────────────────────────────────────────
-keep class com.cloudinary.** { *; }
-dontwarn com.cloudinary.**

# ───────────────────────────────────────────────────────────────
# 9. JSOUP - HTML parser
# ───────────────────────────────────────────────────────────────
-keep class org.jsoup.** { *; }
-dontwarn org.jsoup.**

# ───────────────────────────────────────────────────────────────
# 10. LOTTIE - Animation library
# ───────────────────────────────────────────────────────────────
-keep class com.airbnb.lottie.** { *; }
-dontwarn com.airbnb.lottie.**

# ───────────────────────────────────────────────────────────────
# 11. WEBVIEW - Keep JavaScript interfaces
# ───────────────────────────────────────────────────────────────
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-keepattributes JavascriptInterface

# ───────────────────────────────────────────────────────────────
# 12. OPTIMIZATION - Aggressive obfuscation
# ───────────────────────────────────────────────────────────────
# Bật optimization passes (càng nhiều càng tốt, nhưng build lâu hơn)
-optimizationpasses 5
-overloadaggressively
-repackageclasses ''
-allowaccessmodification

# Obfuscate với dictionary tùy chỉnh (optional - tạo tên random)
-obfuscationdictionary proguard-dict.txt
-classobfuscationdictionary proguard-dict.txt
-packageobfuscationdictionary proguard-dict.txt

# ───────────────────────────────────────────────────────────────
# 13. WARNINGS - Suppress các warning không quan trọng
# ───────────────────────────────────────────────────────────────
-dontwarn javax.annotation.**
-dontwarn javax.inject.**
-dontwarn sun.misc.Unsafe
-dontwarn com.google.common.**

# ───────────────────────────────────────────────────────────────
# 14. CUSTOM - Giữ các class quan trọng của app (nếu cần)
# ───────────────────────────────────────────────────────────────
# Uncomment nếu muốn giữ một số class không bị obfuscate
# -keep class com.example.clientsellingmedicine.YourImportantClass { *; }