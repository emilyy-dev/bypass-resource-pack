pluginManagement {
  repositories {
    maven("https://maven.fabricmc.net/")
    gradlePluginPortal()
  }
}

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
}

rootProject.name = "bypass-resource-pack"
