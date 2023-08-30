/*
* Copyright (c) 2018. Christian Grach <christian.grach@cmgapps.com>
*
* SPDX-License-Identifier: Apache-2.0
*/

@file:Suppress("UnstableApiUsage")

rootProject.name = "lint-nullify"

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

include(
    ":checks",
    ":library",
)
