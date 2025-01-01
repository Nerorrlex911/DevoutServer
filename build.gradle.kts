val graalVersion: String by project

plugins {
    kotlin("jvm") version "1.9.22"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.github.zimablue.devoutserver"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven { url = uri("https://repo.tabooproject.org/repository/releases/") }
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
    maven { url = uri("https://repo.hypera.dev/snapshots") }

}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    //config
    compileOnly("org.yaml:snakeyaml:2.2")
    compileOnly("com.typesafe:config:1.4.3")
    compileOnly("com.electronwill.night-config:core:3.6.7")
    compileOnly("com.electronwill.night-config:toml:3.6.7")
    compileOnly("com.electronwill.night-config:json:3.6.7")
    compileOnly("com.electronwill.night-config:hocon:3.6.7")
    implementation("com.electronwill.night-config:core-conversion:6.0.0")
    //terminal
    implementation("org.jline:jline-reader:3.25.0")
    implementation("org.jline:jline-terminal:3.25.0")
    implementation("org.jline:jline-terminal-jna:3.25.0")
    implementation("org.tinylog:tinylog-api:2.7.0")
    implementation("org.tinylog:tinylog-impl:2.7.0")
    implementation("org.fusesource.jansi:jansi:2.4.1")
    //reflex
    // 本体
    implementation("org.tabooproject.reflex:analyser:1.0.23")
    implementation("org.tabooproject.reflex:fast-instance-getter:1.0.23")
    implementation("org.tabooproject.reflex:reflex:1.0.23") // 需要 analyser 模块
    // 本体依赖
    implementation("org.ow2.asm:asm:9.2")
    implementation("org.ow2.asm:asm-util:9.2")
    implementation("org.ow2.asm:asm-commons:9.2")
    // guava
    compileOnly("com.google.guava:guava:21.0")
    // minestom
    // https://mvnrepository.com/artifact/net.minestom/minestom-snapshots
    implementation("net.minestom:minestom-snapshots:698af959c8")
    implementation("com.github.Minestom:DependencyGetter:v1.0.1")
    implementation("dev.hollowcube:minestom-ce-extensions:1.2.0")
    // database
    implementation("com.zaxxer:HikariCP:4.0.3")
    // spark
    compileOnly("me.lucko:spark-api:0.1-SNAPSHOT")
    // luckperms
    implementation("me.lucko.luckperms:minestom:5.4-SNAPSHOT")
    //nashorn
    implementation("org.openjdk.nashorn:nashorn-core:15.4")

    implementation(fileTree("libs"))

}

tasks.test {
    useJUnitPlatform()
}
tasks.withType<Jar> {
    dependsOn(tasks.shadowJar)
    manifest {
        attributes["Main-Class"] = "com.github.zimablue.devoutserver.MainKt"
    }
}