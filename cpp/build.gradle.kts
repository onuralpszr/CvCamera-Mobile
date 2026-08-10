plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.spotless)
}

android {
    namespace = "com.os.cvcamera.ndk"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.os.cvcamera.ndk"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get()

        externalNativeBuild {
            cmake {
                arguments(
                    "-DOpenCV_DIR=${rootProject.file("opencvsdk4140").absolutePath}/sdk/native/jni",
                    "-DANDROID_STL=c++_shared",
                )
                cppFlags("-std=c++17")
            }
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        buildConfig = true
    }

    packaging {
        jniLibs {
            pickFirsts += "**/libc++_shared.so"
        }
    }
}

spotless {
    kotlin {
        target("**/*.kt")
        ktlint()
    }
}

dependencies {
    implementation(project(":camera-ui"))
    implementation(libs.timber)
}
