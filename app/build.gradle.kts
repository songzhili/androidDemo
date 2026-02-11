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
        versionName = "1.0.6"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // ---------- Signing (KTS) ----------
    fun env(name: String): String? = System.getenv(name)?.takeIf { it.isNotBlank() }
    val isCi = env("CI") != null

    // 本地可选：keystore.properties（不提交git）
    val ksProps = Properties()
    val ksPropsFile = rootProject.file("keystore.properties")
    if (ksPropsFile.exists()) {
        ksPropsFile.inputStream().use { ksProps.load(it) }
    }

    val storeFilePath = ksProps.getProperty("storeFile") ?: env("KEYSTORE_FILE")
    val storePassword = ksProps.getProperty("storePassword") ?: env("KEYSTORE_PASSWORD")
    val keyAlias = ksProps.getProperty("keyAlias") ?: env("KEY_ALIAS")
    val keyPassword = ksProps.getProperty("keyPassword") ?: env("KEY_PASSWORD")

    // CI 必须有签名信息，否则直接失败（避免产出 unsigned）
    if (isCi) {
        val missing = mutableListOf<String>()
        if (storeFilePath.isNullOrBlank()) missing += "KEYSTORE_FILE"
        if (storePassword.isNullOrBlank()) missing += "KEYSTORE_PASSWORD"
        if (keyAlias.isNullOrBlank()) missing += "KEY_ALIAS"
        if (keyPassword.isNullOrBlank()) missing += "KEY_PASSWORD"
        if (missing.isNotEmpty()) {
            throw GradleException("CI signing missing: ${missing.joinToString(", ")}")
        }
    }

    signingConfigs {
        create("release") {
            // 本地/CI 都走这套，只要参数齐
            if (!storeFilePath.isNullOrBlank()) {
                storeFile = file(storeFilePath)
                this.storePassword = storePassword
                this.keyAlias = keyAlias
                this.keyPassword = keyPassword
            }
        }
    }



    buildTypes {
        release {
            // 关键：release 强制绑定 signingConfig（CI 缺配置会在上面 fail）
            signingConfig = signingConfigs.getByName("release")
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