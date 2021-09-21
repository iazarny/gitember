package com.az.gitember.controller.handlers;

import com.az.gitember.controller.DefaultProgressMonitor;
import com.az.gitember.controller.IntegerlDialog;
import com.az.gitember.data.ScmItemDocument;
import com.az.gitember.data.ScmRevisionInformation;
import com.az.gitember.service.Context;
import com.az.gitember.service.SearchService;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.text.MessageFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IndexEventHandler extends AbstractLongTaskEventHandler implements EventHandler<ActionEvent> {

    private final static Logger log = Logger.getLogger(IndexEventHandler.class.getName());

    Integer docQty  = 0 ;

    @Override
    public void handle(ActionEvent event) {

        IntegerlDialog integerlDialog = new IntegerlDialog("Index repository history " , "Limit revisions to reindex", "Quantiy", 100);

        integerlDialog.showAndWait().ifPresent( r -> {


            Task<Void> longTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {

                    DefaultProgressMonitor progressMonitor = new DefaultProgressMonitor((t, d) -> {
                        updateTitle(t);
                        updateProgress(d, 1.0);
                    });

                    List<ScmRevisionInformation> sriList = Context.getGitRepoService().getItemsToIndex(null, r, progressMonitor);

                    SearchService service = new SearchService( Context.getProjectFolder() );

                    progressMonitor.beginTask("Indexing ", sriList.size());

                    int idx = 0;
                    for (ScmRevisionInformation sri : sriList) {
                        sri.getAffectedItems().forEach(
                                i -> {
                                    service.submitItemToReindex(new ScmItemDocument(i));
                                }
                        );
                        idx++;
                        progressMonitor.update(idx);
                        docQty = docQty + sri.getAffectedItems().size();
                    }

                    service.close();

                    return null;

                }
            };

            launchLongTask(
                    longTask,
                    o -> {
                        Context.getCurrentProject().setIndexed(true);
                        Context.saveSettings();
                        Context.getMain().showResult(
                                "Indexing", "Was indexed " + docQty + " documents",
                                Alert.AlertType.INFORMATION);
                    },
                    o -> {
                        log.log(Level.WARNING,
                                MessageFormat.format("Indexing error", o.getSource().getException()));
                        Context.getMain().showResult("Indexing", "Cannot index history of changess\n" +
                                ExceptionUtils.getStackTrace(o.getSource().getException()), Alert.AlertType.ERROR);
                        o.getSource().getException().printStackTrace();
                    }
            );




        } );



    }

}
