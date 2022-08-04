import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    id("com.github.node-gradle.node")  version "3.4.0"
    id("maven-publish")
}

group = "com.daymxn"
version = "0.9.15"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
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
    implementation("com.github.junrar:junrar:7.5.2")
    implementation("com.github.node-gradle:gradle-node-plugin:3.4.0")
}
