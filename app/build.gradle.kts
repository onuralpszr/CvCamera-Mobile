plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.spotless)
}

android {

    abstract class GitVersionValueSource : ValueSource<String, ValueSourceParameters.None> {
        @get:Inject
        abstract val execOperations: ExecOperations

        override fun obtain(): String {
            val output = ByteArrayOutputStream()
            return try {
                execOperations.exec {
                    commandLine("git", "rev-parse", "--verify", "--short", "HEAD")
                    standardOutput = output
                }
                String(output.toByteArray()).trim()
            } catch (e: Exception) {
                "unknown"
            }
        }
    }
    namespace = "com.os.cvCamera"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.os.cvCamera"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.1.0"

        val gitCommitHash = providers.of(GitVersionValueSource::class) {}.get()
        buildConfigField("String", "GIT_HASH", "\"$gitCommitHash\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        externalNativeBuild {
            cmake {
                 arguments(
                    "-DOpenCV_DIR=${file("../opencvsdk4130").absolutePath}/sdk/native/jni",
                    "-DANDROID_TOOLCHAIN=clang",
                    "-DANDROID_STL=c++_shared"
                )
                cppFlags("")
            }
        }
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

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
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

    // Source - OpenCV-4 - Patched
    implementation(project(":opencvsdk4130"))

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.junit.ktx)
}
