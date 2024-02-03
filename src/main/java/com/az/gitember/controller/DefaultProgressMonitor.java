package com.az.gitember.controller;


import org.eclipse.jgit.lib.ProgressMonitor;

import java.util.function.BiConsumer;

/**
 * Created by Igor_Azarny on 03 -Jan - 2017.
 */
public class DefaultProgressMonitor implements ProgressMonitor {

    public enum Strategy {
        Completed,
        Step
    }

    private final BiConsumer<String, Double> percentageCompleteConsumer;

    private int totalWork = Integer.MAX_VALUE;
    private int completed = 0;
    private String title;
    private Strategy strategy = Strategy.Completed;

    public DefaultProgressMonitor(BiConsumer<String, Double> percentageCompleteConsumer) {
        this(percentageCompleteConsumer, Strategy.Completed);
    }

    public DefaultProgressMonitor(BiConsumer<String, Double> percentageCompleteConsumer, Strategy strategy) {
        this.percentageCompleteConsumer = percentageCompleteConsumer;
        this.strategy = strategy;
    }

    @Override
    public void start(int totalTasks) {
        percentageCompleteConsumer.accept(title, 0.0);

    }

    @Override
    public void beginTask(String title, int totalWork) {
        setTotalWork(totalWork);
        this.completed = 0;
        this.title = title;

    }

    @Override
    public void update(int completed) {

        if (Strategy.Completed == strategy) {
            setCompleted(completed);
        } else if (Strategy.Step == strategy) {
            setStep(completed);
        }

    }

    @Override
    public void endTask() {
        percentageCompleteConsumer.accept(title, 1.0);
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    public void setTotalWork(int totalWork) {
        this.totalWork = totalWork;
        if (totalWork > 0) {
            double tw = 1.0 * completed  / totalWork;
            percentageCompleteConsumer.accept(title, tw );
        }
    }

    public void setCompleted(int completed) {
        this.completed = completed;
        if (totalWork > 0) {
            double c = 1.0 * this.completed / totalWork;
            percentageCompleteConsumer.accept(title, c);
        }
    }

    public void setStep(int completed) {
        this.completed += completed;
        if (totalWork > 0) {
            double c = 1.0 * this.completed / totalWork;
            percentageCompleteConsumer.accept(title, c);
        }
    }

    @Override
    public void showDuration(boolean b) {

    }
}
