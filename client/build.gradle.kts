plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    linuxX64 {
        binaries {
            executable("prismc")
        }
    }

    sourceSets {
        getByName("linuxX64Main") {
            dependencies {
                implementation(projects.common)
                implementation(libs.kotlin.cli)
                implementation(libs.kotlin.coroutines.core)
            }
        }
    }
}