import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

// Production AdMob IDs live in local.properties (never committed).
// Fallbacks are Google's official test IDs, safe for development.
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

/** Reads from local.properties first, then environment variables (CI), then a fallback. */
fun localProp(key: String, fallback: String): String =
    localProperties.getProperty(key) ?: System.getenv(key) ?: fallback

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

dependencies {
    implementation(project(":shared"))

    implementation(libs.androidx.activity.compose)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

android {
    namespace = "com.hcn2048.game"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.hcn2048.game"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0.0"

        manifestPlaceholders["admobAppId"] =
            localProp("ADMOB_APP_ID", "ca-app-pub-3940256099942544~3347511713")

        buildConfigField(
            "String", "ADMOB_BANNER_ID",
            "\"${localProp("ADMOB_BANNER_ID", "ca-app-pub-3940256099942544/6300978111")}\""
        )
        buildConfigField(
            "String", "ADMOB_INTERSTITIAL_ID",
            "\"${localProp("ADMOB_INTERSTITIAL_ID", "ca-app-pub-3940256099942544/1033173712")}\""
        )
        buildConfigField(
            "String", "ADMOB_REWARDED_ID",
            "\"${localProp("ADMOB_REWARDED_ID", "ca-app-pub-3940256099942544/5224354917")}\""
        )
    }

    val releaseStorePath = localProp("RELEASE_STORE_FILE", "")
    val hasReleaseSigning = releaseStorePath.isNotBlank()

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(releaseStorePath)
                storePassword = localProp("RELEASE_STORE_PASSWORD", "")
                keyAlias = localProp("RELEASE_KEY_ALIAS", "")
                keyPassword = localProp("RELEASE_KEY_PASSWORD", "")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = if (hasReleaseSigning) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}
