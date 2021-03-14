import org.gradle.internal.os.OperatingSystem

plugins {
    id("java-library")
}

val lwjglVersion = "3.2.4-SNAPSHOT"
val jomlVersion = "1.10.0"
val lwjglNatives = when (OperatingSystem.current()) {
    OperatingSystem.LINUX -> "natives-linux"
    OperatingSystem.MAC_OS -> "natives-macos"
    OperatingSystem.WINDOWS -> "natives-windows"
    else -> throw Error("Unrecognized or unsupported Operating system. Please set \"lwjglNatives\" manually")
}
val slf4jVersion = "1.8.0-beta4"
var disruptorVersion = "1.2.19"

logger.quiet("Natives in use: $lwjglNatives")

sourceSets.main.get().resources.srcDirs("../resources")

dependencies {
    api(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))

    api("org.lwjgl", "lwjgl")
    api("org.lwjgl", "lwjgl-glfw")
    api("org.lwjgl", "lwjgl-assimp")
    api("org.lwjgl", "lwjgl-opengl")
    api("org.lwjgl", "lwjgl-shaderc")
    runtimeOnly("org.lwjgl", "lwjgl", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-glfw", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-assimp", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-opengl", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-shaderc", classifier = lwjglNatives)

    api("org.joml", "joml", jomlVersion)
    api("org.slf4j", "slf4j-api", slf4jVersion)
    api("org.slf4j", "slf4j-simple", slf4jVersion)
    api("com.conversantmedia", "disruptor", disruptorVersion)
}