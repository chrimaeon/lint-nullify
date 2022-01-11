/*
 * Copyright (c) 2018. Christian Grach <christian.grach@cmgapps.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import kotlinx.kover.api.VerificationValueType.COVERED_LINES_PERCENTAGE
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Date
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.div

plugins {
    kotlin("jvm") version Version.KOTLIN
    kotlin("kapt") version Version.KOTLIN
    id("com.android.lint")
    id("org.jetbrains.kotlinx.kover") version Version.KOVER_PLUGIN
    id("com.cmgapps.gradle.ktlint")
}

@OptIn(ExperimentalPathApi::class)
val buildConfigDirPath = buildDir.toPath() / "generated" / "source" / "buildConfig"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
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
                "Lint-Registry-v2" to "com.cmgapps.lint.NullifyIssueRegistry"
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
            outputDir.toFile().mkdirs()

            file(outputDir.resolve("BuildConfig.kt")).bufferedWriter().use {
                it.write(
                    """
                        |package $packageName
                        |const val FEEDBACK_URL = "$feedbackUrl"
                        |const val PROJECT_ARTIFACT = "$projectArtifactId"
                    """.trimMargin()
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
        kotlinOptions.jvmTarget = "1.8"
        dependsOn(generateBuildConfig)
    }

    koverVerify {
        rule {
            name = "Minimal line coverage rate in percent"
            bound {
                minValue = 80
                valueType = COVERED_LINES_PERCENTAGE
            }
        }
    }
}

dependencies {
    compileOnly(Deps.LINT_API)
    compileOnly(Deps.LINT_CHECKS)

    compileOnly(kotlin("stdlib-jdk8", Version.KOTLIN))
    // Necessary to bump a transitive dependency.
    compileOnly(kotlin("reflect", Version.KOTLIN))

    compileOnly(Deps.AUTO_SERVICE_ANNOTATIONS)
    kapt(Deps.AUTO_SERVICE)

    testImplementation(kotlin("stdlib-jdk8", Version.KOTLIN))
    // Necessary to bump a transitive dependency.
    testImplementation(kotlin("reflect", Version.KOTLIN))
    testImplementation(Deps.JUNIT)
    testImplementation(Deps.LINT)
    testImplementation(Deps.LINT_TEST)
    testImplementation(Deps.ANDROID_TESTUTILS)
    testImplementation(Deps.HAMCREST)
}
