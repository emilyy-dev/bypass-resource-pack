import net.fabricmc.loom.task.RemapJarTask

plugins {
  id("fabric-loom") version "1.10-SNAPSHOT"
  id("com.github.hierynomus.license-base") version "0.16.1"
}

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(21)
  }
}

val minecraftVersion: String by project
val loaderVersion: String by project

dependencies {
  minecraft("com.mojang", "minecraft", minecraftVersion)
  mappings(loom.officialMojangMappings())
  modImplementation("net.fabricmc", "fabric-loader", loaderVersion)
}

license {
  header = file("header.txt")
  encoding = "UTF-8"
  mapping("java", "DOUBLESLASH_STYLE")
  include("**/*.java")
}

tasks {
  check {
    dependsOn(licenseMain)
  }

  withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release = 21
  }

  withType<ProcessResources> {
    inputs.property("project.version", project.version)

    filteringCharset = "UTF-8"
    filesMatching("fabric.mod.json") {
      expand("version" to project.version)
    }
  }

  withType<RemapJarTask> {
    archiveBaseName = "EpicForcedResourcePackBypassMod"
  }

  withType<Jar> {
    inputs.files("COPYING", "COPYING.LESSER")
    inputs.properties(
      "project.group" to project.group,
      "project.name" to project.name
    )

    manifestContentCharset = "UTF-8"
    metaInf {
      into("${project.group}/${project.name}") {
        from("COPYING")
        from("COPYING.LESSER")
      }
    }
  }
}
