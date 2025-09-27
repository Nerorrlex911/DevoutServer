package com.github.zimablue.devoutserver.util
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.net.URL
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.util.Comparator
import kotlin.io.path.absolute

object ResourceUtils {
    /**
     * Extracts a resource from the classpath to a target directory.
     * If the resource is inside a JAR, it handles the extraction accordingly.
     * @param source The resource path in the classpath (e.g., "config/defaults.yml")
     * @param targetDir The target directory to extract to (default is the same as source)
     * @param overwrite Whether to overwrite existing files in the target directory
     * @throws URISyntaxException If the resource URI is malformed
     * @throws IOException If an I/O error occurs during extraction
     */
    @Throws(URISyntaxException::class, IOException::class)
    @JvmStatic
    fun extractResource(source: String,targetDir: String=source,overwrite: Boolean=false) {
        val url = ResourceUtils::class.java.getResource("/$source")?.toURI()?: throw IOException("Resource not found: $source")
        extractResource(url,Path.of(targetDir),overwrite)
    }

    /**
     * Extracts a resource from the classpath to a target directory.
     * If the resource is inside a JAR, it handles the extraction accordingly.
     * @param url The resource URL
     * @param targetDir The target directory to extract to.
     * @param overwrite Whether to overwrite existing files in the target directory
     * @throws URISyntaxException If the resource URI is malformed
     * @throws IOException If an I/O error occurs during extraction
     */
    @Throws(URISyntaxException::class, IOException::class)
    @JvmStatic
    fun extractResource(uri: URI,targetDir: Path,overwrite: Boolean=false) {
        var fileSystem: FileSystem? = null

        // Only create a new filesystem if it's a jar file
        // (People can run this from their IDE too)
        if (uri.toString().startsWith("jar:")) {
            fileSystem = FileSystems.newFileSystem(uri, mapOf("create" to "true"))
        }

        try {
            val jarPath = Paths.get(uri)
            // Delete existing files only if overwrite is true
            Files.walkFileTree(jarPath, object : SimpleFileVisitor<Path>() {
                @Throws(IOException::class)
                override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                    val currentTarget = targetDir.resolve(jarPath.relativize(dir).toString())
                    Files.createDirectories(currentTarget)
                    return FileVisitResult.CONTINUE
                }

                @Throws(IOException::class)
                override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                    val to = targetDir.resolve(jarPath.relativize(file).toString())
                    if (!Files.exists(to)) {
                        Files.copy(file, to)
                    } else if(overwrite) {
                        Files.copy(file, to, StandardCopyOption.REPLACE_EXISTING)
                    }
                    return FileVisitResult.CONTINUE
                }
            })
        } finally {
            fileSystem?.close()
        }
    }
}