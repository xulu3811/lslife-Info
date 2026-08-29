import java.util.Properties
import java.io.File
import java.util.Locale

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

val versionPropsFile = file("version.properties")
val versionProps = Properties()

if (!versionPropsFile.exists()) {
    versionProps["versionCode"] = "1"
    versionProps["versionName"] = "0.0"
    versionProps.store(versionPropsFile.writer(), "Auto-generated Version Properties")
} else {
    versionProps.load(versionPropsFile.reader())
}

val isReleaseTask = gradle.startParameter.taskNames.any { 
    it.contains("Release", ignoreCase = true) || it.contains("assemble", ignoreCase = true) 
}

if (isReleaseTask) {
    val curCode = (versionProps["versionCode"] as? String)?.toIntOrNull() ?: 1
    val curName = (versionProps["versionName"] as? String) ?: "2.00"

    var nextVersionName = curName
    try {
        val verDouble = curName.toDouble()
        nextVersionName = String.format(Locale.US, "%.2f", verDouble + 0.01)
    } catch (e: Exception) {
        nextVersionName = "2.01"
    }

    val newCode = curCode + 1
    val newName = nextVersionName

    // Set the properties in memory for the current build
    versionProps["versionCode"] = newCode.toString()
    versionProps["versionName"] = newName
}

val appVersionCode = (versionProps["versionCode"] as String).toInt()
val appVersionName = versionProps["versionName"] as String

android {
    namespace = "com.qingyuan.lslife"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.qingyuan.lslife"
        minSdk = 24
        targetSdk = 34
        versionCode = appVersionCode
        versionName = appVersionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
            abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a"))
        }
    }

    buildTypes {
        debug {
            // 默认对接生产域名; 本地后端请临时改为 http://10.0.2.2:4000/api/
            buildConfigField("String", "API_BASE_URL", "\"https://mentalhlp.site/api/\"")
            buildConfigField("String", "WS_BASE_URL", "\"wss://mentalhlp.site/ws\"")
        }
        release {
            signingConfig = signingConfigs.getByName("debug")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("String", "API_BASE_URL", "\"https://mentalhlp.site/api/\"")
            buildConfigField("String", "WS_BASE_URL", "\"wss://mentalhlp.site/ws\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // AMap SDKs (Location & 3D Map)
    // REMOVED 3dmap as it causes a 30+MB bloat and is currently unused. 
    // implementation("com.amap.api:3dmap:latest.integration")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    debugImplementation(libs.androidx.ui.tooling)

    implementation(libs.androidx.navigation.compose)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.datastore.preferences)
    implementation(libs.coil.compose)

    // Map SDKs have been completely removed by product definition (No LBS)
    // ZXing QR Code Scanner
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    
    // CanHub Image Cropper
    implementation("com.vanniktech:android-image-cropper:4.6.0")
    
    // CameraX
    val camerax_version = "1.3.1"
    implementation("androidx.camera:camera-core:${camerax_version}")
    implementation("androidx.camera:camera-camera2:${camerax_version}")
    implementation("androidx.camera:camera-lifecycle:${camerax_version}")
    implementation("androidx.camera:camera-view:${camerax_version}")

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
}

afterEvaluate {
    val verName = android.defaultConfig.versionName ?: "0.1"
    val propsFile = project.file("version.properties")
    val localReleases = rootProject.file("../releases")
    val buildReleaseDir = layout.buildDirectory.dir("outputs/apk/release").get().asFile
    val targetReleases = File("D:/LsLife/releases")
    val nextCode = android.defaultConfig.versionCode.toString()

    val copyReleaseApk by tasks.registering {
        mustRunAfter("assembleRelease")
        doLast {
            if (!targetReleases.exists()) {
                targetReleases.mkdirs()
            }
            if (!localReleases.exists()) {
                localReleases.mkdirs()
            }
            val builtApk = buildReleaseDir.listFiles()?.firstOrNull { it.name.endsWith(".apk") }
            if (builtApk != null) {
                builtApk.copyTo(File(targetReleases, "LsLife-v${verName}-release.apk"), overwrite = true)
                builtApk.copyTo(File(localReleases, "LsLife-v${verName}-release.apk"), overwrite = true)
            }

            val props = Properties()
            if (propsFile.exists()) {
                props.load(propsFile.reader())
            }
            props.setProperty("versionCode", nextCode)
            props.setProperty("versionName", verName)
            props.store(propsFile.writer(), "Auto-incremented Version Properties")
        }
    }

    tasks.named("assembleRelease") {
        finalizedBy(copyReleaseApk)
    }
}
