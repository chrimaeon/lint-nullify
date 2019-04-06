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
import com.jfrog.bintray.gradle.BintrayExtension
import java.io.FileInputStream
import java.util.*

plugins {
    `java-library`
    `maven-publish`
    kotlin("jvm") version "1.3.21"
    id("com.jfrog.bintray") version "1.8.4"
    id("com.github.ben-manes.versions") version "0.21.0"
}

group = "com.cmgapps.android"
version = "1.1"

val lintVersion = "26.3.2"

dependencies {
    compileOnly("com.android.tools.lint:lint-api:$lintVersion")
    compileOnly("com.android.tools.lint:lint-checks:$lintVersion")

    testImplementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.21")
    testImplementation("junit:junit:4.12")
    testImplementation("com.android.tools.lint:lint:$lintVersion")
    testImplementation("com.android.tools.lint:lint-tests:$lintVersion")
    testImplementation("com.android.tools:testutils:$lintVersion")
}

tasks.named<DependencyUpdatesTask>("dependencyUpdates") {
    revision = "release"
    resolutionStrategy {
        componentSelection {
            all {
                listOf("alpha", "beta", "rc", "cr", "m", "preview", "b", "ea")
                    .map { qualifier -> Regex("(?i).*[.-]$qualifier[.\\d-+]*") }
                    .any { it.matches(candidate.version) }
                    .let {
                        if (it) {
                            reject("Release candidate")
                        }
                    }
            }
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

val pomName = "Android Nullify Lint Checks"

tasks.named<Jar>("jar") {
    manifest {
        attributes("Implementation-Title" to pomName,
            "Implementation-Version" to project.version.toString(),
            "Built-By" to System.getProperty("user.name"),
            "Built-Date" to Date(),
            "Built-JDK" to System.getProperty("java.version"),
            "Built-Gradle" to gradle.gradleVersion,
            "Lint-Registry-v2" to "com.cmgapps.lint.IssueRegistry")
    }
}

tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}

tasks.register<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    from(tasks["javadoc"])
}

val scmUrl = "https://github.com/chrimaeon/lint-nullify"

publishing {
    publications {
        create<MavenPublication>("bintray") {
            from(components["java"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])
            artifactId = "checks"
            pom {

                name.set(pomName)
                description.set("Lint checks for @Nullable/@NonNull")
                url.set(scmUrl)

                licenses {
                    license {
                        name.set("The Apache Software License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                    }
                }

                developers {
                    developer {
                        id.set("chrimaeon")
                        name.set("Christian Grach")
                        email.set("christian.grach@cmgapps.com")
                    }
                }

                scm {
                    connection.set("scm:git:git@github.com:chrimaeon/lint-nullify.git")
                    developerConnection.set("scm:git:git@github.com:chrimaeon/lint-nullify.git")
                    url.set(scmUrl)
                }
            }
        }
    }

}

bintray {
    val credentialProps = Properties()
    credentialProps.load(FileInputStream(file("${project.rootDir}/credentials.properties")))
    user = credentialProps.getProperty("user")
    key = credentialProps.getProperty("key")
    setPublications("bintray")

    pkg(closureOf<BintrayExtension.PackageConfig> {
        repo = "maven"
        name = "${project.group}:checks"
        userOrg = user
        setLicenses("Apache-2.0")
        vcsUrl = scmUrl
        issueTrackerUrl = "https://github.com/chrimaeon/lint-nullify/issues"
        version(closureOf<BintrayExtension.VersionConfig> {
            name = project.version as String
            vcsTag = project.version as String
            released = Date().toString()
        })
    })
}
