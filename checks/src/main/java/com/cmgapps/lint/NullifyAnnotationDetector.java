package com.cmgapps.lint;

import com.android.SdkConstants;
import com.android.annotations.NonNull;
import com.android.tools.lint.client.api.UElementHandler;
import com.android.tools.lint.detector.api.*;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiTypeElement;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.uast.*;

import java.util.Arrays;
import java.util.List;

public class NullifyAnnotationDetector extends Detector implements SourceCodeScanner {

    @SuppressWarnings("WeakerAccess")
    public static final Issue ISSUE = Issue.create(
        "MissingNullifyAnnotation",

        // Title -- shown in the IDE's preference dialog, as category headers in the
        // Analysis results window, etc
        "Nullable/NonNull check",

        // Full explanation of the issue; you can use some markdown markup such as
        // `monospace`, *italic*, and **bold**.
        "Checks for missing `@NonNull/@Nullable Annotations",
        Category.CORRECTNESS,
        6,
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
            PsiModifierList modifierList = field.getModifierList();
            if (modifierList == null || isPrimitive(field.getTypeElement()) || isEnumConstant(field)) {
                return;
            }

            boolean hasNullifyAnnotaion = modifierList.findAnnotation(ANNOTATION_NON_NULL) != null ||
                modifierList.findAnnotation(ANNOTATION_NULLABLE) != null;

            if (!hasNullifyAnnotaion) {
                mContext.report(ISSUE, field, mContext.getLocation(field), MISSING_ANNOTATION);
            }
        }


        @Override
        public void visitMethod(@NonNull UMethod method) {
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

            PsiModifierList modifierList = method.getModifierList();
            boolean hasNullifyAnnotaion = modifierList.findAnnotation(ANNOTATION_NON_NULL) != null ||
                modifierList.findAnnotation(ANNOTATION_NULLABLE) != null;

            if (!hasNullifyAnnotaion) {
                mContext.report(ISSUE, (UElement) method, mContext.getLocation((UElement) method), MISSING_RETURN_ANNOTATION);
            }
        }

        private void handleParameter(UParameter parameter) {
            PsiModifierList modifierList = parameter.getModifierList();
            if (modifierList == null || isPrimitive(parameter.getTypeElement())) {
                return;
            }

            boolean hasNullifyAnnotaion = modifierList.findAnnotation(ANNOTATION_NON_NULL) != null ||
                modifierList.findAnnotation(ANNOTATION_NULLABLE) != null;

            if (!hasNullifyAnnotaion) {
                mContext.report(ISSUE, (UElement) parameter, mContext.getLocation((UElement) parameter), MISSING_ANNOTATION);
            }
        }

        private boolean isPrimitive(@Nullable PsiTypeElement psiTypeElement) {
            return psiTypeElement != null
                && psiTypeElement.getInnermostComponentReferenceElement() == null;
        }

        private boolean isEnumConstant(UField field) {
            return field instanceof UEnumConstant;
        }
    }
}
