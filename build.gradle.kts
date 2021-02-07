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

import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

buildscript {
    repositories {
        google()
        mavenCentral()
        jcenter()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:" + Version.ANDROID_GRADLE_PLUGIN)
    }
}

plugins {
    id("com.github.ben-manes.versions") version Version.VERSIONS_PLUGIN
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
    repositories {
        google()
        jcenter()
        mavenCentral()
    }

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
        delete(rootProject.buildDir)
    }

    wrapper {
        distributionType = Wrapper.DistributionType.ALL
        gradleVersion = "6.8.2"
    }
}
