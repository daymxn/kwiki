package util

import java.io.File
import kotlin.reflect.KClass

fun File.findFirstMatchFrom(regex: Regex): String? =
    useLines {
        it.mapNotNull { regex.find(it)?.groupValues?.get(1) }.firstOrNull()
    }

/**
 * Is there a more optimal way to do this?
 */
fun File.prependTextIfNotPresent(text: String) {
    readText().takeIf { !it.startsWith(text) }?.let {
        writeText(text.newLine() + it)
    }
}

fun File.file(filePath: String) = File("$path/$filePath")

fun File.makeDirectory(filePath: String) = file(filePath).also { it.mkdirs() }

fun File.maybeFile(filePath: String) = file(filePath).takeIf { it.exists() }

fun File.getMarkdownFiles() = walkTopDown().filter { it.extension == "md" }

fun String.newLine() = plus('\n')

fun String.backSlashToForwardSlash() = replace('\\', '/')

fun String.removeStartingSlashIfPresent() = runIf(firstOrNull() == '\\' || firstOrNull() == '/') {
    drop(1)
}


inline fun <T> T.runIf(condition: Boolean, block: T.() -> T): T = if(condition) block() else this

inline fun <T> List<T>.mapIf(condition: Boolean, transform: (T) -> T): List<T> = if(condition) map(transform) else this

inline fun <reified R : Any, T> T.runAs(clazz: KClass<R>, block: R.() -> T): T = (this as? R)?.let(block) ?: this
