import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val snakeyamlVersion: String by project
val typesafeConfigVersion: String by project
val nightConfigVersion: String by project
val coreConversionVersion: String by project
val jlineVersion: String by project
val tinylogVersion: String by project
val jansiVersion: String by project
val reflexVersion: String by project
val asmVersion: String by project
val guavaVersion: String by project
val minestomVersion: String by project
val dependencyGetterVersion: String by project
val hikariCPVersion: String by project
val mysqlConnectorVersion: String by project
val nashornVersion: String by project
plugins {
    kotlin("jvm") version "2.1.10"
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
    // config
    implementation("org.yaml:snakeyaml:$snakeyamlVersion")
    compileOnly("com.typesafe:config:$typesafeConfigVersion")
    compileOnly("com.electronwill.night-config:core:$nightConfigVersion")
    compileOnly("com.electronwill.night-config:toml:$nightConfigVersion")
    compileOnly("com.electronwill.night-config:json:$nightConfigVersion")
    compileOnly("com.electronwill.night-config:hocon:$nightConfigVersion")
    compileOnly("com.electronwill.night-config:core-conversion:$coreConversionVersion")
    // terminal
    compileOnly("org.jline:jline-reader:$jlineVersion")
    compileOnly("org.jline:jline-terminal:$jlineVersion")
    compileOnly("org.jline:jline-terminal-jna:$jlineVersion")
    compileOnly("org.tinylog:tinylog-api:$tinylogVersion")
    compileOnly("org.tinylog:tinylog-impl:$tinylogVersion")
    compileOnly("org.tinylog:slf4j-tinylog:$tinylogVersion")
    compileOnly("org.fusesource.jansi:jansi:$jansiVersion")
    //reflex
    // 本体
    compileOnly("org.tabooproject.reflex:analyser:$reflexVersion")
    compileOnly("org.tabooproject.reflex:fast-instance-getter:$reflexVersion")
    compileOnly("org.tabooproject.reflex:reflex:$reflexVersion")
    // 本体依赖
    compileOnly("org.ow2.asm:asm:$asmVersion")
    compileOnly("org.ow2.asm:asm-util:$asmVersion")
    compileOnly("org.ow2.asm:asm-commons:$asmVersion")
    // minestom
    implementation("net.minestom:minestom-snapshots:$minestomVersion")
    implementation("com.github.Minestom:DependencyGetter:$dependencyGetterVersion")
    // database
    compileOnly("com.zaxxer:HikariCP:$hikariCPVersion")
    compileOnly("com.mysql:mysql-connector-j:$mysqlConnectorVersion")
    // nashorn
    compileOnly("org.openjdk.nashorn:nashorn-core:$nashornVersion")

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
tasks.named<ShadowJar>("shadowJar") {
    mergeServiceFiles()

}
tasks.processResources {
    filesMatching("dependencies.yml") {
        expand(project.properties)
    }
}