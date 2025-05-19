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
val sparkMinestomVersion: String by project
val luckpermsMinestomVersion: String by project
val nashornVersion: String by project
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
    // config
    implementation("org.yaml:snakeyaml:$snakeyamlVersion")
    implementation("com.typesafe:config:$typesafeConfigVersion")
    implementation("com.electronwill.night-config:core:$nightConfigVersion")
    implementation("com.electronwill.night-config:toml:$nightConfigVersion")
    implementation("com.electronwill.night-config:json:$nightConfigVersion")
    implementation("com.electronwill.night-config:hocon:$nightConfigVersion")
    implementation("com.electronwill.night-config:core-conversion:$coreConversionVersion")
    // terminal
    implementation("org.jline:jline-reader:$jlineVersion")
    implementation("org.jline:jline-terminal:$jlineVersion")
    implementation("org.jline:jline-terminal-jna:$jlineVersion")
    implementation("org.tinylog:tinylog-api:$tinylogVersion")
    implementation("org.tinylog:tinylog-impl:$tinylogVersion")
    implementation("org.tinylog:slf4j-tinylog:$tinylogVersion")
    implementation("org.fusesource.jansi:jansi:$jansiVersion")
    //reflex
    // 本体
    implementation("org.tabooproject.reflex:analyser:$reflexVersion")
    implementation("org.tabooproject.reflex:fast-instance-getter:$reflexVersion")
    implementation("org.tabooproject.reflex:reflex:$reflexVersion")
    // 本体依赖
    implementation("org.ow2.asm:asm:$asmVersion")
    implementation("org.ow2.asm:asm-util:$asmVersion")
    implementation("org.ow2.asm:asm-commons:$asmVersion")
    // minestom
    implementation("net.minestom:minestom-snapshots:$minestomVersion")
    implementation("com.github.Minestom:DependencyGetter:$dependencyGetterVersion")
    // database
    implementation("com.zaxxer:HikariCP:$hikariCPVersion")
    implementation("com.mysql:mysql-connector-j:$mysqlConnectorVersion")
    // spark
    implementation("dev.lu15:spark-minestom:$sparkMinestomVersion")
    // luckperms
    implementation("dev.lu15:luckperms-minestom:$luckpermsMinestomVersion")
    // nashorn
    implementation("org.openjdk.nashorn:nashorn-core:$nashornVersion")

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
tasks.processResources {
    filesMatching("dependencies.yml") {
        expand(project.properties)
    }
}