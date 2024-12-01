// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}

tasks.register("dependencyReport") {
    doLast {
        println("Dependencies for " + project.name)
        configurations.forEach { config ->
            println("Configuration: " + config.name)
            config.resolvedConfiguration.resolvedArtifacts.forEach { artifact ->
                println("  ${artifact.moduleVersion.id}:${artifact.classifier ?: ""}")
            }
            println()
        }
    }
}
