package com.az.gitember.handler;

import com.az.gitember.ui.StatusBar;
import org.eclipse.jgit.lib.ProgressMonitor;

/**
 * JGit {@link ProgressMonitor} that drives the {@link StatusBar} progress bar.
 * <p>
 * When JGit reports a task with a known total, the progress bar shows
 * determinate progress via {@link StatusBar#setProgress(int, int)}.
 * For tasks with an unknown total (UNKNOWN == 0) it falls back to the
 * indeterminate spinner via {@link StatusBar#showProgress(boolean)}.
 */
public class StatusBarProgressMonitor implements ProgressMonitor {

    private final StatusBar statusBar;

    private int totalWork;
    private int completed;

    public StatusBarProgressMonitor(StatusBar statusBar) {
        this.statusBar = statusBar;
    }

    @Override
    public void start(int totalTasks) {
        statusBar.showProgress(true);
    }

    @Override
    public void beginTask(String title, int totalWork) {
        this.totalWork = totalWork;
        this.completed = 0;
        if (title != null && !title.isBlank()) {
            statusBar.setStatus(title);
        }
        if (totalWork > 0 && totalWork != UNKNOWN) {
            statusBar.setProgress(0, totalWork);
        } else {
            statusBar.showProgress(true);
        }
    }

    @Override
    public void update(int steps) {
        completed += steps;
        if (totalWork > 0 && totalWork != UNKNOWN) {
            statusBar.setProgress(completed, totalWork);
        }
    }

    @Override
    public void endTask() {
        if (totalWork > 0 && totalWork != UNKNOWN) {
            statusBar.setProgress(totalWork, totalWork);
        }
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public void showDuration(boolean enabled) {
        // not supported
    }
}
