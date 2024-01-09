import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.ktlint) apply false
}

subprojects {
    apply {
        plugin(rootProject.libs.plugins.kotlin.multiplatform.get().pluginId)
        plugin(rootProject.libs.plugins.ktlint.get().pluginId)
    }

    configure<KtlintExtension> {
        version = "1.1.0"

        reporters {
            reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.JSON)
        }
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