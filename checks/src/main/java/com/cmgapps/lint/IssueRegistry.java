package com.cmgapps.lint;

import com.android.tools.lint.detector.api.Issue;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class IssueRegistry extends com.android.tools.lint.client.api.IssueRegistry {
    @NotNull
    @Override
    public List<Issue> getIssues() {
        return Collections.singletonList(NullifyAnnotationDetector.ISSUE);
    }
}
