plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    linuxX64 {
        compilations.getByName("main") {
            cinterops.create("xlib")
        }
    }
}