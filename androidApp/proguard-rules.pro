# ============================================================
# Regras de R8/ProGuard do 2048 Classic
# ============================================================

# ---- Relatórios de crash legíveis ----
-keepattributes SourceFile,LineNumberTable,Signature,InnerClasses,EnclosingMethod,*Annotation*
-renamesourcefileattribute SourceFile

# ---- WorkManager / Room ----
# O SDK do AdMob usa WorkManager, que carrega o WorkDatabase (Room)
# por REFLEXÃO em runtime. Sem estas regras o R8 remove as classes e
# o app crasha na abertura: "Failed to create an instance of WorkDatabase".
-keep class androidx.work.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep class androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# ---- Koin (injeção de dependência) ----
-keep class org.koin.** { *; }

# ---- kotlinx-coroutines (ServiceLoader do Main dispatcher) ----
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# ---- SDKs Google (reforço; já incluem consumer rules) ----
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.android.ump.** { *; }
-keep class com.android.billingclient.** { *; }
-keep class com.google.android.play.core.** { *; }
