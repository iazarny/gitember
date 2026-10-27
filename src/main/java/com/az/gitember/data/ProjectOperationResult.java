package com.az.gitember.data;

import org.apache.commons.lang3.ObjectUtils;

import java.io.File;

/**
 * Outcome of a remote operation (push / pull / fetch) performed against a single
 * {@link Project} of a workspace. Exactly one of {@link #getResult()} (success) or
 * {@link #getError()} (failure) is meaningful, distinguished by {@link #isSuccess()}.
 *
 * @param <T> operation-specific payload: the server message {@code String} for push,
 *            a {@link PullOperationResult} for pull, {@code Void} for fetch.
 */
public class ProjectOperationResult<T> {

    private final Project project;
    private final String remoteUrl;
    private final T result;
    private final Exception error;

    public ProjectOperationResult(Project project, String remoteUrl, T result, Exception error) {
        this.project = project;
        this.remoteUrl = remoteUrl;
        this.result = result;
        this.error = error;
    }

    public static <T> ProjectOperationResult<T> ok(Project project, String remoteUrl, T result) {
        return new ProjectOperationResult<>(project, remoteUrl, result, null);
    }

    public static <T> ProjectOperationResult<T> failed(Project project, Exception error) {
        return new ProjectOperationResult<>(project, null, null, error);
    }

    public Project getProject() {
        return project;
    }

    /** Short, human-readable repository name derived from the project home folder. */
    public String getProjectName() {
        return new File(ObjectUtils.getIfNull(
                project != null ? project.getProjectHomeFolder() : "", "")).getName();
    }

    public String getRemoteUrl() {
        return remoteUrl;
    }

    public T getResult() {
        return result;
    }

    public Exception getError() {
        return error;
    }

    public boolean isSuccess() {
        return error == null;
    }
}
