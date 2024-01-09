import java.util.concurrent.CompletableFuture
import kotlin.concurrent.thread

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    listOf(
        linuxX64()
        // linuxArm64()
    ).forEach {
        it.binaries {
            executable("prism") {
                entryPoint = "main"
            }
        }
    }

    sourceSets {
        linuxMain {
            dependencies {
                implementation(projects.common)
                implementation(libs.clikt)
                implementation(libs.kotlin.coroutines.core)
                implementation(libs.bundles.ktoml)
                implementation(libs.bundles.ktor)
            }
        }
    }
}

abstract class XvfbServer : BuildService<XvfbServer.Parameters>, AutoCloseable {
    interface Parameters : BuildServiceParameters {
        val executable: Property<String>
        val arguments: ListProperty<String>
    }

    private val xvfbProcess = ProcessBuilder()
        .command(parameters.executable.get(), "-displayfd", "1", *parameters.arguments.get().toTypedArray())
        .redirectInput(ProcessBuilder.Redirect.from(File("/dev/null")))
        .start()

    private val _display = CompletableFuture<String>()

    init {
        val logger = Logging.getLogger(XvfbServer::class.java)
        logger.info("Xvfb: PID=${xvfbProcess.pid()}")
        thread {
            xvfbProcess.inputStream.reader().useLines { lines ->
                lines.fold(true) { isFirst, line ->
                    if (isFirst) {
                        logger.info("Xvfb: DISPLAY=:$line")
                        _display.complete(":$line")
                    } else {
                        logger.info("Xvfb: out=$line")
                    }
                    false
                } && _display.completeExceptionally(IllegalStateException("No display"))
            }
        }
        thread {
            xvfbProcess.errorStream.reader().useLines { lines ->
                for (line in lines) logger.info("Xvfb: err=$line")
            }
        }
    }

    val display: String get() = _display.get()

    override fun close() {
        xvfbProcess.destroy()
    }
}

val xvfbServer = gradle.sharedServices.registerIfAbsent("xvfb", XvfbServer::class) {
    parameters.executable = "Xvfb"
    parameters.arguments.empty()
}

tasks.withType<Test>().configureEach {
    usesService(xvfbServer)
    doFirst { environment("DISPLAY", xvfbServer.get().display) }
    useJUnitPlatform()
}