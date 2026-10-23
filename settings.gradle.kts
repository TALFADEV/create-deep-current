pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie" }
    }
    plugins {
        id("net.neoforged.moddev") version "2.0.148"
        id("net.neoforged.moddev.legacyforge") version "2.0.148"
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.8"
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "deep-current"

// Loader-independent logic, shared by every Minecraft version.
include("core")

stonecutter {
    create(rootProject) {
        fun match(version: String, loader: String) =
            version("$version-$loader", version).buildscript("build.$loader.gradle.kts")
        match("1.21.1", "neoforge")
        match("1.20.1", "forge")
        vcsVersion = "1.21.1-neoforge"
    }
}
