plugins {
    id("net.neoforged.moddev.legacyforge")
}

fun prop(key: String): String = sc.properties[key]
val mc = sc.current.version
val modId = prop("mod.id")
val javaVersion = prop("mod.java").toInt()
val core = project(":core")

version = "${prop("mod.version")}+$mc-forge"
group = prop("mod.group")
base.archivesName = "create-deep-current"

java.toolchain.languageVersion = JavaLanguageVersion.of(javaVersion)

repositories {
    maven("https://maven.createmod.net") { name = "Create (Ponder, Flywheel)" }
    maven("https://maven.ithundxr.dev/mirror") { name = "Registrate" }
    maven("https://maven.parchmentmc.org") { name = "ParchmentMC" }
}

legacyForge {
    version = prop("deps.minecraftforge")

    parchment {
        minecraftVersion = prop("deps.parchment_mc")
        mappingsVersion = prop("deps.parchment")
    }

    runs {
        register("client") {
            client()
            gameDirectory = rootProject.file("run/${sc.current.project}/client")
        }
        register("server") {
            server()
            gameDirectory = rootProject.file("run/${sc.current.project}/server")
            programArgument("--nogui")
        }
        // Boots a headless server, runs the mod's game tests and exits: used to check that the mod loads.
        register("gameTestServer") {
            type = "gameTestServer"
            gameDirectory = rootProject.file("run/${sc.current.project}/gametest")
            systemProperty("forge.enabledGameTestNamespaces", modId)
        }
    }

    mods {
        register(modId) {
            sourceSet(sourceSets.main.get())
            sourceSet(core.sourceSets.main.get())
        }
    }
}

dependencies {
    implementation(core)

    // Create is compiled against its slim jar; its libraries are declared explicitly.
    modImplementation("com.simibubi.create:create-$mc:${prop("deps.create")}:slim") { isTransitive = false }
    modImplementation("net.createmod.ponder:Ponder-Forge-$mc:${prop("deps.ponder")}")
    modImplementation("com.tterrag.registrate:Registrate:${prop("deps.registrate")}")
    modCompileOnly("dev.engine-room.flywheel:flywheel-forge-api-$mc:${prop("deps.flywheel")}")
    modRuntimeOnly("dev.engine-room.flywheel:flywheel-forge-$mc:${prop("deps.flywheel")}")

    // Shipped inside Create's full jar; the slim jar needs it on the dev runtime.
    runtimeOnly("io.github.llamalad7:mixinextras-forge:${prop("deps.mixinextras")}")
}

tasks.processResources {
    val metadata = mapOf(
        "mod_id" to modId,
        "mod_version" to version.toString(),
        "mod_name" to prop("mod.name"),
        "mod_description" to prop("mod.description"),
        "mod_authors" to prop("mod.authors"),
        "mod_license" to prop("mod.license"),
        "mod_issues_url" to prop("mod.issues_url"),
        "dep_minecraft" to prop("meta.minecraft"),
        "dep_loader" to prop("meta.loader"),
        "dep_create" to prop("meta.create"),
    )
    inputs.properties(metadata)
    filesMatching(listOf("META-INF/mods.toml", "pack.mcmeta")) { expand(metadata) }
    // Model extras (emissive faces...) are written for NeoForge; Forge 1.20.1 names the same object forge_data.
    filesMatching("assets/*/models/**/*.json") {
        filter { line -> line.replace("\"neoforge_data\"", "\"forge_data\"") }
    }
    exclude("META-INF/neoforge.mods.toml")
}

// The core module is not a separate mod: its classes ship inside the mod jar.
tasks.jar {
    from(core.sourceSets.main.get().output)
    from(rootProject.file("LICENSE")) { rename { "LICENSE_$modId" } }
}

tasks.named("createMinecraftArtifacts") {
    dependsOn("stonecutterGenerate")
}
