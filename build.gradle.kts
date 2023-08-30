/*
 * Copyright (c) 2018. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath(libs.android.gradlePlugin)
    }
}

plugins {
    alias(libs.plugins.versions)
}

tasks.withType<DependencyUpdatesTask> {
    revision = "release"

    rejectVersionIf {
        listOf("alpha", "beta", "rc", "cr", "m").any { qualifier ->
            """(?i).*[.-]$qualifier[.\d-]*""".toRegex()
                .containsMatchIn(candidate.version)
        }
    }
}

allprojects {
    gradle.projectsEvaluated {
        tasks {
            withType<JavaCompile> {
                options.compilerArgs.addAll(listOf("-Xlint:deprecation"))
            }
        }
    }
}

tasks {
    register<Delete>("clean") {
        delete(rootProject.layout.buildDirectory)
    }

    wrapper {
        distributionType = Wrapper.DistributionType.ALL
        gradleVersion = "8.3"
    }
}
