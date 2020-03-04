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

package com.cmgapps.lint;

import com.android.SdkConstants;
import com.android.tools.lint.client.api.UElementHandler;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Lint;
import com.android.tools.lint.detector.api.LintFix;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.android.tools.lint.detector.api.SourceCodeScanner;
import com.intellij.psi.util.TypeConversionUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.uast.UAnnotated;
import org.jetbrains.uast.UAnnotationMethod;
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UEnumConstant;
import org.jetbrains.uast.UField;
import org.jetbrains.uast.UMethod;
import org.jetbrains.uast.UParameter;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class NullifyAnnotationDetector extends Detector implements SourceCodeScanner {

    private static final Issue ISSUE_METHOD = Issue.create(
        "MissingNullifyMethodAnnotation",
        "Nullable/NonNull method parameter/return type check",
        "Checks for missing `@NonNull/@Nullable` Annotations for method parameters and return types",
        Category.CORRECTNESS,
        4,
        Severity.WARNING,
        new Implementation(
            NullifyAnnotationDetector.class,
            Scope.JAVA_FILE_SCOPE));

    private static final Issue ISSUE_FIELD = Issue.create(
        "MissingNullifyFieldAnnotation",
        "Nullable/NonNull field check",
        "Checks for missing `@NonNull/@Nullable` Annotations for fields",
        Category.CORRECTNESS,
        4,
        Severity.WARNING,
        new Implementation(
            NullifyAnnotationDetector.class,
            Scope.JAVA_FILE_SCOPE));

    private static final String SUPPORT_ANNOTATION_NON_NULL = SdkConstants.SUPPORT_ANNOTATIONS_PREFIX.oldName()
        + "NonNull";
    private static final String ANDROIDX_ANNOTATION_NON_NULL = SdkConstants.SUPPORT_ANNOTATIONS_PREFIX.newName()
        + "NonNull";
    private static final String JETBRAINS_NOTNULL = "org.jetbrains.annotations.NotNull";
    private static final String JAVAX_NONNULL = "javax.annotation.Nonnull";

    private static final String SUPPORT_ANNOTATION_NULLABLE = SdkConstants.SUPPORT_ANNOTATIONS_PREFIX.oldName()
        + "Nullable";
    private static final String ANDROIDX_ANNOTATION_NULLABLE = SdkConstants.SUPPORT_ANNOTATIONS_PREFIX.newName()
        + "Nullable";
    private static final String JETBRAINS_NULLABLE = "org.jetbrains.annotations.Nullable";
    private static final String JAVAX_NULLABLE = "javax.annotation.Nullable";

    private static final String[] NULLIFY_ANNOTATIONS = new String[]{
        SUPPORT_ANNOTATION_NON_NULL,
        ANDROIDX_ANNOTATION_NON_NULL,
        JETBRAINS_NOTNULL,
        JAVAX_NONNULL,
        SUPPORT_ANNOTATION_NULLABLE,
        ANDROIDX_ANNOTATION_NULLABLE,
        JETBRAINS_NULLABLE,
        JAVAX_NULLABLE
    };

    static Issue[] getIssues() {
        return new Issue[]{ISSUE_METHOD, ISSUE_FIELD};
    }

    @Nullable
    @Override
    public List<Class<? extends UElement>> getApplicableUastTypes() {
        return Arrays.asList(UMethod.class, UField.class);
    }

    @Nullable
    @Override
    public UElementHandler createUastHandler(@NotNull JavaContext context) {
        return new NullifyAnnotationHandler(context);
    }

    private class NullifyAnnotationHandler extends UElementHandler {

        private static final String MISSING_ANNOTATION = "Missing @NonNull or @Nullable";
        private static final String MISSING_RETURN_ANNOTATION = "Return type is missing @NonNull or @Nullable";

        private final JavaContext mContext;

        private NullifyAnnotationHandler(JavaContext context) {
            mContext = context;
        }

        @Override
        public void visitField(@NotNull UField field) {

            if (Lint.isKotlin(field.getLanguage()) || TypeConversionUtil.isPrimitiveAndNotNull(field.getType()) ||
                isEnumConstant(field) || isConstant(field) || isInitializedFinalField(field)) {
                return;
            }

            if (hasNoNullifyAnnotation(field)) {
                mContext.report(ISSUE_FIELD, field, mContext.getLocation(field), MISSING_ANNOTATION, quickFixAnnotation(field));
            }
        }

        @Override
        public void visitMethod(@NotNull UMethod method) {
            if (Lint.isKotlin(method.getLanguage()) || method instanceof UAnnotationMethod) {
                return;
            }

            handleMethodsParameterList(method.getUastParameters());
            handleMethodReturnType(method);
        }

        private void handleMethodsParameterList(List<UParameter> parameterList) {
            for (UParameter parameter : parameterList) {
                handleParameter(parameter);
            }
        }

        private void handleMethodReturnType(UMethod method) {
            if (method.isConstructor() || TypeConversionUtil.isPrimitiveAndNotNull(method.getReturnType())) {
                return;
            }

            if (hasNoNullifyAnnotation(method)) {
                mContext.report(ISSUE_METHOD, (UElement) method, mContext.getLocation(method), MISSING_RETURN_ANNOTATION, quickFixAnnotation(method));
            }
        }

        private void handleParameter(UParameter parameter) {
            if (TypeConversionUtil.isPrimitiveAndNotNull(parameter.getType())) {
                return;
            }

            if (hasNoNullifyAnnotation(parameter)) {
                mContext.report(ISSUE_METHOD, (UElement) parameter, mContext.getLocation((UElement) parameter), MISSING_ANNOTATION, quickFixAnnotation(parameter));
            }
        }

        private boolean isEnumConstant(UField field) {
            return field instanceof UEnumConstant;
        }

        private boolean isConstant(UField field) {
            return field.isStatic() && field.isFinal();
        }

        private LintFix quickFixAnnotation(UElement element) {
            String sourceString = element.asSourceString();
            String nonNullFixString = "@NonNull " + sourceString;
            String nullableFixString = "@Nullable " + sourceString;

            return fix().group()
                .add(fix()
                    .name("Add @NonNull")
                    .replace()
                    .text(sourceString)
                    .shortenNames()
                    .reformat(true)
                    .with(nonNullFixString)
                    .build())
                .add(fix()
                    .name("Add @Nullable")
                    .replace()
                    .text(sourceString)
                    .shortenNames()
                    .reformat(true)
                    .with(nullableFixString)
                    .build())
                .build();
        }

        private boolean isInitializedFinalField(UField field) {
            return field.isFinal() && field.getUastInitializer() != null;
        }

        private boolean hasNoNullifyAnnotation(UAnnotated annotated) {
            for (String annotation : NULLIFY_ANNOTATIONS) {
                if (annotated.findAnnotation(annotation) != null) {
                    return false;
                }
            }

            return true;
        }
    }
}
