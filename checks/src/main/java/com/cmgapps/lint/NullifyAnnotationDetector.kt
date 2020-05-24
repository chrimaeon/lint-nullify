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

package com.cmgapps.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category.Companion.CORRECTNESS
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.LintFix
import com.android.tools.lint.detector.api.Scope.Companion.JAVA_FILE_SCOPE
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.android.tools.lint.detector.api.isKotlin
import com.intellij.psi.util.TypeConversionUtil.isPrimitiveAndNotNull
import org.jetbrains.uast.UAnnotated
import org.jetbrains.uast.UAnnotationMethod
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UEnumConstant
import org.jetbrains.uast.UField
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.UParameter

@Suppress("UnstableApiUsage")
class NullifyAnnotationDetector : Detector(), SourceCodeScanner {

    override fun getApplicableUastTypes(): List<Class<out UElement>>? = listOf(UMethod::class.java, UField::class.java)

    @Suppress("Deprecation")
    override fun createUastHandler(context: JavaContext): UElementHandler? = if (!isKotlin(context.uastFile?.psi)) {
        NullifyAnnotationHandler(context)
    } else null

    private class NullifyAnnotationHandler(private val context: JavaContext) : UElementHandler() {
        override fun visitField(node: UField) {
            if (isPrimitiveAndNotNull(node.type) ||
                isEnumConstant(node) ||
                isConstant(node) ||
                isInitializedFinalField(node)
            ) return

            if (hasNoNullifyAnnotation(node)) {
                context.report(
                    ISSUE_FIELD,
                    node,
                    context.getLocation(node),
                    MISSING_ANNOTATION,
                    quickFixAnnotation(node)
                )
            }
        }

        override fun visitMethod(node: UMethod) {
            if (node is UAnnotationMethod) return

            handleMethodsParameterList(node.uastParameters)
            handleMethodReturnType(node)
        }

        private fun handleMethodsParameterList(parameterList: List<UParameter>) {
            parameterList.forEach {
                handleParameter(it)
            }
        }

        private fun handleMethodReturnType(method: UMethod) {
            if (method.isConstructor || isPrimitiveAndNotNull(method.returnType)) return

            if (hasNoNullifyAnnotation(method)) {
                context.report(
                    ISSUE_METHOD,
                    method as UElement,
                    context.getLocation(method),
                    MISSING_RETURN_ANNOTATION,
                    quickFixAnnotation(method)
                )
            }
        }

        private fun handleParameter(parameter: UParameter) {
            if (isPrimitiveAndNotNull(parameter.type)) return

            if (hasNoNullifyAnnotation(parameter)) {
                context.report(
                    ISSUE_METHOD,
                    parameter as UElement,
                    context.getLocation((parameter as UElement)),
                    MISSING_ANNOTATION,
                    quickFixAnnotation(parameter)
                )
            }
        }

        private fun hasNoNullifyAnnotation(annotated: UAnnotated): Boolean {
            return context.evaluator.getAllAnnotations(annotated, false).none { annotation ->
                val name = annotation.qualifiedName ?: return@none false

                name.endsWith("Nullable") ||
                    name.endsWith("NonNull") ||
                    name.endsWith("NotNull") ||
                    name.endsWith("Nonnull")
            }
        }

        private companion object {
            private const val MISSING_ANNOTATION = "Missing @NonNull or @Nullable"
            private const val MISSING_RETURN_ANNOTATION = "Return type is missing @NonNull or @Nullable"

            private fun isEnumConstant(field: UField): Boolean {
                return field is UEnumConstant
            }

            private fun isConstant(field: UField): Boolean {
                return field.isStatic && field.isFinal
            }

            private fun isInitializedFinalField(field: UField): Boolean {
                return field.isFinal && field.uastInitializer != null
            }

            private fun quickFixAnnotation(element: UElement): LintFix {
                val sourceString = element.asSourceString()
                val nonNullFixString = "@NonNull $sourceString"
                val nullableFixString = "@Nullable $sourceString"

                return LintFix.create().group()
                    .add(
                        LintFix.create()
                            .name("Annotate @NonNull")
                            .replace()
                            .text(sourceString)
                            .shortenNames()
                            .reformat(true)
                            .with(nonNullFixString)
                            .build()
                    )
                    .add(
                        LintFix.create()
                            .name("Annotate @Nullable")
                            .replace()
                            .text(sourceString)
                            .shortenNames()
                            .reformat(true)
                            .with(nullableFixString)
                            .build()
                    )
                    .build()
            }
        }
    }

    companion object {
        private val ISSUE_METHOD: Issue = Issue.create(
            id = "MissingNullifyMethodAnnotation",
            briefDescription = "Nullable/NonNull method parameter/return type check",
            explanation = "Checks for missing `@NonNull/@Nullable` Annotations for method parameters and return types",
            category = CORRECTNESS,
            priority = 4,
            severity = Severity.WARNING,
            implementation = Implementation(
                NullifyAnnotationDetector::class.java,
                JAVA_FILE_SCOPE
            )
        )
        private val ISSUE_FIELD: Issue = Issue.create(
            id = "MissingNullifyFieldAnnotation",
            briefDescription = "Nullable/NonNull field check",
            explanation = "Checks for missing `@NonNull/@Nullable` Annotations for fields",
            category = CORRECTNESS,
            priority = 4,
            severity = Severity.WARNING,
            implementation = Implementation(
                NullifyAnnotationDetector::class.java,
                JAVA_FILE_SCOPE
            )
        )

        val issues = arrayOf(ISSUE_METHOD, ISSUE_FIELD)
    }
}
