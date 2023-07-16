plugins {
  `kotlin-dsl`
  id("com.github.node-gradle.node") version "3.5.1"
  id("maven-publish")
  kotlin("plugin.serialization") version "1.8.10"
}

group = "com.daymxn"
version = "1.0.0"

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

publishing {
  repositories {
    maven {
      name = "GitHubPackages"
      url = uri("https://maven.pkg.github.com/daymxn/kwiki")
      credentials {
        username = project.findProperty("gpr.user")?.toString() ?: System.getenv("USERNAME")
        password = project.findProperty("gpr.key")?.toString() ?: System.getenv("TOKEN")
      }
    }
  }
}

repositories {
  gradlePluginPortal()
  mavenCentral()
}

gradlePlugin {
  plugins {
    register("kwiki") {
      id = "com.daymxn.kwiki"
      implementationClass = "core.kWikiPlugin"
    }
  }
}

dependencies {
  implementation("com.github.junrar:junrar:7.5.4")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
  implementation("com.github.node-gradle:gradle-node-plugin:3.5.1")
}
