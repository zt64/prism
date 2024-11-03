import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest
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
                entryPoint = "dev.zt64.prism.main"
            }
        }
    }

    sourceSets {
        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlin.coroutines.test)
            }
        }

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

abstract class XvfbServer :
    BuildService<XvfbServer.Parameters>,
    AutoCloseable {
    interface Parameters : BuildServiceParameters {
        val executable: Property<String>
        val arguments: ListProperty<String>
    }

    val display: String = ":99"

    private val xvfbProcess = ProcessBuilder()
        .command(parameters.executable.get(), *parameters.arguments.get().toTypedArray(), display)
        .redirectInput(ProcessBuilder.Redirect.from(File("/dev/null")))
        .start()

    init {
        val logger = Logging.getLogger(XvfbServer::class.java)
        logger.info("Xvfb: PID=${xvfbProcess.pid()}")

        thread {
            xvfbProcess.errorStream.reader().useLines { lines ->
                for (line in lines) logger.error("Xvfb: err=$line")
            }
        }
    }

    override fun close() {
        xvfbProcess.destroy()
    }
}

val xvfbServer = gradle.sharedServices.registerIfAbsent("xvfb", XvfbServer::class) {
    parameters.executable = "Xvfb"
    parameters.arguments.empty()
}

tasks.withType<KotlinNativeTest>().configureEach {
    usesService(xvfbServer)
    doFirst { environment("DISPLAY", xvfbServer.get().display) }
}