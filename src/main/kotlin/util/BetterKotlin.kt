package util

import java.io.File
import java.io.InputStream
import kotlin.io.path.createTempFile
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.configurationcache.extensions.capitalized
import kotlin.io.path.outputStream

/** TODO() */
fun Project.provideResource(resource: String): Provider<InputStream> = provider {
  resource(resource)
}

/** TODO() */
fun Project.resource(resource: String): InputStream =
  buildscript.classLoader.getResourceAsStream(resource)

/**
 * TODO()
 */
fun InputStream.toFile(): File = use {
  createTempFile("InputStream").toFile().from(it)
}

/**
 * TODO()
 */
fun File.from(stream: InputStream): File = also {
  outputStream().use {
    stream.transferTo(it)
  }
}

/** TODO() */
fun File.file(filePath: String) = File("$path/$filePath")

/** TODO() */
fun String.toPascalCase() =
  split(" ").filterNot { it.isBlank() }.joinToString { it.capitalized() }.remove("-")

/** TODO() */
fun String.remove(str: String) = replace(str, "")
