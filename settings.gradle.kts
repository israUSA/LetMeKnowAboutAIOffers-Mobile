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

// Resuelve y descarga automáticamente el JDK que pide `gradle/gradle-daemon-jvm.properties`
// si no hay uno local. Evita hardcodear rutas de una máquina concreta en un repo público
// y hace que cada worktree compile sin configuración manual previa.
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "LetMeKnowAboutStudentOffers"
include(":app")
