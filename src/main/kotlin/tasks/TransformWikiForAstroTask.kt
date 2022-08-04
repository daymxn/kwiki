package tasks

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import util.*
import java.io.File

/**
 * Transforms a wiki to the expected format for Astro.
 *
 * This task expects the docs to already be in the astro directory.
 * The reasoning for this, is we need to resolve relative layouts for each page's header.
 */
abstract class TransformWikiForAstroTask: TransformWikiForAstroTaskConfiguration() {
    private val astroDirectory by lazy { astroRootDirectory.get() }
    private val astroLayoutFile by lazy { astroDirectory.file(ASTRO_LAYOUT_PATH) }
    private val astroConfigFile by lazy { astroDirectory.file(ASTRO_CONFIG_PATH) }
    private val astroRootConfigFile by lazy { astroDirectory.file(ASTRO_ROOT_CONFIG_PATH) }
    private val astroIndexFile by lazy { astroDirectory.file(ASTRO_INDEX_PATH) }

    private val wikiDirectory by lazy { astroDirectory.file(WIKI_FILES_PATH) }
    private val wikiLogo by lazy { wikiDirectory.maybeFile("logo.svg") }
    private val wikiIcon by lazy { wikiDirectory.maybeFile("icon.ico") }

    @TaskAction
    fun apply() {
        replaceAstroLogo()
        replaceAstroIcon()
        replaceAstroConfig()
        replaceAstroRootConfig()
        replaceAstroIndexFile()
        appendAstroSidebar()
        appendAstroHeaderToWikiFiles()
    }

    fun replaceAstroLogo() {
        wikiLogo?.let {
            val currentAstroLogo = astroDirectory.file(ASTRO_LOGO_PATH)
            val fixedNewLogo = it.generateFixedSVG()

            currentAstroLogo.writeText(fixedNewLogo)
        }
    }

    fun replaceAstroIcon() {
        wikiIcon?.let {
            astroDirectory.file(ASTRO_ICON_PATH).writeBytes(it.readBytes())
        }
    }

    fun appendAstroHeaderToWikiFiles() {
        wikiDirectory.getMarkdownFiles().forEach {
            writeHeader(it, astroLayoutFile)
        }
    }

    fun replaceAstroIndexFile() {
        wikiWebsiteBase.orNull?.let {
            astroIndexFile.writeText(generateAstroIndexFile(it))
        }
    }

    fun replaceAstroConfig() {
        val newSiteConfig = generateSiteConfig(wikiTitle.get(), wikiDescription.get())

        astroConfigFile.writeText(newSiteConfig)
    }

    fun replaceAstroRootConfig() {
        val newRootConfig = generateAstroRootConfig(wikiWebsite.get(), wikiWebsiteBase.orNull)

        astroRootConfigFile.writeText(newRootConfig)
    }

    fun appendAstroSidebar() {
        val sidebarEntries = convertWikiFilesToSidebarEntries(wikiDirectory).run {
            maybeAppendBaseToSidebarPageLinks(this, wikiWebsiteBase.orNull)
        }
        val sidebar = Sidebar(sidebarEntries)

        writeSidebar(astroConfigFile, sidebar)
    }
}

abstract class TransformWikiForAstroTaskConfiguration: DefaultTask() {
    @get:InputDirectory
    abstract val astroRootDirectory: Property<File>

    @get:Input
    abstract val wikiTitle: Property<String>

    @get:Input
    abstract val wikiDescription: Property<String>

    @get:Input
    abstract val wikiWebsite: Property<String>

    @get:Optional
    @get:Input
    abstract val wikiWebsiteBase: Property<String>
}
