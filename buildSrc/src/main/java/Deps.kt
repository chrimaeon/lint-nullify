/*
 * Copyright (c) 2020. Christian Grach <christian.grach@cmgapps.com>
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

object Deps {
    const val ANDROID_TESTUTILS = "com.android.tools:testutils:" + Version.LINT
    const val AUTO_SERVICE = "com.google.auto.service:auto-service:" + Version.AUTO_SERVICE
    const val HAMCREST = "org.hamcrest:hamcrest:" + Version.HAMCREST
    const val JUNIT = "junit:junit:" + Version.JUNIT
    const val KTLINT = "com.pinterest:ktlint:" + Version.KTLINT
    const val LINT = "com.android.tools.lint:lint:" + Version.LINT
    const val LINT_API = "com.android.tools.lint:lint-api:" + Version.LINT
    const val LINT_CHECKS = "com.android.tools.lint:lint-checks:" + Version.LINT
    const val LINT_TEST = "com.android.tools.lint:lint-tests:" + Version.LINT
}

object Version {

    const val KOTLIN = "1.3.72"

    const val BINTRAY_PLUGIN = "1.8.5"
    const val VERSIONS_PLUGIN = "0.28.0"

    internal const val AUTO_SERVICE = "1.0-rc7"
    internal const val HAMCREST = "2.2"
    internal const val JUNIT = "4.13"
    internal const val KTLINT = "0.36.0"
    internal const val LINT = "26.6.3"
}