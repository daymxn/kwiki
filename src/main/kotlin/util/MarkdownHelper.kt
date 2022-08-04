package util

import java.io.File

fun extractTitle(file: File): String {
    val regex = "\\[//]: # \\(title: ([^)]+)".toRegex()
    return file.findFirstMatchFrom(regex) ?: throw RuntimeException("File is missing title: ${file.path}")
}

fun extractOrder(file: File): Int {
    val regex = "\\[//]: # \\(order: ([^)]+)".toRegex()
    return file.findFirstMatchFrom(regex)?.toIntOrNull() ?: throw RuntimeException("File is missing order: ${file.path}")
}
