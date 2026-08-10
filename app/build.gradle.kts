import com.os.cvCamera.build.GitVersionValueSource

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.spotless)
}

val gitCommitHash = providers.of(GitVersionValueSource::class) {}.get()

@Suppress("UnstableApiUsage")
android {

    namespace = "com.os.cvCamera"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.os.cvCamera"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get()

        buildConfigField("String", "GIT_HASH", "\"$gitCommitHash\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val appKeystore = file("keystore.jks")
            // Also check root for local convenience if not in app
            val rootKeystore = rootProject.file("keystore.jks")

            if (appKeystore.exists()) {
                storeFile = appKeystore
                println("Using app keystore: ${appKeystore.absolutePath}")
            } else if (rootKeystore.exists()) {
                storeFile = rootKeystore
                println("Using root keystore: ${rootKeystore.absolutePath}")
            } else {
                // Default to app keystore so valid error message if missing
                storeFile = appKeystore
                println("Keystore not found, defaulting to: ${appKeystore.absolutePath}")
            }

            storePassword = System.getenv("SIGNING_STORE_PASSWORD")
            keyAlias = System.getenv("SIGNING_KEY_ALIAS")
            keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs {
            pickFirsts += "**/libc++_shared.so"
        }
    }

    lint {
        baseline = file("lint-baseline.xml")
        xmlOutput = file("lint-results.xml")
    }
}

spotless {
    kotlin {
        target("**/*.kt")
        ktlint()
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.material)
    implementation(libs.timber)
    implementation(libs.androidx.startup)
    implementation(libs.androidx.exifinterface)

    // Source - OpenCV-4 - Patched
    implementation(project(":opencvsdk4140"))

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.junit.ktx)
}
