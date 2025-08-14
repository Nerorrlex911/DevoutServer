package com.github.zimablue.devoutserver.server

import com.github.zimablue.devoutserver.util.ResourceUtils
import net.bytebuddy.agent.ByteBuddyAgent
import net.minestom.dependencies.DependencyGetter
import net.minestom.dependencies.ResolvedDependency
import net.minestom.dependencies.maven.MavenRepository
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.IOException
import java.util.jar.JarFile

object ServerDependencyManager {
    init {
        ByteBuddyAgent.install()
        ResourceUtils.extractResource("dependencies.yml")
    }

//    private val systemClassloader by lazy {
//        ClassLoader.getSystemClassLoader() as? URLClassLoader ?: throw IllegalStateException("System class loader is not a URLClassLoader")
//    }
//
//    private val addUrlMethod by lazy {
//        URLClassLoader::class.java.getDeclaredMethod("addURL", URL::class.java).apply {
//            isAccessible = true
//        }
//    }


    private fun addURLs(resolvedDependencies: List<ResolvedDependency>) {
        for (dependency in resolvedDependencies) {
            addJarToClasspath(File(dependency.contentsLocation.file))
            if (dependency.subdependencies.isNotEmpty()) {
                addURLs(dependency.subdependencies)
            }
        }
    }

    fun loadDependencies() {
        val dependencyGetter = DependencyGetter()
        val yaml = Yaml()
        val dependencies = yaml.loadAs(File("dependencies.yml").reader(), Dependencies::class.java)
        dependencyGetter.addMavenResolver(dependencies.repositories.map {
            MavenRepository("Repo",it)
        } + listOf(
            MavenRepository.Central,
            MavenRepository.Jitpack
        ))

        val dependenciesFolder = File("dependencies")
        if (!dependenciesFolder.exists()) {
            dependenciesFolder.mkdirs()
        }

        val resolvedDependencies = dependencies.dependencies.map { dependency -> dependencyGetter.get(dependency,dependenciesFolder) }
        addURLs(resolvedDependencies)
    }
    class Dependencies {
        var repositories: List<String> = mutableListOf()
        var dependencies: List<String> = mutableListOf()
    }

    private fun addJarToClasspath(file: File) {
        val instrumentation = ByteBuddyAgent.getInstrumentation()
        try {
            instrumentation.appendToSystemClassLoaderSearch(JarFile(file.canonicalPath))
        } catch (e: IOException) {
            e.printStackTrace();
        }
    }



}