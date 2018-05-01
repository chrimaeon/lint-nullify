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

import com.android.tools.lint.checks.infrastructure.LintDetectorTest;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Issue;

import java.util.Collections;
import java.util.List;

public class NullifyAnnotationDetectorTest extends LintDetectorTest {

    public void testMissingConstructorParameterAnnotation() {
        lint().files(
            java(
                "package test.pkg;\n" +
                    "public class Test {\n" +
                    "   public Test(int myInt, String myString){}\n" +
                    "}"
            ))
            .run()
            .expect("src/test/pkg/Test.java:3: Warning: Missing @NonNull or @Nullable [MissingNullifyAnnotation]\n" +
                "   public Test(int myInt, String myString){}\n" +
                "                          ~~~~~~~~~~~~~~~\n" +
                "0 errors, 1 warnings")
            .expectFixDiffs("Fix for src/test/pkg/Test.java line 2: Add @NonNull:\n" +
                "@@ -3 +3\n" +
                "-    public Test(int myInt, String myString){}\n" +
                "+    public Test(int myInt, @NonNull String myString){}\n" +
                "Fix for src/test/pkg/Test.java line 2: Add @Nullable:\n" +
                "@@ -3 +3\n" +
                "-    public Test(int myInt, String myString){}\n" +
                "+    public Test(int myInt, @Nullable String myString){}");
    }

    public void testConstructorParameterAnnotation() {
        lint().files(
            java(
                "package test.pkg;\n" +
                    "public class Test {\n" +
                    "   public Test(int myInt, @android.support.annotation.Nullable String myString){}\n" +
                    "}"
            )).run().expect("No warnings.");
    }

    public void testMissingFieldAnnotation() {
        lint().files(
            java(
                "package test.pkg;\n" +
                    "public class Test {\n" +
                    "   private String myString;\n" +
                    "   private int myInt;\n" +
                    "   private static final String constString = \"Test\";\n" +
                    "}"
            ))
            .run()
            .expect("src/test/pkg/Test.java:3: Warning: Missing @NonNull or @Nullable [MissingNullifyAnnotation]\n" +
                "   private String myString;\n" +
                "   ~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "0 errors, 1 warnings")
            .expectFixDiffs("Fix for src/test/pkg/Test.java line 2: Add @NonNull:\n" +
                "@@ -3 +3\n" +
                "-    private String myString;\n" +
                "+    @NonNull private String myString;\n" +
                "Fix for src/test/pkg/Test.java line 2: Add @Nullable:\n" +
                "@@ -3 +3\n" +
                "-    private String myString;\n" +
                "+    @Nullable private String myString;");
    }

    public void testFieldAnnotation() {
        lint().files(
            java(
                "package test.pkg;\n" +
                    "public class Test {\n" +
                    "   @android.support.annotation.NonNull private String myString;\n" +
                    "   private int myInt;\n" +
                    "   private static final String constString = \"Test\";\n" +
                    "}"
            )).run().expect("No warnings.");
    }

    public void testMissingMethodAnnotation() {
        lint().files(
            java(
                "package test.pkg;\n" +
                    "public class Test {\n" +
                    "   public void foo(int myInt, String myString){};\n" +
                    "}"
            ))
            .run()
            .expect("src/test/pkg/Test.java:3: Warning: Missing @NonNull or @Nullable [MissingNullifyAnnotation]\n" +
                "   public void foo(int myInt, String myString){};\n" +
                "                              ~~~~~~~~~~~~~~~\n" +
                "0 errors, 1 warnings")
            .expectFixDiffs("Fix for src/test/pkg/Test.java line 2: Add @NonNull:\n" +
                "@@ -3 +3\n" +
                "-    public void foo(int myInt, String myString){};\n" +
                "+    public void foo(int myInt, @NonNull String myString){};\n" +
                "Fix for src/test/pkg/Test.java line 2: Add @Nullable:\n" +
                "@@ -3 +3\n" +
                "-    public void foo(int myInt, String myString){};\n" +
                "+    public void foo(int myInt, @Nullable String myString){};");
    }

    public void testMethodAnnotation() {
        lint().files(java(
            "package test.pkg;\n" +
                "public class Test {\n" +
                "   public void foo(int myInt, @android.support.annotation.NonNull String myString){};\n" +
                "}"
        )).run().expect("No warnings.");
    }

    public void testMethodArrayMissingAnnotation() {
        lint().files(
            java(
                "package test.pkg;\n" +
                    "public class Test {\n" +
                    "   public void foo(int[] myInt, String[] myString){};\n" +
                    "}"
            ))
            .run()
            .expect("src/test/pkg/Test.java:3: Warning: Missing @NonNull or @Nullable [MissingNullifyAnnotation]\n" +
                "   public void foo(int[] myInt, String[] myString){};\n" +
                "                                ~~~~~~~~~~~~~~~~~\n" +
                "0 errors, 1 warnings")
            .expectFixDiffs("Fix for src/test/pkg/Test.java line 2: Add @NonNull:\n" +
                "@@ -3 +3\n" +
                "-    public void foo(int[] myInt, String[] myString){};\n" +
                "+    public void foo(int[] myInt, @NonNull String[] myString){};\n" +
                "Fix for src/test/pkg/Test.java line 2: Add @Nullable:\n" +
                "@@ -3 +3\n" +
                "-    public void foo(int[] myInt, String[] myString){};\n" +
                "+    public void foo(int[] myInt, @Nullable String[] myString){};");
    }

    public void testMethodArrayAnnotation() {
        lint().files(java(
            "package test.pkg;\n" +
                "public class Test {\n" +
                "   public void foo(int[] myInt, @android.support.annotation.Nullable String[] myString){};\n" +
                "}"
        )).run().expect("No warnings.");
    }

    public void testMissingReturnAnnotation() {
        lint().files(
            java(
                "package test.pkg;\n" +
                    "public class Test {\n" +
                    "   public int foo(){};\n" +
                    "   public String foo(){};\n" +
                    "}"
            ))
            .run()
            .expect("src/test/pkg/Test.java:4: Warning: Return type is missing @NonNull or @Nullable [MissingNullifyAnnotation]\n" +
                "   public String foo(){};\n" +
                "   ~~~~~~~~~~~~~~~~~~~~~\n" +
                "0 errors, 1 warnings")
            .expectFixDiffs("Fix for src/test/pkg/Test.java line 3: Add @NonNull:\n" +
                "@@ -4 +4\n" +
                "-    public String foo(){};\n" +
                "+    @NonNull public String foo(){};\n" +
                "Fix for src/test/pkg/Test.java line 3: Add @Nullable:\n" +
                "@@ -4 +4\n" +
                "-    public String foo(){};\n" +
                "+    @Nullable public String foo(){};");
    }

    public void testReturnAnnotation() {
        lint().files(java(
            "package test.pkg;\n" +
                "public class Test {\n" +
                "   public int foo(){};\n" +
                "   @android.support.annotation.NonNull\n" +
                "   public String foo(){};\n" +
                "}"
        )).run().expect("No warnings.");
    }

    public void testUninitializedFinalField() {
        lint().files(java(
            "package test.pkg;\n" +
                "public class Test {\n" +
                "   public final Foo foo;\n" +
                "}"
        )).run().expect("src/test/pkg/Test.java:3: Warning: Missing @NonNull or @Nullable [MissingNullifyAnnotation]\n" +
            "   public final Foo foo;\n" +
            "   ~~~~~~~~~~~~~~~~~~~~~\n" +
            "0 errors, 1 warnings");
    }

    public void testInitializedFinalField() {
        lint().files(java(
            "package test.pkg;\n" +
                "public class Test {\n" +
                "   public final Foo foo = new Foo();\n" +
                "}"
        )).run().expect("No warnings.");
    }

    public void testAnnotationDefinition() {
        lint().files(java(
            "package test.pkg;\n" +
                "public @interface Test {\n" +
                "   String value();\n" +
                "}"
        )).run().expect("No warnings.");
    }

    @Override
    protected Detector getDetector() {
        return new NullifyAnnotationDetector();
    }

    @Override
    protected List<Issue> getIssues() {
        return Collections.singletonList(NullifyAnnotationDetector.ISSUE);
    }
}
