package util

import java.io.File


fun convertWikiFilesToSidebarEntries(wikiFiles: File): List<SidebarEntry> {
    return wikiFiles.walkTopDown().filter { it.isDirectory || it.extension == "md" }.map {
        if(it.isDirectory) {
            SidebarHeader(it.nameWithoutExtension)
        } else {
            val path = it.toRelativeString(wikiFiles.parentFile).backSlashToForwardSlash()

            SidebarPage(extractTitle(it), path.removeSuffix(".md"), extractOrder(it))
        }
    }.drop(1).sortedBy { it.order }.toList()
}

data class Sidebar(val pages: List<SidebarEntry>) {
    override fun toString() =
        """
            export const SIDEBAR = { 
                en: [
                    ${pages.joinToString("\n                    ") /* lol idk a better way to make it pretty */}
                ]
            };
        """.trimIndent()
}

sealed class SidebarEntry {
    abstract val order: Int
}

data class SidebarHeader(val text: String, override val order: Int = 0): SidebarEntry() {
    override fun toString() = "{ text: '$text', header: true },"
}

data class SidebarPage(val text: String, val link: String, override val order: Int): SidebarEntry() {
    override fun toString() = "{ text: '$text', link: '$link' },"
}
