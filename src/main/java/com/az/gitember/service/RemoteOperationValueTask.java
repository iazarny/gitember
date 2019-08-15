package com.az.gitember.service;

import com.az.gitember.misc.RemoteOperationValue;
import com.az.gitember.scm.impl.git.DefaultProgressMonitor;
import javafx.concurrent.Task;
import org.eclipse.jgit.lib.ProgressMonitor;

import java.util.function.Supplier;

public class RemoteOperationValueTask   extends Task<RemoteOperationValue>  {

    private  Supplier<RemoteOperationValue> supplier;
    private  GitemberServiceImpl service;

    private final ProgressMonitor progressMonitor = new DefaultProgressMonitor((t, d) -> {
        updateTitle(t);
        updateProgress(d, 1.0);
    });

    public RemoteOperationValueTask(final GitemberServiceImpl service, Supplier<RemoteOperationValue> supplier) {
        this.service = service;
        this.supplier = supplier;
    }

    public RemoteOperationValueTask(GitemberServiceImpl service) {
        this.service = service;
    }

    public RemoteOperationValueTask() {
    }

    public Supplier<RemoteOperationValue> getSupplier() {
        return supplier;
    }

    public ProgressMonitor getProgressMonitor() {
        return progressMonitor;
    }

    public void setSupplier(Supplier<RemoteOperationValue> supplier) {
        this.supplier = supplier;
    }

    public void setService(GitemberServiceImpl service) {
        this.service = service;
    }

    @Override
    protected RemoteOperationValue call() throws Exception {
        return service.remoteRepositoryOperation(
                supplier
        );
    }


}
