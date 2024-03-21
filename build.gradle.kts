import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.ktlint) apply false
}

allprojects {
    apply {
        plugin(rootProject.libs.plugins.ktlint.get().pluginId)
    }

    configure<KtlintExtension> {
        version = rootProject.libs.versions.ktlint.get()

        reporters {
            reporter(ReporterType.JSON)
        }
    }
}

subprojects {
    apply {
        plugin(rootProject.libs.plugins.kotlin.multiplatform.get().pluginId)
    }

    with(kotlinExtension) {
        sourceSets {
            all {
                languageSettings {
                    optIn("kotlinx.cinterop.ExperimentalForeignApi")
                }
            }
        }
    }
}