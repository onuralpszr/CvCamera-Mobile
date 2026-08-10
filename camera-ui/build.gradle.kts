plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.spotless)
}

android {
    namespace = "com.os.cvcamera.ui"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        viewBinding = true
    }
}

spotless {
    kotlin {
        target("**/*.kt")
        ktlint()
    }
}

dependencies {
    // api so the example apps see OpenCV and the widgets without repeating the dependency.
    api(project(":opencvsdk4140"))
    api(libs.material)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.exifinterface)
    implementation(libs.timber)

    testImplementation(libs.junit)
}
