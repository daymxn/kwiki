package util

import java.io.File

private fun makeHeader(title: String, layout: String) =
    """
        ---
        title: $title
        layout: $layout
        ---
    """.trimIndent()

fun writeHeader(file: File, layoutFile: File) {
    val title = extractTitle(file)
    val layout = layoutFile.toRelativeString(file.parentFile.parentFile).backSlashToForwardSlash()
    file.prependTextIfNotPresent(makeHeader(title, layout))
}

fun generateSiteConfig(title: String, description: String) =
    """
        export const SITE = {
            title: "$title",
            description: "$description",
            defaultLanguage: "en_US",
        };
    """.trimIndent().newLine()

fun generateAstroIndexFile(base: String) =
    """
        <script is:inline>
        	window.location.pathname = "$base/en/overview";
        </script>
    """.trimIndent()

fun generateAstroRootConfig(website: String, base: String? = null) =
    """
        import { defineConfig } from 'astro/config';
        import preact from '@astrojs/preact';
        import react from '@astrojs/react';

        export default defineConfig({
        	integrations: [
        		preact(),
        		react(),
        	],
        	site: "$website",
            ${base?.let { "base: '$it'," }}
        	markdown: {
        		syntaxHighlight: 'prism'
        	}
        });
    """.trimIndent()

fun writeSidebar(file: File, sidebar: Sidebar) {
    file.appendText("$sidebar")
}

fun maybeAppendBaseToSidebarPageLinks(entries: List<SidebarEntry>, base: String?) = entries.mapIf(base != null) {
    it.runAs(SidebarPage::class) {
        copy(link = "${base!!.removeStartingSlashIfPresent()}/$link")
    }
}

/**
 * Removes width and height props, and sets them to what Astro expects them to be.
 * Also adds import for Astro props.
 */
fun File.generateFixedSVG(): String =
    readText().run {
        val regex = "(width|height) ?= ?([\\d.]+) (?![\\s\\S]*<svg)".toRegex()
        regex.replace(this, "")
    }.run {
        val regex = "(?<=<svg) ".toRegex()
        regex.replace(this, " width={size} height={size} ")
    }.run {
        """
            ---
            const { size } = Astro.props;
            ---
        """.trimIndent() + '\n' + this
    }
