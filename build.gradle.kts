plugins {
    kotlin("multiplatform") version "1.7.20-Beta"
    kotlin("plugin.serialization") version "1.7.20-Beta"
}

repositories {
    mavenCentral()
}

kotlin {
    linuxX64("native") {
        compilations.getByName("main") {
            val xlib by cinterops.creating
        }

        binaries {
            executable("prism")

            executable("prismc") {
                entryPoint = "prismClient"
            }
        }
    }

    sourceSets {
        val nativeMain by getting {
            dependencies {
                val ktomlVersion = "0.2.13"

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-linuxx64:1.6.4")
                implementation("com.akuleshov7:ktoml-core:$ktomlVersion")
                implementation("com.akuleshov7:ktoml-file:$ktomlVersion")
                implementation("com.github.ajalt.clikt:clikt-linuxx64:3.5.0")
                implementation("io.insert-koin:koin-core:3.2.0")
            }
        }
    }
}