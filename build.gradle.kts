plugins {
    id("fabric-loom") version "0.10-SNAPSHOT"
    id("com.github.hierynomus.license-base") version "0.16.1"
}

project.group = "io.github.emilyy-dev"
project.version = "1.0.0"

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

    withType<Wrapper> {
        gradleVersion = "7.3.3"
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

    withType<Jar> {
        inputs.property("project.group", project.group)
        inputs.property("project.name", project.name)

        manifestContentCharset = Charsets.UTF_8.name()
        manifest.attributes["Automatic-Module-Name"] = "${project.group}.${project.name}".replace("-", "_")
        metaInf {
            from("LICENSE.txt") {
                into("${project.group}/${project.name}")
            }
        }
    }
}
