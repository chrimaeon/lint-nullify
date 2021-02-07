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

import java.util.Date
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version Version.KOTLIN
    kotlin("kapt") version Version.KOTLIN
    id("com.android.lint")
    jacoco
    id("com.cmgapps.gradle.ktlint")
    id("org.jetbrains.dokka") version Version.DOKKA_PLUGIN
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

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
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

    withType<Test> {
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    jacocoTestCoverageVerification {
        violationRules {
            rule {
                limit {
                    counter = "INSTRUCTION"
                    minimum = "0.8".toBigDecimal()
                }
            }
        }
    }
}
