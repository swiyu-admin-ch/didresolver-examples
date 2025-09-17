plugins {
    kotlin("jvm") version "1.9.23"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
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
    implementation("io.github.swiyu-admin-ch:didresolver:2.2.0")
    implementation("io.github.swiyu-admin-ch:didtoolbox:1.6.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation(kotlin("stdlib-jdk8"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
