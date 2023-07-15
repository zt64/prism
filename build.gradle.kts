import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

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
        outputToConsole.set(true)
        outputToConsole = true

        reporters {
            reporter(ReporterType.SARIF)
            reporter(ReporterType.CHECKSTYLE)
        }
    }
}