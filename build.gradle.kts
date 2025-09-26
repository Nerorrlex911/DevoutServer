import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "2.1.10"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("maven-publish")
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

}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    implementation(libs.kotlinx.coroutines.core)
    implementation(kotlin("reflect"))

    // config
    implementation(libs.snakeyaml)
    compileOnly(libs.typesafeConfig)
    compileOnly(libs.nightConfig.core)
    compileOnly(libs.nightConfig.toml)
    compileOnly(libs.nightConfig.json)
    compileOnly(libs.nightConfig.hocon)
    compileOnly(libs.nightConfig.coreConversion)
    // terminal
    implementation(libs.jline.reader)
    implementation(libs.jline.terminal)
    implementation(libs.jline.terminalJna)
    implementation(libs.tinylog.api)
    implementation(libs.tinylog.impl)
    implementation(libs.tinylog.slf4j)
    implementation(libs.jansi)
    // reflex
    compileOnly(libs.reflex.analyser)
    compileOnly(libs.reflex.fastInstanceGetter)
    compileOnly(libs.reflex.core)
    compileOnly(libs.asm.core)
    compileOnly(libs.asm.util)
    compileOnly(libs.asm.commons)
    // minestom
    implementation(libs.minestom)
    implementation(libs.dependencyGetter)
    // command framework
    implementation(libs.lamp.common)
    implementation(libs.lamp.minestom)
    // database
    compileOnly(libs.hikariCP)
    // nashorn
    compileOnly(libs.nashorn)
    // guava
    compileOnly(libs.guava)
    // bytebuddy
    implementation(libs.byteBuddy)
    implementation(libs.byteBuddyAgent)
    // luckperms 无法联网下载，只能打包起来了
    implementation(libs.luckperms)
    implementation(libs.enhancedlegacytext)


    implementation(fileTree("libs"))

}

tasks.withType<JavaCompile> {
    // Preserve parameter names in the bytecode
    options.compilerArgs.add("-parameters")
}
kotlin {
    compilerOptions {
        javaParameters = true
    }
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
    archiveFileName.set("${project.name}-${project.version}.jar")
}

tasks.processResources {
    filesMatching("dependencies.yml") {
        fun toDependencyStr(dependency: MinimalExternalModuleDependency) : String {
            return "${dependency.module}:${dependency.versionConstraint.displayName}"
        }
        //expand(project.properties)
        val dependenciesStr =
            """
            repositories:
              - "https://repo.tabooproject.org/repository/releases/"
              - "https://oss.sonatype.org/content/repositories/snapshots"
              - "https://repo.hypera.dev/snapshots"
              - "https://repo.lucko.me/"
            dependencies:
              - "${toDependencyStr(libs.typesafeConfig.get())}"
              - "${toDependencyStr(libs.nightConfig.core.get())}"
              - "${toDependencyStr(libs.nightConfig.toml.get())}"
              - "${toDependencyStr(libs.nightConfig.json.get())}"
              - "${toDependencyStr(libs.nightConfig.hocon.get())}"
              - "${toDependencyStr(libs.nightConfig.coreConversion.get())}"
              - "${toDependencyStr(libs.reflex.analyser.get())}"
              - "${toDependencyStr(libs.reflex.fastInstanceGetter.get())}"
              - "${toDependencyStr(libs.reflex.core.get())}"
              - "${toDependencyStr(libs.asm.core.get())}"
              - "${toDependencyStr(libs.asm.util.get())}"
              - "${toDependencyStr(libs.asm.commons.get())}"
              - "${toDependencyStr(libs.hikariCP.get())}"
              - "${toDependencyStr(libs.nashorn.get())}"
              - "${toDependencyStr(libs.guava.get())}"
              - "com.mysql:mysql-connector-j:9.4.0"
              - "org.xerial:sqlite-jdbc:3.50.3.0"
            """
                .trimIndent()
        this.file.writeText(dependenciesStr)
    }
}
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            artifact(tasks.named("shadowJar"))
        }
    }
}