plugins {
  id("net.fabricmc.fabric-loom") version "1.15-SNAPSHOT"
}

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(25)
  }
}

val minecraftVersion: Provider<String> = providers.gradleProperty("minecraftVersion")
val loaderVersion: Provider<String> = providers.gradleProperty("loaderVersion")

dependencies {
  minecraft(minecraftVersion.map { "com.mojang:minecraft:$it" })
  implementation(loaderVersion.map { "net.fabricmc:fabric-loader:$it" })
}

tasks {
  withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs = listOf("-Xlint:all")
  }

  withType<ProcessResources> {
    val version = project.version
    inputs.property("project.version", version)

    filteringCharset = "UTF-8"
    filesMatching("fabric.mod.json") {
      expand("version" to version)
    }
  }

  withType<Jar> {
    archiveBaseName = "EpicForcedResourcePackBypassMod"
    inputs.files("COPYING", "COPYING.LESSER")
    inputs.properties(
      "project.group" to project.group,
      "project.name" to project.name
    )

    manifestContentCharset = "UTF-8"
    metaInf {
      into("${project.group}/${project.name}").from("COPYING", "COPYING.LESSER")
    }
  }
}
