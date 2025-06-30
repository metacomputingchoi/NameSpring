// namingengine/build.gradle.kts
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.ssc.namingengine"
    compileSdk = 35

    defaultConfig {
        minSdk = 24  // 26보다 낮게 설정하여 호환성 확보

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        // Java 8+ Time API 지원 필수
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // Java 8+ API desugaring support (LocalDateTime 등을 위해 필수)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // JSON parsing (기존 코드에서 org.json 사용)
    implementation("org.json:json:20230227")

    // Gson (선택사항)
    implementation("com.google.code.gson:gson:2.10.1")

    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // Coroutines (선택사항)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}