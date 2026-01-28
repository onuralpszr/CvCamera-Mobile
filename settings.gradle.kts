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
include(":opencvsdk4130")
project(":opencvsdk4130").projectDir = file("opencvsdk4130/sdk")
