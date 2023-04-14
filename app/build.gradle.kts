plugins {
    id("com.android.application")
    kotlin("android")

    // ❗作为 Xposed 模块使用务必添加，其它情况可选
    id("com.google.devtools.ksp") version "1.8.0-1.0.9"
}

configurations.all {
    exclude("androidx.appcompat", "appcompat")
}

configurations.all {
    exclude("androidx.appcompat", "appcompat")
}

android {
    namespace = "cn.cyanc.xposed.noregionlimits"
    compileSdk = 33

    defaultConfig {
        applicationId = "cn.cyanc.xposed.noregionlimits"
        minSdk = 28
        targetSdk = 33
        versionCode = 1211
        versionName = "v1.2.11"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        resourceConfigurations += setOf()
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
    buildToolsVersion = "33.0.2"
}

dependencies {
    implementation("androidx.core:core-ktx:1.10.0")
    implementation("com.google.android.material:material:1.8.0")
    implementation("dev.rikka.rikkax.material:material:2.7.0")
    implementation("com.github.knightwood:material3-preference:1.4")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("dev.rikka.rikkax.appcompat:appcompat:1.6.1")
    implementation("androidx.preference:preference-ktx:1.2.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // // 基础依赖
    // implementation("com.highcapable.yukihookapi:api:1.1.8")
    // ❗作为 Xposed 模块使用务必添加，其它情况可选
    compileOnly("de.robv.android.xposed:api:82")
    // ❗作为 Xposed 模块使用务必添加，其它情况可选
    // ksp("com.highcapable.yukihookapi:ksp-xposed:1.1.8")
}