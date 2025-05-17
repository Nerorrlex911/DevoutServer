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
    maven { url = uri("https://repo.lucko.me/") } // spark-common
    maven { url = uri("https://mvnrepository.com/artifact/com.mysql/mysql-connector-j")}

}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    //config
    implementation("org.yaml:snakeyaml:2.2")
    implementation("com.typesafe:config:1.4.3")
    implementation("com.electronwill.night-config:core:3.6.7")
    implementation("com.electronwill.night-config:toml:3.6.7")
    implementation("com.electronwill.night-config:json:3.6.7")
    implementation("com.electronwill.night-config:hocon:3.6.7")
    implementation("com.electronwill.night-config:core-conversion:6.0.0")
    //terminal
    implementation("org.jline:jline-reader:3.25.0")
    implementation("org.jline:jline-terminal:3.25.0")
    implementation("org.jline:jline-terminal-jna:3.25.0")
    implementation("org.tinylog:tinylog-api:2.7.0")
    implementation("org.tinylog:tinylog-impl:2.7.0")
    implementation("org.tinylog:slf4j-tinylog:2.7.0")
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
    implementation("com.google.guava:guava:21.0")
    // minestom
    // https://mvnrepository.com/artifact/net.minestom/minestom-snapshots
    implementation("net.minestom:minestom-snapshots:1_21_5-c5b715aa82")
    implementation("com.github.Minestom:DependencyGetter:v1.0.1")
    // database
    implementation("com.zaxxer:HikariCP:4.0.3")
    implementation("com.mysql:mysql-connector-j:9.3.0")
    // spark
    implementation("dev.lu15:spark-minestom:1.10-SNAPSHOT")
    // luckperms
    implementation("dev.lu15:luckperms-minestom:5.4-SNAPSHOT")
    //nashorn
    implementation("org.openjdk.nashorn:nashorn-core:15.4")

    implementation(fileTree("libs"))

}

tasks.test {
    useJUnitPlatform()
    systemProperty("user.dir",file("test").absolutePath)
}
tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "com.github.zimablue.devoutserver.MainKt"
    }
}