package core

import com.github.gradle.node.NodePlugin
import com.github.gradle.node.npm.task.NpmTask
import java.io.File
import kotlinx.serialization.json.JsonNull
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Copy
import org.gradle.kotlin.dsl.*
import tasks.*
import tasks.Sidebar
import util.*

/**
 * TODO()
 */
abstract class kWikiPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    with(project) {
      configureWikiExtension()
      apply<NodePlugin>()

      val prepareWiki = registerPrepareWikiTask()
      val buildWiki = registerBuildWikiTask(prepareWiki)

      tasks.register("kWiki") {
        group = GRADLE_GROUP
        description = GRADLE_DESCRIPTION

        dependsOn(prepareWiki, buildWiki)
      }
    }
  }
}

private fun Project.registerPrepareWikiTask() = kWiki {
  val makeTheme = registerMakeThemeFileTask()
  val extractPresetFiles = registerExtractPresetFilesTask()

  tasks.register<Copy>("prepareWiki") {
    into(temporaryDir)
    from(extractPresetFiles)
    from(makeTheme)
    from(theme.collectImageFiles())

    into(PUBLIC_PATH) {
      from(theme.icon.orNull).rename { "favicon.ico" }
    }

    into(PAGES_PATH) {
      from(sourceDirectory)
    }
  }
}

private fun Project.registerBuildWikiTask(preparedWiki: Provider<Copy>) = kWiki {
  val preparedWikiFiles = preparedWiki.map { it.destinationDir }

  tasks.register<NpmTask>("buildWiki") {
    group = "other"
    mustRunAfter(preparedWiki)

    npmCommand.set(listOf("run", "make"))
    workingDir.fileProvider(preparedWikiFiles)
    ignoreExitValue.set(false)

    logging.captureStandardOutput(LogLevel.DEBUG)
    logging.captureStandardError(LogLevel.DEBUG)

    doLast {
      copy {
        from(preparedWikiFiles.map { it.file(EXPORT_PATH) })
        into(outputDirectory)
      }
    }
  }
}

private fun Project.configureWikiExtension() =
  extensions.create<kWikiPluginExtension>("kWiki").apply {
    sourceDirectory.convention(file("docs"))
  }

private fun Project.registerMakeThemeFileTask() =
  tasks.register<CreateThemeFileTask>("makeThemeFile") {
    val theme = kWiki { theme }
    options.set(theme.convert().toJsonString())
    imports.set(theme.collectImports())
    outputFile.set(temporaryDir.file(THEME_FILE_NAME))
  }

private fun Project.registerExtractPresetFilesTask() =
  tasks.register<Copy>("extractPresetFiles") {
    val presetFiles = provideResource(NEXTRA_COMPRESSED_PATH).map { it.toFile() }

    from(zipTree(presetFiles))
    into(temporaryDir)
  }

private fun <T> Project.kWiki(block: kWikiPluginExtension.() -> T): T =
  the<kWikiPluginExtension>().run(block)

/**
 * TODO()
 */
abstract class kWikiPluginExtension : ExtensionAware {
  abstract val sourceDirectory: Property<File>
  abstract val outputDirectory: Property<File>

  internal val theme = link<ThemeOptions>("theme")
}

/**
 * TODO()
 */
internal interface ExtensionConfiguration : ExtensionAware {
  fun defaults()
}

/**
 * TODO()
 */
internal inline fun <reified T : ExtensionConfiguration> ExtensionAware.link(name: String) =
  extensions.create<T>(name).apply { defaults() }

/**
 * Provides configuration points for customizing how the generated wiki looks.
 *
 * @property icon favicon to be used for the webpages.
 * @property githubDocumentation A link to the wiki on GitHub.
 * @property darkModeToggle Allow users to toggle between Dark Mode + Light Mode.
 * @property feedbackLink Show or hide the feedback link on the right side of pages.
 * @property lastUpdatedDate Show or hide the last updated date at the bottom of pages.
 * @property syntaxHighlight Whether to enable syntax highlighting or not.
 */
abstract class ThemeOptions : ExtensionConfiguration {
  abstract val icon: Property<File>
  abstract val githubDocumentation: Property<String>
  abstract val darkModeToggle: Property<Boolean>
  abstract val feedbackLink: Property<Boolean>
  abstract val lastUpdatedDate: Property<Boolean>
  abstract val syntaxHighlight: Property<Boolean>

  internal val color = link<ThemeColorOptions>("color")
  internal val navbar = link<NavbarOptions>("navbar")
  internal val sidebar = link<SidebarOptions>("sidebar")
  internal val bottomNavigation = link<BottomNavigationOptions>("bottomNavigation")
  internal val footer = link<FooterOptions>("footer")

  override fun defaults() {
    githubDocumentation.convention("https://github.com/daymxn/kWiki")
    darkModeToggle.convention(true)
    feedbackLink.convention(true)
    lastUpdatedDate.convention(true)
    syntaxHighlight.convention(true)
  }

  internal fun collectImageFiles() =
    listOfNotNull(
      navbar.logo.orNull,
      navbar.projectIcon.icon.orNull,
      navbar.chatIcon.icon.orNull,
    )

  internal fun collectImports() =
    listOfNotNull(
      navbar.logo.orNull?.nameWithoutExtension ?: "logo",
      navbar.projectIcon.icon.orNull?.nameWithoutExtension,
      navbar.chatIcon.icon.orNull?.nameWithoutExtension,
    )

  internal fun convert() =
    Theme(
      githubDocumentation.get(),
      darkModeToggle.get(),
      color.convert(),
      ReactNode.fromImageOrNull(navbar.logo.orNull, 90)
        ?: ReactNode.create("<Logo width={90} height={90} />"),
      navbar.projectIcon.convert(),
      navbar.chatIcon.convert(),
      navbar.search.convert(),
      navbar.banner.takeIf { it.text.isPresent }?.convert(),
      sidebar.convert(),
      bottomNavigation.convert(),
      ReactNode.empty.takeUnless { lastUpdatedDate.get() },
      footer.convert(),
      Feedback(JsonNull).takeUnless { feedbackLink.get() }
    )
}

/**
 * Configuration for the footer element shown at the bottom of every page.
 *
 * @property enabled the visibility of the footer
 * @property text additional text to place in the footer
 */
abstract class FooterOptions : ExtensionConfiguration {
  abstract val enabled: Property<Boolean>
  abstract val text: Property<String>

  override fun defaults() {
    enabled.convention(true)
    text.convention("Made with love - 2023")
  }

  internal fun convert() =
    Footer(text = text.get(), component = EmptyReactNode.takeUnless { enabled.get() })
}

/**
 * Additional elements at the end of pages for further navigation.
 *
 * @property previous show a text link to the previous page, according to the meta file
 * @property next show a text link to the following page, according to the meta file
 */
abstract class BottomNavigationOptions : ExtensionConfiguration {
  abstract val previous: Property<Boolean>
  abstract val next: Property<Boolean>

  override fun defaults() {
    previous.convention(true)
    next.convention(true)
  }

  internal fun convert() = Navigation(previous.get(), next.get())
}

/**
 * Configurations for the sidebar on the page.
 *
 * @property defaultMenuCollapseLevel The folder level at which the menu on the left is
 *   collapsed
 * @property toggleButton Show an extra "toggle" button for the sidebar, to toggle its visibility
 */
abstract class SidebarOptions : ExtensionConfiguration {
  abstract val defaultMenuCollapseLevel: Property<Int>
  abstract val toggleButton: Property<Boolean>

  override fun defaults() {
    defaultMenuCollapseLevel.convention(2)
    toggleButton.convention(false)
  }

  internal fun convert() = Sidebar(defaultMenuCollapseLevel.get(), toggleButton.get())
}

/** TODO() */
abstract class NavbarOptions : ExtensionConfiguration {
  abstract val logo: Property<File>

  override fun defaults() {
    logo.convention(null)
  }

  internal val banner = link<BannerOptions>("banner")
  internal val search = link<SearchOptions>("search")
  internal val projectIcon = link<ClickableIconOptions>("projectIcon")
  internal val chatIcon = link<ClickableIconOptions>("chatIcon")
}

/**
 * A banner to display at the top of the website.
 *
 * @property dismissible Should the banner have a button to close it
 * @property key A unique storage key for this banner. When updating banners, duplicate keys may
 *   result in banners not being properly updated. TODO() - double check this
 * @property text Text to show in the body of the banner
 */
abstract class BannerOptions : ExtensionConfiguration {
  abstract val dismissible: Property<Boolean>
  abstract val key: Property<String>
  abstract val text: Property<String>

  override fun defaults() {
    dismissible.convention(true)
    key.convention("banner")
  }

  internal fun convert() =
    Banner(dismissible.get(), key.get(), ReactNode.create("<span>${text.get()}</span>"))
}

/**
 * Configurations for the Search bar in the navbar element on the page.
 *
 * @property placeholder The placeholder text showcased on the input field for the search bar
 */
abstract class SearchOptions : ExtensionConfiguration {
  abstract val placeholder: Property<String>

  override fun defaults() {
    placeholder.convention("Search documentation...")
  }

  internal fun convert() = Search(placeholder.get())
}

/**
 * Represents an Icon with a link to follow when clicked.
 *
 * @property icon An image [File] to use as the Icon
 * @property link A destination to follow when the [icon] is clicked
 */
abstract class ClickableIconOptions : ExtensionConfiguration {
  abstract val icon: Property<File>
  abstract val link: Property<String>

  override fun defaults() {
    icon.convention(null)
    link.convention(null)
  }

  internal fun convert() =
    ClickableIcon(link.orNull, icon.orNull?.let { ReactNode.fromImage(it, 24) })
}

/**
 * The primary hues used for `Dark` and `Light` modes.
 *
 * @property dark The hue used for the primary color of the theme, when in `Dark` mode
 * @property light The hue used for the primary color of the theme, when in `Light` mode
 * @see ThemeOptions
 */
abstract class ThemeColorOptions : ExtensionConfiguration {
  abstract val dark: Property<Int>
  abstract val light: Property<Int>

  override fun defaults() {
    dark.convention(204)
    light.convention(212)
  }

  internal fun convert() = ThemeColor(dark.get(), light.get())
}
