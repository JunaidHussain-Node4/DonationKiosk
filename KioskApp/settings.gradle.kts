pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // SumUp SDK repository
        maven { url = uri("https://maven.sumup.com/releases") }
    }
}

rootProject.name = "KioskDonationApp"
include(":app")
