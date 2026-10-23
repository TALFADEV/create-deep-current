plugins {
    id("net.neoforged.moddev")
}

fun prop(key: String): String = sc.properties[key]
val mc = sc.current.version
val modId = prop("mod.id")
val javaVersion = prop("mod.java").toInt()
val core = project(":core")

version = "${prop("mod.version")}+$mc-neoforge"
group = prop("mod.group")
base.archivesName = "create-deep-current"

java.toolchain.languageVersion = JavaLanguageVersion.of(javaVersion)

repositories {
    maven("https://maven.createmod.net") { name = "Create (Ponder, Flywheel)" }
    maven("https://maven.ithundxr.dev/snapshots") { name = "Registrate" }
    maven("https://maven.parchmentmc.org") { name = "ParchmentMC" }
}

neoForge {
    version = prop("deps.neo")

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
            systemProperty("neoforge.enabledGameTestNamespaces", modId)
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

    // Create is compiled against its slim jar; its libraries are declared explicitly so that the
    // optional integrations listed in Create's POM (FTB, JourneyMap, CC...) stay out of the workspace.
    implementation("com.simibubi.create:create-$mc:${prop("deps.create")}:slim") { isTransitive = false }
    implementation("net.createmod.ponder:ponder-neoforge:${prop("deps.ponder")}")
    implementation("com.tterrag.registrate:Registrate:${prop("deps.registrate")}")
    compileOnly("dev.engine-room.flywheel:flywheel-neoforge-api-$mc:${prop("deps.flywheel")}")
    runtimeOnly("dev.engine-room.flywheel:flywheel-neoforge-$mc:${prop("deps.flywheel")}")
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
    filesMatching("META-INF/neoforge.mods.toml") { expand(metadata) }
    exclude("META-INF/mods.toml", "pack.mcmeta")
}

// The core module is not a separate mod: its classes ship inside the mod jar.
tasks.jar {
    from(core.sourceSets.main.get().output)
    from(rootProject.file("LICENSE")) { rename { "LICENSE_$modId" } }
}

tasks.named("createMinecraftArtifacts") {
    dependsOn("stonecutterGenerate")
}
