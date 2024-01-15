plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.football.football"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.football.football"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("androidx.activity:activity-ktx:1.4.0")

    implementation("com.squareup.okhttp3:okhttp:4.11.0")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")

    implementation("com.google.code.gson:gson:2.8.8")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")

    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-android-compiler:2.48")
    kapt("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.5.0")

    implementation("com.github.kirich1409:viewbindingpropertydelegate-noreflection:1.5.3")

    implementation("com.github.delight-im:Android-AdvancedWebView:v3.2.1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.9")

    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")

    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-analytics-ktx:21.5.0")
    implementation("com.google.firebase:firebase-messaging:23.4.0")

    implementation("com.google.android.gms:play-services-ads-identifier:17.0.0")
    implementation("com.google.android.gms:play-services-base:17.6.0")

    implementation("com.android.installreferrer:installreferrer:2.2")
}