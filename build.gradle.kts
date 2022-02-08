plugins {
    kotlin("multiplatform") version "1.6.0"
    kotlin("plugin.serialization") version "1.5.31"
}

repositories {
    mavenCentral()
}

kotlin {
    val nativeTarget = if (System.getProperty("os.name") == "Linux") linuxX64("native")
    else throw GradleException("Host OS is not supported in Kotlin/Native.")

    nativeTarget.apply {
        binaries {
            executable("prism") {
                entryPoint = "main"
            }
            executable("prismc") {
                entryPoint = "prismClient"
            }
        }

        val xlib by compilations.getByName("main").cinterops.creating
    }

    sourceSets {
        val nativeMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-linuxx64:1.6.0-RC")
                implementation("com.akuleshov7:ktoml-core:0.2.8")
                implementation("com.akuleshov7:ktoml-file:0.2.8")
                implementation("com.github.ajalt.clikt:clikt-linuxx64:3.3.0")
            }
        }
        val nativeTest by getting
    }
}