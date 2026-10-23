plugins {
    id("dev.kikugie.stonecutter")
    id("net.neoforged.moddev") apply false
    id("net.neoforged.moddev.legacyforge") apply false
}

stonecutter active "1.21.1-neoforge"

stonecutter parameters {
    val loader = current.project.substringAfter('-')
    properties { tags(current.version, loader) }
    constants { match(loader, "forge", "neoforge") }
}
