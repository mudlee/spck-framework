import org.gradle.internal.os.OperatingSystem
import kotlin.streams.toList

plugins {
    id("java")
    id("application")
    id("org.beryx.jlink").version("2.23.1")
}

dependencies {
    implementation(project(":core"))
}

// NATIVES SETUP
var jars: Set<File> = sourceSets.main.get().runtimeClasspath.files
var nativeJars: List<String> = jars.filter { file -> file.name.contains("natives-") }.stream().map { file -> file.absolutePath }.toList()

logger.quiet("NATIVES:")
nativeJars.forEach {
    logger.quiet("  $it")
}

fun getJvmArgs(): List<String> {
    logger.quiet("OS: ${OperatingSystem.current()}");
    val extraJvmArgs = if (OperatingSystem.current().isMacOsX) listOf("-XstartOnFirstThread") else emptyList()
    var args = listOf("-cp", nativeJars.joinToString(File.pathSeparator), "-Dorg.lwjgl.system.allocator=system", "-Dorg.lwjgl.util.DebugLoader=true", "-Dorg.lwjgl.util.Debug=true", "-Dorg.lwjgl.opengl.Display.enableHighDPI=true", "-Dorg.lwjgl.opengl.Display.enableOSXFullscreenModeAPI=true")
    args = args.plus(extraJvmArgs)
    return args
}

application {
    val args = getJvmArgs()
    logger.quiet("JVM args: ${args.joinToString(", ")}")
    mainModule.set(moduleName)
    mainClass.set("spck.desktop.Example")
    applicationDefaultJvmArgs = args
}

jlink {
    val args = getJvmArgs()
    logger.quiet("JVM args: ${args.joinToString(", ")}")
    addOptions("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages")

    launcher {
        name = "app"
        jvmArgs = args
    }
}