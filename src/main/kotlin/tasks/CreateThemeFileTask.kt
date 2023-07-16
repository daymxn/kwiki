package tasks

import java.io.File
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonUnquotedLiteral
import org.gradle.api.DefaultTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.configurationcache.extensions.capitalized
import util.toPascalCase

/** TODO() */
abstract class CreateThemeFileTask : DefaultTask() {
  @get:Input abstract val options: Property<String>

  @get:Input abstract val imports: ListProperty<String>

  @get:OutputFile abstract val outputFile: Property<File>

  @TaskAction
  fun create() {
    outputFile.get().writeText(generateFileText(options.get(), imports.get()))
  }

  private fun generateFileText(content: String, imports: List<String>) =
    """
      ${makeImageImports(imports).joinToString(";")}

      export default ${content.dropLast(1)},
      useNextSeoProps() {
        return {
          titleTemplate: '%s'
        }
      }
      }
    """
      .trimIndent()

  private fun makeImageImports(imports: List<String>) = imports.map { it.toJavascriptImport() }

  private fun String.toJavascriptImport() = "import ${capitalized()} from './$this.svg'"
}

@OptIn(ExperimentalSerializationApi::class)
private val Format = Json {
  explicitNulls = false
  isLenient = true
}

@Serializable data class ThemeColor(val dark: Int, val light: Int)

@Serializable
data class Theme(
  val docsRepositoryBase: String,
  val darkMode: Boolean,
  val primaryHue: ThemeColor,
  val logo: JsonPrimitive,
  val project: ClickableIcon?,
  val chat: ClickableIcon?,
  val search: Search,
  val banner: Banner?,
  val sidebar: Sidebar,
  val navigation: Navigation,
  val gitTimestamp: JsonPrimitive?,
  val footer: Footer,
  val feedback: Feedback?
) {
  fun toJsonString() = Format.encodeToString(this)
}

@Serializable data class Feedback(val content: JsonPrimitive)

@Serializable data class Search(val placeholder: String)

@Serializable data class Footer(val text: String, val component: JsonPrimitive?)

@Serializable data class Navigation(val prev: Boolean, val next: Boolean)

@Serializable data class Sidebar(val defaultMenuCollapseLevel: Int, val toggleButton: Boolean)

@Serializable
data class Banner(val dismissible: Boolean, val key: String, val text: JsonPrimitive?)

@Serializable data class ClickableIcon(val link: String?, val icon: JsonPrimitive?)

@OptIn(ExperimentalSerializationApi::class)
object ReactNode {
  fun fromImage(imageFile: File?, size: Int) = fromImageOrNull(imageFile, size) ?: empty

  fun fromImageOrNull(imageFile: File?, size: Int) =
    imageFile?.let {
      create(
        "<${imageFile.nameWithoutExtension.toPascalCase()} width={${size}} height={${size}} />"
      )
    }

  fun create(content: String) = JsonUnquotedLiteral(content)

  val empty = create(EMPTY_NODE)
}

const val EMPTY_NODE = "<></>"

val EmptyReactNode = ReactNode.create(EMPTY_NODE)
