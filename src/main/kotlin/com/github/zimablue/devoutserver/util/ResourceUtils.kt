package com.github.zimablue.devoutserver.util
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.util.Comparator
import java.util.stream.Stream

object ResourceUtils {
    @Throws(URISyntaxException::class, IOException::class)
    @JvmStatic
    fun extractResource(source: String) {
        val uri: URI = ResourceUtils::class.java.getResource("/$source")?.toURI() ?: throw IOException("Resource not found: $source")
        var fileSystem: FileSystem? = null

        // Only create a new filesystem if it's a jar file
        // (People can run this from their IDE too)
        if (uri.toString().startsWith("jar:")) {
            fileSystem = FileSystems.newFileSystem(uri, mapOf("create" to "true"))
        }

        try {
            val jarPath: Path = Paths.get(uri)
            val target: Path = Path.of(source)
            if (Files.exists(target)) {
                Files.walk(target).use { pathStream ->
                    pathStream.sorted(Comparator.reverseOrder())
                        .forEach { path ->
                            try {
                                Files.delete(path)
                            } catch (e: IOException) {
                                throw RuntimeException(e)
                            }
                        }
                }
            }
            Files.walkFileTree(jarPath, object : SimpleFileVisitor<Path>() {
                @Throws(IOException::class)
                override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                    val currentTarget: Path = target.resolve(jarPath.relativize(dir).toString())
                    Files.createDirectories(currentTarget)
                    return FileVisitResult.CONTINUE
                }

                @Throws(IOException::class)
                override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                    val to: Path = target.resolve(jarPath.relativize(file).toString())
                    Files.copy(file, to, StandardCopyOption.REPLACE_EXISTING)
                    return FileVisitResult.CONTINUE
                }
            })
        } finally {
            fileSystem?.close()
        }
    }
}