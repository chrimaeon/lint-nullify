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
import com.android.annotations.NonNull;
import com.android.tools.lint.client.api.UElementHandler;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.LintFix;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.android.tools.lint.detector.api.SourceCodeScanner;
import com.intellij.psi.PsiTypeElement;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.uast.UAnnotationMethod;
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UEnumConstant;
import org.jetbrains.uast.UField;
import org.jetbrains.uast.UMethod;
import org.jetbrains.uast.UParameter;

import java.util.Arrays;
import java.util.List;

public class NullifyAnnotationDetector extends Detector implements SourceCodeScanner {

    private static final Issue ISSUE_MEDHOD = Issue.create(
        "MissingNullifyMethodAnnotation",

        // Title -- shown in the IDE's preference dialog, as category headers in the
        // Analysis results window, etc
        "Nullable/NonNull method parameter/return type check",

        // Full explanation of the issue; you can use some markdown markup such as
        // `monospace`, *italic*, and **bold**.
        "Checks for missing `@NonNull/@Nullable` Annotations for method paramters and return types",
        Category.CORRECTNESS,
        4,
        Severity.WARNING,
        new Implementation(
            NullifyAnnotationDetector.class,
            Scope.JAVA_FILE_SCOPE));

    @SuppressWarnings("WeakerAccess")
    private static final Issue ISSUE_FIELD = Issue.create(
        "MissingNullifyFieldAnnotation",

        // Title -- shown in the IDE's preference dialog, as category headers in the
        // Analysis results window, etc
        "Nullable/NonNull field check",

        // Full explanation of the issue; you can use some markdown markup such as
        // `monospace`, *italic*, and **bold**.
        "Checks for missing `@NonNull/@Nullable` Annotations for fields",
        Category.CORRECTNESS,
        4,
        Severity.WARNING,
        new Implementation(
            NullifyAnnotationDetector.class,
            Scope.JAVA_FILE_SCOPE));


    @NonNull
    private static final String ANNOTATION_NON_NULL = SdkConstants.SUPPORT_ANNOTATIONS_PREFIX
        + "NonNull";

    @NonNull
    private static final String ANNOTATION_NULLABLE = SdkConstants.SUPPORT_ANNOTATIONS_PREFIX
        + "Nullable";

    static Issue[] getIssues() {
        return new Issue[]{ISSUE_MEDHOD, ISSUE_FIELD};
    }

    @Nullable
    @Override
    public List<Class<? extends UElement>> getApplicableUastTypes() {
        return Arrays.asList(UMethod.class, UField.class);
    }

    @Nullable
    @Override
    public UElementHandler createUastHandler(JavaContext context) {
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
        public void visitField(@NonNull UField field) {

            if (isPrimitive(field.getTypeElement()) || isEnumConstant(field) || isConstant(field) || isInitializedFinalField(field)) {
                return;
            }

            boolean hasNullifyAnnotaion = field.findAnnotation(ANNOTATION_NON_NULL) != null ||
                field.findAnnotation(ANNOTATION_NULLABLE) != null;

            if (!hasNullifyAnnotaion) {
                mContext.report(ISSUE_FIELD, field, mContext.getLocation(field), MISSING_ANNOTATION, quickFixAnnotation(field));
            }
        }

        @Override
        public void visitMethod(@NonNull UMethod method) {
            if (method instanceof UAnnotationMethod) {
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
            if (method.isConstructor() || isPrimitive(method.getReturnTypeElement())) {
                return;

            }

            boolean hasNullifyAnnotaion = method.findAnnotation(ANNOTATION_NON_NULL) != null ||
                method.findAnnotation(ANNOTATION_NULLABLE) != null;

            if (!hasNullifyAnnotaion) {
                mContext.report(ISSUE_MEDHOD, (UElement) method, mContext.getLocation((UElement) method), MISSING_RETURN_ANNOTATION, quickFixAnnotation(method));
            }
        }

        private void handleParameter(UParameter parameter) {
            if (isPrimitive(parameter.getTypeElement())) {
                return;
            }

            boolean hasNullifyAnnotaion = parameter.findAnnotation(ANNOTATION_NON_NULL) != null ||
                parameter.findAnnotation(ANNOTATION_NULLABLE) != null;

            if (!hasNullifyAnnotaion) {
                mContext.report(ISSUE_MEDHOD, (UElement) parameter, mContext.getLocation((UElement) parameter), MISSING_ANNOTATION, quickFixAnnotation(parameter));
            }
        }

        private boolean isPrimitive(@Nullable PsiTypeElement psiTypeElement) {
            return psiTypeElement != null
                && psiTypeElement.getInnermostComponentReferenceElement() == null;
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

            LintFix.GroupBuilder group = fix().group();
            group.add(fix().name("Add @NonNull").replace().text(sourceString).shortenNames().reformat(true).with(nonNullFixString).build());
            group.add(fix().name("Add @Nullable").replace().text(sourceString).shortenNames().reformat(true).with(nullableFixString).build());

            return group.build();
        }

        private boolean isInitializedFinalField(UField field) {
            return field.isFinal() && field.asSourceString().indexOf('=') > -1;
        }
    }
}
