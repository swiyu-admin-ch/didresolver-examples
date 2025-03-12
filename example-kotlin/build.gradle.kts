plugins {
    kotlin("jvm") version "1.9.23"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/swiyu-admin-ch/didresolver-kotlin")
    }
    maven {
        // CAUTION The maven repo of https://github.com/multiformats/java-multibase
        //         required (as dep) by ch.admin.bj.swiyu:didtoolbox:1.*.*
        name = "jitpack.io"
        url = uri("https://jitpack.io")
    }
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.mock-server:mockserver-netty-no-dependencies:5.14.0")
    implementation("ch.admin.eid:didresolver:2.0.0")
    //implementation(files("/Users/u80850818/Documents/GitHub/swiyu-admin-ch/didresolver-kotlin/build/libs/didresolver-kotlin-2.0.0.jar"))
    implementation("ch.admin.bj.swiyu:didtoolbox:1.0.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation(kotlin("stdlib-jdk8"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
