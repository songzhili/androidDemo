import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.keyitech.actionsdemo"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.keyitech.actionsdemo"
        minSdk = 24
        targetSdk = 36
        versionCode = 2
        versionName = "1.0.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    val keystoreProperties = Properties()
    val keystorePropertiesFile = rootProject.file("keystore.properties")
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { keystoreProperties.load(it) }
    }

    fun env(name: String): String? = System.getenv(name)?.takeIf { it.isNotBlank() }
    val storeFilePath = keystoreProperties.getProperty("storeFile") ?: env("KEYSTORE_FILE")

    signingConfigs {
        create("release") {
            val storeFilePath = keystoreProperties.getProperty("storeFile") ?: env("KEYSTORE_FILE")
            if (!storeFilePath.isNullOrBlank()) {
                storeFile = file(storeFilePath)
                storePassword = keystoreProperties.getProperty("storePassword") ?: env("KEYSTORE_PASSWORD")
                keyAlias = keystoreProperties.getProperty("keyAlias") ?: env("KEY_ALIAS")
                keyPassword = keystoreProperties.getProperty("keyPassword") ?: env("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            // 关键：只有在拿到 storeFile 时才绑定，否则你本地/CI 没配会直接报错
            if (!storeFilePath.isNullOrBlank()) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}