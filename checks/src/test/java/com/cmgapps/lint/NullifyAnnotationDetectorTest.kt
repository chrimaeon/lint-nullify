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

import com.android.tools.lint.checks.infrastructure.TestFiles.java
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

class NullifyAnnotationDetectorTest {

    @Test
    fun missingConstructorParameterAnnotation() {
        lint()
            .files(
                java("""
                    |package test.pkg;
                    |public class Test {
                    |    public Test(int myInt, String myString){}
                    |}""".trimMargin()
                )
            )
            .issues(*NullifyAnnotationDetector.getIssues())
            .run()
            .expect("""
                |src/test/pkg/Test.java:3: Warning: Missing @NonNull or @Nullable [MissingNullifyMethodAnnotation]
                |    public Test(int myInt, String myString){}
                |                           ~~~~~~~~~~~~~~~
                |0 errors, 1 warnings""".trimMargin()
            )
            .expectFixDiffs("""
                |Fix for src/test/pkg/Test.java line 2: Add @NonNull:
                |@@ -3 +3
                |-     public Test(int myInt, String myString){}
                |+     public Test(int myInt, @NonNull String myString){}
                |Fix for src/test/pkg/Test.java line 2: Add @Nullable:
                |@@ -3 +3
                |-     public Test(int myInt, String myString){}
                |+     public Test(int myInt, @Nullable String myString){}""".trimMargin()
            )
    }

    @Test
    fun correctConstructorParameterAnnotation() {
        lint()
            .files(
                java("""
                    |package test.pkg;
                    |public class Test {
                    |    public Test(int myInt, @android.support.annotation.Nullable String myString){}
                    |}""".trimMargin()
                )
            )
            .issues(*NullifyAnnotationDetector.getIssues())
            .run()
            .expect("No warnings.")
    }

    @Test
    fun missingFieldAnnotation() {
        lint()
            .files(
                java("""
                    |package test.pkg;
                    |public class Test {
                    |    private String myString;
                    |    private int myInt;
                    |    private static final String constString = "Test";
                    |}""".trimMargin()
                )
            )
            .issues(*NullifyAnnotationDetector.getIssues())
            .run()
            .expect("""
                |src/test/pkg/Test.java:3: Warning: Missing @NonNull or @Nullable [MissingNullifyFieldAnnotation]
                |    private String myString;
                |    ~~~~~~~~~~~~~~~~~~~~~~~~
                |0 errors, 1 warnings""".trimMargin()
            )
            .expectFixDiffs("""
                |Fix for src/test/pkg/Test.java line 2: Add @NonNull:
                |@@ -3 +3
                |-     private String myString;
                |+     @NonNull private String myString;
                |Fix for src/test/pkg/Test.java line 2: Add @Nullable:
                |@@ -3 +3
                |-     private String myString;
                |+     @Nullable private String myString;""".trimMargin()
            )
    }

    @Test
    fun correctFieldAnnotation() {
        lint()
            .files(
                java("""
                    |package test.pkg;
                    |public class Test {
                    |    @android.support.annotation.NonNull private String myString;
                    |    private int myInt;
                    |    private static final String constString = "Test";
                    |}""".trimMargin()
                )
            )
            .issues(*NullifyAnnotationDetector.getIssues())
            .run()
            .expect("No warnings.")
    }

    @Test
    fun correctFieldAndroidXAnnotation() {
        lint()
            .files(
                java("""
                    |package test.pkg;
                    |public class Test {
                    |    @androidx.annotation.Nullable private String myString;
                    |    private int myInt;
                    |    private static final String constString = "Test";
                    |}""".trimMargin()
                )
            )
            .issues(*NullifyAnnotationDetector.getIssues())
            .run()
            .expect("No warnings.")
    }

    @Test
    fun missingMethodAnnotation() {
        lint()
            .files(
                java("""
                    |package test.pkg;
                    |public class Test {
                    |    public void foo(int myInt, String myString){};
                    |}""".trimMargin()
                )
            )
            .issues(*NullifyAnnotationDetector.getIssues())
            .run()
            .expect("""
                |src/test/pkg/Test.java:3: Warning: Missing @NonNull or @Nullable [MissingNullifyMethodAnnotation]
                |    public void foo(int myInt, String myString){};
                |                               ~~~~~~~~~~~~~~~
                |0 errors, 1 warnings""".trimMargin()
            )
            .expectFixDiffs("""
                |Fix for src/test/pkg/Test.java line 2: Add @NonNull:
                |@@ -3 +3
                |-     public void foo(int myInt, String myString){};
                |+     public void foo(int myInt, @NonNull String myString){};
                |Fix for src/test/pkg/Test.java line 2: Add @Nullable:
                |@@ -3 +3
                |-     public void foo(int myInt, String myString){};
                |+     public void foo(int myInt, @Nullable String myString){};
                """.trimMargin())
    }

    @Test
    fun correctMethodAnnotation() {
        lint()
            .files(
                java("""
                    |package test.pkg;
                    |public class Test {
                    |    public void foo(int myInt, @android.support.annotation.NonNull String myString){};
                    |}""".trimMargin()
                )
            )
            .issues(*NullifyAnnotationDetector.getIssues())
            .run()
            .expect("No warnings.")
    }

    @Test
    fun correctMethodAndroidXAnnotation() {
        lint()
            .files(
                java("""
                    |package test.pkg;
                    |public class Test {
                    |    public void foo(int myInt, @androidx.annotation.NonNull String myString){};
                    |}""".trimMargin()
                )
            )
            .issues(*NullifyAnnotationDetector.getIssues())
            .run()
            .expect("No warnings.")
    }

    @Test
    fun missingMethodArrayAnnotation() {
        lint()
            .files(
                java("""
                    |package test.pkg;
                    |public class Test {
                    |    public void foo(int[] myInt, String[] myString){};
                    |}""".trimMargin()
                )
            )
            .issues(*NullifyAnnotationDetector.getIssues())
            .run()
            .expect("""
                |src/test/pkg/Test.java:3: Warning: Missing @NonNull or @Nullable [MissingNullifyMethodAnnotation]
                |    public void foo(int[] myInt, String[] myString){};
                |                                 ~~~~~~~~~~~~~~~~~
                |0 errors, 1 warnings""".trimMargin()
            )
            .expectFixDiffs("""
                |Fix for src/test/pkg/Test.java line 2: Add @NonNull:
                |@@ -3 +3
                |-     public void foo(int[] myInt, String[] myString){};
                |+     public void foo(int[] myInt, @NonNull String[] myString){};
                |Fix for src/test/pkg/Test.java line 2: Add @Nullable:
                |@@ -3 +3
                |-     public void foo(int[] myInt, String[] myString){};
                |+     public void foo(int[] myInt, @Nullable String[] myString){};
                """.trimMargin()
            )
    }

    @Test
    fun correctMethodArrayAnnotation() {
        lint()
            .files(
                java("""
                        |package test.pkg;
                        |public class Test {
                        |    public void foo(int[] myInt, @android.support.annotation.Nullable String[] myString){};
                        |}""".trimMargin()
                )
            )
            .issues(*NullifyAnnotationDetector.getIssues())
            .run()
            .expect("No warnings.")
    }

    @Test
    fun missingReturnAnnotation() {
        lint()
            .files(
                java("""
                    |package test.pkg;
                    |public class Test {
                    |    public int foo(){};
                    |    public String foo(){};
                    |}""".trimMargin()
                )
            )
            .issues(*NullifyAnnotationDetector.getIssues())
            .run()
            .expect("""
                |src/test/pkg/Test.java:4: Warning: Return type is missing @NonNull or @Nullable [MissingNullifyMethodAnnotation]
                |    public String foo(){};
                |    ~~~~~~~~~~~~~~~~~~~~~
                |0 errors, 1 warnings""".trimMargin()
            )
            .expectFixDiffs("""
                |Fix for src/test/pkg/Test.java line 3: Add @NonNull:
                |@@ -4 +4
                |-     public String foo(){};
                |+     @NonNull public String foo(){};
                |Fix for src/test/pkg/Test.java line 3: Add @Nullable:
                |@@ -4 +4
                |-     public String foo(){};
                |+     @Nullable public String foo(){};
                """.trimMargin()
            )
    }

    @Test
    fun correctReturnAnnotation() {
        lint()
            .files(
                java("""
                    |package test.pkg;
                    |public class Test {
                    |    public int foo(){};
                    |    @android.support.annotation.NonNull
                    |    public String foo(){};
                    |}""".trimMargin()
                )
            )
            .issues(*NullifyAnnotationDetector.getIssues())
            .run()
            .expect("No warnings.")
    }

    @Test
    fun uninitializedFinalField() {
        lint()
            .files(
                java("""
                    |package test.pkg;
                    |public class Test {
                    |    public final Foo foo;
                    |}""".trimMargin()
                )
            )
            .issues(*NullifyAnnotationDetector.getIssues())
            .run()
            .expect("""
                |src/test/pkg/Test.java:3: Warning: Missing @NonNull or @Nullable [MissingNullifyFieldAnnotation]
                |    public final Foo foo;
                |    ~~~~~~~~~~~~~~~~~~~~~
                |0 errors, 1 warnings""".trimMargin()
            )
            .expectFixDiffs("""
                |Fix for src/test/pkg/Test.java line 2: Add @NonNull:
                |@@ -3 +3
                |-     public final Foo foo;
                |+     @NonNull public final Foo foo;
                |Fix for src/test/pkg/Test.java line 2: Add @Nullable:
                |@@ -3 +3
                |-     public final Foo foo;
                |+     @Nullable public final Foo foo;
                """.trimMargin()
            )
    }

    @Test
    fun initializedFinalField() {
        lint()
            .files(
                java("""
                    |package test.pkg;
                    |public class Test {
                    |    public final Foo foo = new Foo();
                    |}""".trimMargin()
                )
            )
            .issues(*NullifyAnnotationDetector.getIssues())
            .run()
            .expect("No warnings.")
    }

    @Test
    fun testAnnotationDefinition() {
        lint()
            .files(
                java("""
                |package test.pkg;
                |public @interface Test {
                |   String value();
                |}""".trimMargin()
                ))
            .issues(*NullifyAnnotationDetector.getIssues())
            .run()
            .expect("No warnings.")
    }
}