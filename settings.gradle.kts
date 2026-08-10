pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "cvCamera"
include(":app")
include(":opencvsdk4140")
project(":opencvsdk4140").projectDir = file("opencvsdk4140/sdk")

// UI, widgets and shared resources used by every example app.
include(":camera-ui")

// Language specific examples. The flagship app in :app is the Kotlin one.
include(":cpp")
include(":java")
