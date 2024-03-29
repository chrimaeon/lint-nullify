/*
 * Copyright (c) 2021. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    id("com.android.library")
    `maven-publish`
    signing
}

android {
    namespace = "com.cmgapps.lint.nullify"
    compileSdk = 34
    defaultConfig {
        minSdk = 15
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = false
    }

    publishing {
        singleVariant("release")
    }
}

val projectGroup: String by project
group = projectGroup
val projectVersion: String by project
version = projectVersion

val scmUrl = "https://github.com/chrimaeon/lint-nullify"

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(projectDir.resolve("README.md"))
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    from(projectDir.resolve("README.md"))
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("libraryMaven") {
                from(components["release"])

                artifact(sourcesJar)
                artifact(javadocJar)

                val projectArtifactId: String by project
                artifactId = projectArtifactId

                pom {
                    val pomName: String by project
                    val pomDescription: String by project
                    name.set(pomName)
                    description.set(pomDescription)
                    url.set(scmUrl)

                    issueManagement {
                        val feedbackUrl: String by project
                        url.set(feedbackUrl)
                        system.set("github")
                    }

                    licenses {
                        license {
                            name.set("Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
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

        repositories {
            maven {
                name = "sonatype"
                val releaseUrl =
                    uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                val snapshotUrl = uri("https://oss.sonatype.org/content/repositories/snapshots/")
                url = if (projectVersion.endsWith("SNAPSHOT")) snapshotUrl else releaseUrl

                val username by credentials()
                val password by credentials()

                credentials {
                    this.username = username
                    this.password = password
                }
            }
        }
    }

    signing {
        sign(publishing.publications["libraryMaven"])
    }
}

dependencies {
    lintPublish(project(":checks"))
}
