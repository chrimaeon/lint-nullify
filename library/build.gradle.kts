/*
 * Copyright (c) 2021. Christian Grach <christian.grach@cmgapps.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    id("com.android.library")
    `maven-publish`
    signing
}

android {
    compileSdkVersion(30)
    defaultConfig {
        minSdkVersion(15)
        targetSdkVersion(30)
    }

    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_1_8)
        targetCompatibility(JavaVersion.VERSION_1_8)
    }

    buildFeatures {
        buildConfig = false
    }

}

val projectGroup: String by project
group = projectGroup
val projectVersion: String by project
version = projectVersion

val scmUrl = "https://github.com/chrimaeon/lint-nullify"

tasks {

    val checksProject = project(":checks")

    register<Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        from(checksProject.sourceSets["main"].allSource)
    }

    register<Jar>("javadocJar") {
        archiveClassifier.set("javadoc")
        from(checksProject.tasks["dokkaJavadoc"])
    }
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("libraryMaven") {
                from(components["release"])

                artifact(tasks["sourcesJar"])
                artifact(tasks["javadocJar"])

                val projectArtifactId: String by project
                artifactId = projectArtifactId

                pom {
                    val pomName: String by project
                    val pomDescription: String by project
                    name.set(pomName)
                    description.set(pomDescription)
                    url.set(scmUrl)

                    issueManagement {
                        url.set("https://github.com/chrimaeon/lint-nullify/issues")
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
                val releaseUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
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
