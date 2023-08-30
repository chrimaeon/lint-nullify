/*
 * Copyright (c) 2018. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

import kotlinx.kover.gradle.plugin.dsl.AggregationType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Date

plugins {
    kotlin("jvm") version libs.versions.kotlin
    kotlin("kapt") version libs.versions.kotlin
    id("com.android.lint")
    alias(libs.plugins.kover)
    id("com.cmgapps.gradle.ktlint")
}

val buildConfigDirPath: Provider<Directory> = layout.buildDirectory.dir("generated/source/buildConfig")

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17)
}

sourceSets {
    main {
        java.srcDir(buildConfigDirPath)
    }
}

val pomName: String by project
val projectVersion: String by project

tasks {
    named<Jar>("jar") {
        manifest {
            attributes(
                "Implementation-Title" to pomName,
                "Implementation-Version" to projectVersion,
                "Built-By" to System.getProperty("user.name"),
                "Built-Date" to Date(),
                "Built-JDK" to System.getProperty("java.version"),
                "Built-Gradle" to gradle.gradleVersion,
                "Lint-Registry-v2" to "com.cmgapps.lint.NullifyIssueRegistry",
            )
        }
    }

    val generateBuildConfig by registering {
        val outputDir = buildConfigDirPath

        val projectArtifactId: String by project
        inputs.property("projectArtifactId", projectArtifactId)

        val feedbackUrl: String by project
        inputs.property("feedbackUrl", feedbackUrl)

        val packageName = "com.cmgapps.lint"
        inputs.property("packageName", packageName)

        outputs.dir(outputDir)

        doLast {
            val outputDirFile = outputDir.get().asFile
            outputDirFile.mkdirs()

            file(outputDirFile.resolve("BuildConfig.kt")).bufferedWriter().use {
                it.write(
                    """
                        |package $packageName
                        |const val FEEDBACK_URL = "$feedbackUrl"
                        |const val PROJECT_ARTIFACT = "$projectArtifactId"
                    """.trimMargin(),
                )
            }
        }
    }

    withType<Test> {
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    withType<KotlinCompile> {
        dependsOn(generateBuildConfig)
    }
}

koverReport {
    defaults {
        verify {
            rule {
                bound {
                    minValue = 80
                    aggregation = AggregationType.COVERED_PERCENTAGE
                }
            }
        }
    }
}

dependencies {
    compileOnly(libs.android.tools.lintApi)
    compileOnly(libs.android.tools.lintApi)

    compileOnly(libs.kotlin.stdlib7)

    compileOnly(libs.auto.serviceAnnotations)
    kapt(libs.auto.service)

    testImplementation(libs.junit)
    testImplementation(libs.android.tools.lint)
    testImplementation(libs.android.tools.lintTests)
    testImplementation(libs.android.tools.testutils)
    testImplementation(libs.hamcrest)
}
