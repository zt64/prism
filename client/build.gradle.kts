plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    listOf(
        linuxX64(),
        linuxArm64()
    ).forEach {
        it.binaries {
            executable("prismc")
        }
    }

    sourceSets {
        linuxMain {
            dependencies {
                implementation(projects.common)
                implementation(libs.clikt)
                implementation(libs.kotlin.coroutines.core)
            }
        }
    }
}