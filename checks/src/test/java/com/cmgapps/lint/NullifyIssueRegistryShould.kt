/*
 * Copyright (c) 2020. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.cmgapps.lint

import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import com.android.tools.lint.detector.api.CURRENT_API
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.hasProperty
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Test

class NullifyIssueRegistryShould {

    private lateinit var registry: NullifyIssueRegistry

    @Suppress("CheckResult")
    @Before
    @Throws(Exception::class)
    fun setUp() {
        lint()
        registry = NullifyIssueRegistry()
    }

    @Test
    fun `check ISSUE_METHOD and ISSUE_FIELD`() {
        assertThat(
            registry.issues,
            containsInAnyOrder(
                hasProperty("id", `is`("MissingNullifyMethodAnnotation")),
                hasProperty("id", `is`("MissingNullifyFieldAnnotation")),
            ),
        )
    }

    @Test
    fun `use CURRENT api`() {
        assertThat(registry.api, `is`(CURRENT_API))
    }
}
