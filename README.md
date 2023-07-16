
![Logo](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/th5xamgrr6se0x5ro4g6.png)


# kWiki

Convert your markdown files into beautiful documentation.

Learn how to use kWiki by reading our documentation [here.]()
## Example

```kotlin
kWiki {
  wikiSourceDirectory.set(project.file("wiki"))
  outputDirectory.set(project.file("docs"))
}
```

When you're ready to generate a wiki, just run the gradle task `./gradlew kWiki` and watch the magic happen.


## Overview

kWiki is a Kotlin Gradle Plugin that lets your focus on your documentation; leaving the static site generation to us.
Write your documentation in markdown files, optionally specify a ToC in a local json file, and configure theme specifics
via the gradle plugin in your build file(s).

You can learn more about the background and motivation for kWiki by reading [this page]() from our documentation.

## Screenshots

![App Screenshot](https://via.placeholder.com/468x300?text=App+Screenshot+Here)


## Installation

You can use kWiki in your own build by adding a plugin dependency in your build script:

```kotlin
plugins {
    id("com.daymxn.kwiki")
}
```

And then pulling the artifact from any of the following repositories:

### Gradle Plugin Portal

```kotlin
pluginManagement {
  repositories {
    gradlePluginPortal()
  }
}
```

### GitHub Packages

```kotlin
pluginManagement {
  val user: String by settings.extra.properties.withDefault { System.getenv("USERNAME") }
  val pass: String by settings.extra.properties.withDefault { System.getenv("TOKEN") }
            
  repositories {
    maven {
      name = "GitHubPackages"
      url = uri("https://maven.pkg.github.com/daymxn/kwiki")
      credentials {
        username = user
        password = pass
      }
    }
  }
}
```

## Building from source

Optionally, if you'd rather keep things in-house (or maybe you want to add some additional internal features), you
can pull the artifact locally.

Start by cloning the repository from GitHub:
```shell
git clone git@github.com:daymxn/kwiki.git
```

And then either publish the plugin to your local maven repository:

```shell
./gradlew publishToMavenLocal
```

Or create a Jar artifact to use elsewhere:

```shell
./gradlew jar
```

## Contributing

Contributions are always welcome!

See [contributing]() for ways to get started.

## License

[Apache 2.0](LICENSE)

