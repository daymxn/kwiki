package core

import com.github.gradle.node.npm.task.NpmTask
import com.github.junrar.Junrar
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Optional
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType
import tasks.TransformWikiForAstroTask
import util.*
import util.ASTRO_BUILD_PATH
import util.WIKI_FILES_PATH
import java.io.File

abstract class kWikiPlugin: Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply("com.github.node-gradle.node")

        val extractAstroFiles = extractAstroFilesTask(project)

        with(project) {
            val extension = extensions.create<kWikiPluginExtension>("kWiki")

            val astroInstall = tasks.register<NpmTask>("astroInstall") {

                args.add("install")
            }

            val astroBuild = tasks.register<NpmTask>("astroBuild") {
                waitUntil(astroInstall)

                npmCommand.set(listOf("run", "build"))
            }

            tasks.withType<NpmTask>().configureEach {
                workingDir.set(astroDirectory)
                ignoreExitValue.set(false)
                execOverrides {
                    standardOutput = System.out
                }
            }

            val copyWikiToAstro = tasks.register<Copy>("copyWikiToAstro") {
                waitUntil(extractAstroFiles)

                from(extension.wikiSourceDirectory)
                into(astroDirectory.file(WIKI_FILES_PATH))
            }

            val transformWikiForAstro = tasks.register<TransformWikiForAstroTask>("transformWikiForAstro") {
                waitUntil(copyWikiToAstro)

                wikiTitle.set(extension.wikiTitle)
                wikiDescription.set(extension.wikiDescription)
                astroRootDirectory.set(project.astroDirectory)
                wikiWebsite.set(extension.wikiWebsite)
                wikiWebsiteBase.set(extension.wikiWebsiteBase)
            }

            val moveWikiBuildToOutputDirectory = tasks.register<Copy>("moveWikiBuildToOutputDirectory") {
                from(astroDirectory.file(ASTRO_BUILD_PATH))
                into(extension.outputDirectory.get())
            }

            tasks.register("buildWiki") {
                group = GRADLE_GROUP
                runInOrder(
                        transformWikiForAstro,
                        astroBuild,
                        moveWikiBuildToOutputDirectory
                )
            }
        }
    }

    private fun extractAstroFilesTask(project: Project) = project.tasks.register("extractAstroFiles") {
        val astroRar = project.buildscript.classLoader.getResourceAsStream(ASTRO_COMPRESSED_PATH) ?: throw RuntimeException("Missing astro files")

        Junrar.extract(astroRar, project.astroDirectory)
    }

    fun Project.kWiki(block: kWikiPluginExtension.() -> Unit) {
        the<kWikiPluginExtension>().apply(block)
    }

    private val Project.astroDirectory: File
        get() = buildDir.makeDirectory(ASTRO_EXTRACTED_DIRECTORY)
}

abstract class kWikiPluginExtension {
    abstract val wikiSourceDirectory: Property<File>
    abstract val wikiTitle: Property<String>
    abstract val wikiDescription: Property<String>
    abstract val outputDirectory: Property<File>
    abstract val wikiWebsite: Property<String>

    @get:Optional
    abstract val wikiWebsiteBase: Property<String>

    init {
        wikiTitle.convention("Wiki")
        wikiDescription.convention("")
    }
}
