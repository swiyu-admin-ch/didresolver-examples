# Example Kotlin-Android

## Resolve a [`TDW`](https://identity.foundation/didwebvh/v0.3) or [`WEBVH`](https://identity.foundation/didwebvh/v1.0/) Document

This project contains an example for a simple android app, that resolves a [`TDW`](https://identity.foundation/didwebvh/v0.3) or [`WEBVH`](https://identity.foundation/didwebvh/v1.0/) DID Document.

## Project Integration

Adapt the `settings.gradle.kts` file to be able to import the required dependencies
```kotlin
// file: settings.gradle.kts
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
```

Add the [`didresolver`](https://repo1.maven.org/maven2/ch/admin/swiyu/didresolver/didresolver-android/) and [`jna`](https://repo1.maven.org/maven2/net/java/dev/jna/jna/) to your dependencies.
```kotlin
// file: app/build.gradle.kts
dependencies {
    implementation("ch.admin.swiyu.didresolver:didresolver-android:2.8.2@aar")
    implementation("net.java.dev.jna:jna:5.17.0@aar")
    // Other dependencies
}
```

Needles to say, before running the `./gradlew clean build` command, it is assumed that you are already familiar with [Gradle](https://docs.github.com/articles/configuring-gradle-for-use-with-github-package-registry).
