plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    linuxX64 {
        binaries {
            executable("prism") {
                entryPoint = "main"
            }
        }
    }

    sourceSets {
        getByName("linuxX64Main") {
            dependencies {
                implementation(projects.common)
                implementation(libs.kotlin.cli)
                implementation(libs.kotlin.coroutines.core)
                implementation(libs.bundles.ktoml)
                implementation(libs.bundles.ktor)
            }
        }
    }
}