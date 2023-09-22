import net.fabricmc.loom.task.RemapJarTask

plugins {
  id("fabric-loom") version "1.3-SNAPSHOT"
  id("com.github.hierynomus.license-base") version "0.16.1"
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(17))
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
  encoding = Charsets.UTF_8.name()
  mapping("java", "DOUBLESLASH_STYLE")
  include("**/*.java")
}

tasks {
  check {
    finalizedBy(licenseMain)
  }

  withType<JavaCompile> {
    options.encoding = Charsets.UTF_8.name()
    options.release.set(17)
  }

  withType<ProcessResources> {
    inputs.property("project.version", project.version)

    filteringCharset = Charsets.UTF_8.name()
    filesMatching("fabric.mod.json") {
      expand("version" to project.version)
    }
  }

  withType<RemapJarTask> {
    archiveBaseName.set("EpicForcedResourcePackBypassMod")
  }

  withType<Jar> {
    inputs.property("project.group", project.group)
    inputs.property("project.name", project.name)
    inputs.file("COPYING")
    inputs.file("COPYING.LESSER")

    manifestContentCharset = Charsets.UTF_8.name()
    metaInf {
      into("${project.group}/${project.name}") {
        from("COPYING")
        from("COPYING.LESSER")
      }
    }
  }
}
