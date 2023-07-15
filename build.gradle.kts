import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType
import org.jlleitschuh.gradle.ktlint.tasks.GenerateReportsTask

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
        version = "0.49.1"

        reporters {
            reporter(ReporterType.SARIF)
        }
    }

    tasks.withType<GenerateReportsTask> {
        reportsOutputDirectory.set(
            buildDir.resolve("reports/ktlint/$name")
        )
    }
}