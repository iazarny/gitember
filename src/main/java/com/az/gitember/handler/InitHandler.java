package com.az.gitember.handler;

import com.az.gitember.data.InitRepoParameters;
import com.az.gitember.service.Context;
import com.az.gitember.service.GitRepoService;
import com.az.gitember.ui.StatusBar;

import java.awt.*;

public class InitHandler extends AbstractAsyncHandler<Void> {

    private final InitRepoParameters params;

    public InitHandler(Component parent, StatusBar statusBar, InitRepoParameters params) {
        super(parent, statusBar);
        this.params = params;
    }

    @Override
    protected String getOperationName() {
        return "Init";
    }

    @Override
    protected Void doInBackground() throws Exception {
        GitRepoService.createRepository(
                params.getDestinationFolder(),
                params.isInitWithReame(),
                params.isInitWithIgnore(),
                params.isInitWithLfs()
        );
        Context.init(params.getDestinationFolder());
        return null;
    }

    @Override
    protected void onSuccess(Void result) {
        statusBar.setStatus("Repository initialized successfully");
    }
}
