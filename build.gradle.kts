plugins {
    kotlin("jvm") version "1.9.22"
}

group = "com.github.zimablue.devoutserver"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation("net.minestom:minestom-snapshots:b58db7d5b0")
}

tasks.test {
    useJUnitPlatform()
}