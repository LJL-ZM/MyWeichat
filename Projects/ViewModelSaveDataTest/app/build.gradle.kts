plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.viewmodelsavedatatest"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.viewmodelsavedatatest"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    }

    // 开启DataBinding
    buildFeatures {
        dataBinding =  true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // ViewModel 核心
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.8.4")
    // LiveData + KTX扩展
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.4")
    // 生命周期KTX
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    // ViewModel SavedState 保存状态（屏幕旋转/系统销毁重建恢复数据）
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.8.4")
    // activity快速获取viewModel
    implementation("androidx.activity:activity-ktx:1.9.2")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}