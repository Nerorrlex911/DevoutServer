package com.github.zimablue.devoutserver.util
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.net.URISyntaxException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.util.Comparator
import java.util.stream.Stream

object ResourceUtils {
    @Throws(URISyntaxException::class, IOException::class)
    @JvmStatic
    fun extractResource(source: String,targetDir: String=source,overwrite: Boolean=true) {
        val uri: URI = ResourceUtils::class.java.getResource("/$source")?.toURI() ?: throw IOException("Resource not found: $source")
        var fileSystem: FileSystem? = null

        // Only create a new filesystem if it's a jar file
        // (People can run this from their IDE too)
        if (uri.toString().startsWith("jar:")) {
            fileSystem = FileSystems.newFileSystem(uri, mapOf("create" to "true"))
        }

        try {
            val jarPath = Paths.get(uri)
            val target = Path.of(targetDir)
            // Delete existing files only if overwrite is true
            if (Files.exists(target) && overwrite) {
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
                    val currentTarget = target.resolve(jarPath.relativize(dir).toString())
                    Files.createDirectories(currentTarget)
                    return FileVisitResult.CONTINUE
                }

                @Throws(IOException::class)
                override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                    val to = target.resolve(jarPath.relativize(file).toString())
                    if(!Files.exists(to)) Files.copy(file, to)
                    return FileVisitResult.CONTINUE
                }
            })
        } finally {
            fileSystem?.close()
        }
    }
}