// Loader-independent game logic: no Minecraft, no loader, no Create on the classpath.
// Compiled for Java 17 so both Minecraft versions can use it.
plugins {
    `java-library`
}

group = "fr.talfa.deepcurrent"

java {
    toolchain.languageVersion = JavaLanguageVersion.of(17)
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
