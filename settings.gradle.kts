pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()  // Add Google's Maven repository here
        mavenCentral() // You'll likely want this for other dependencies
    }
}

rootProject.name = "Augment-ED"
include(":app")
 