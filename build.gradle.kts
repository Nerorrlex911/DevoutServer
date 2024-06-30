import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    id("org.jetbrains.kotlin.jvm") version "1.9.22" apply false
    id("com.github.johnrengelman.shadow") version "7.1.2" apply false
}

group = "com.github.zimablue.devoutserver"
version = "1.0-SNAPSHOT"

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "com.github.johnrengelman.shadow")
    apply(plugin = "maven-publish")

    repositories {
        maven("https://libraries.minecraft.net")
        maven("https://repo1.maven.org/maven2")
        maven("https://maven.aliyun.com/repository/central")
        maven("https://repo.codemc.io/repository/nms/")
        maven("https://repo.tabooproject.org/repository/releases/")
        maven("https://jitpack.io")
        mavenLocal()
        mavenCentral()
    }

    dependencies {
        compileOnly(kotlin("stdlib"))
        compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
        compileOnly("com.google.guava:guava:21.0")
        compileOnly("com.google.code.gson:gson:2.8.7")
        compileOnly("org.apache.commons:commons-lang3:3.5")
        compileOnly("org.tabooproject.reflex:reflex:1.0.19")
        compileOnly("org.tabooproject.reflex:analyser:1.0.19")

        //minestom
        // https://mvnrepository.com/artifact/net.minestom/minestom-snapshots
        implementation("net.minestom:minestom-snapshots:edb73f0a5a")
        implementation("com.github.Minestom:DependencyGetter:v1.0.1")
        implementation("dev.hollowcube:minestom-ce-extensions:1.2.0")
    }

    java {
        withSourcesJar()
    }

    tasks.build {
        dependsOn("shadowJar")
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(listOf("-XDenableSunApiLintControl"))
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "21"
            freeCompilerArgs = listOf("-Xjvm-default=all")
        }
    }

    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}



tasks.register<Jar>("uberJar") {
    from(subprojects.flatMap { it.tasks.matching { it is Jar } })
    archiveClassifier.set("DevoutServer")
}

tasks.test {
    useJUnitPlatform()
}