plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    listOf(
        linuxX64()
        // linuxArm64()
    ).forEach {
        it.compilations.getByName("main") {
            cinterops.create("xlib")
        }
    }
}