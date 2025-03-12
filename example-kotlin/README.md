# Example Kotlin

## Resolve a [`TDW`](https://identity.foundation/didwebvh/v0.3) DID document

This project contains examples for different approaches to resolve a [`TDW`](https://identity.foundation/didwebvh/v0.3) DID document:
- [User provided HTTP client](/src/main/kotlin/Main.kt) -> uses an HTTP client to fetch a DID log and then resolves it using the library

## Project integration

Accommodate the `build.gradle.kts` file to be able to import the required dependencies
```kotlin
// file: build.gradle.kts
repositories {
    mavenCentral()
    maven {
        name = "DidResolverGitHubPackages"
        url = uri("https://maven.pkg.github.com/e-id-admin/didresolver-kotlin")
    }
    maven {
        // CAUTION The maven repo of https://github.com/multiformats/java-multibase
        //         required (as dep) by ch.admin.bj.swiyu:didtoolbox:1.*.*
        name = "jitpack.io"
        url = uri("https://jitpack.io")
    }
}
```

For current/latest versions of the required dependencies, just rely on the relevant [`didresolver` GitHub packages](https://github.com/swiyu-admin-ch/didresolver-kotlin/packages/2242843)
```kotlin
// file: build.gradle.kts
dependencies {
    // Other dependencies go here
    implementation("ch.admin.eid:didresolver:{latest version as seen in repository}")
}
```

Needles to say, before running the `./gradlew clean build` command, it is assumed that you are already familiar with [Gradle](https://docs.github.com/articles/configuring-gradle-for-use-with-github-package-registry).
