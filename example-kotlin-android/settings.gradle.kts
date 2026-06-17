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
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            // CAUTION The maven repo of https://github.com/multiformats/java-multibase
            //         required (as dep) by ch.admin.bj.swiyu:didtoolbox:1.*.*
            name = "jitpack.io"
            url = uri("https://jitpack.io")
        }
    }
}

rootProject.name = "DidResolverDemoApp"
include(":app")
 