plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-kapt")
}

android {
    namespace = "com.pdf.pdfmaker"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.pdf.pdfmaker"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Enable ViewBinding
    buildFeatures {
        viewBinding = true
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
}

dependencies {

    // AndroidX Core & UI Components
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation("androidx.recyclerview:recyclerview:1.2.1")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // PDF Libraries
    implementation("com.itextpdf:itext7-core:7.1.15")
    implementation("com.github.barteksc:android-pdf-viewer:2.8.2")

    // Floating Action Button
    implementation("com.github.clans:fab:1.6.4")

    // CameraX for capturing images
    implementation("androidx.camera:camera-core:1.3.0")
    implementation("androidx.camera:camera-camera2:1.3.0")
    implementation("androidx.camera:camera-lifecycle:1.3.0")
    implementation("androidx.camera:camera-view:1.3.0")

    // Glide for image loading
    implementation("com.github.bumptech.glide:glide:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0") // Fixed annotation processor

    // PhotoView (For Zoomable Image Preview)
    implementation("com.github.chrisbanes:PhotoView:2.3.0")

    // UCrop (For Image Cropping)
    implementation("com.github.yalantis:ucrop:2.2.6")

    // PhotoEditor for image editing
    implementation("ja.burhanrashid52:photoeditor:1.1.0")

    // Android-Image-Cropper for cropping
    implementation("com.theartofdev.edmodo:android-image-cropper:2.8.0")

    // GPUImage for filters
    implementation("jp.co.cyberagent.android:gpuimage:2.1.0")

    // Room Database (Fixed)
    implementation("androidx.room:room-runtime:2.6.1")
    //noinspection KaptUsageInsteadOfKsp
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

    // pdf box
//    implementation("org.apache.pdfbox:pdfbox:2.0.27")

    implementation("com.itextpdf:itext7-core:7.1.15")

    // image picker
    implementation("com.github.dhaval2404:imagepicker:2.1")

    implementation("androidx.compose.ui:ui-graphics:1.0.0")

    implementation ("androidx.appcompat:appcompat:1.6.1")
    implementation ("androidx.recyclerview:recyclerview:1.3.1")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    implementation ("androidx.core:core-ktx:1.10.1")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")

}
