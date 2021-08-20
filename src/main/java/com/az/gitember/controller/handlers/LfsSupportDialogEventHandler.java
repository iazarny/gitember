package com.az.gitember.controller.handlers;

import com.az.gitember.controller.LFSDialog;
import com.az.gitember.data.Const;
import com.az.gitember.data.LfsData;
import com.az.gitember.service.Context;
import com.az.gitember.service.GitAttributesUtil;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LfsSupportDialogEventHandler implements EventHandler<ActionEvent> {

    private final static Logger log = Logger.getLogger(LfsSupportDialogEventHandler.class.getName());

    private final Path path = Path.of(Context.getProjectFolder(), Const.GIT_ATTR_NAME);

    @Override
    public void handle(ActionEvent event)  {
        final boolean lfsRepo = Context.getGitRepoService().isLfsRepo();
        final List<String> patternsOriginal = new ArrayList<>();
        LfsData lfsData = new LfsData();
        lfsData.setLfsSupport(lfsRepo);

        if (lfsRepo) {
            List<String> patterns = GitAttributesUtil.getLsfPatters(getAttributesFileSafe());
            patternsOriginal.addAll(patterns);
            lfsData.getExtentions().addAll(patterns);
        }

        Optional<LfsData> dialogResult = new LFSDialog(lfsData).showAndWait();

        if (dialogResult.isPresent()) {
            LfsData newLfsData = dialogResult.get();
            if (newLfsData.isLfsSupport()) {

                String origFile = getAttributesFileSafe();
                for(String pattern : patternsOriginal) {
                    origFile = GitAttributesUtil.removeLfsPattern(origFile, pattern);
                }
                for(String pattern : newLfsData.getExtentions()) {
                    origFile = GitAttributesUtil.addLfsPattern(origFile, pattern);
                }

                try {
                    Files.writeString(path, origFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    //TODO add folder  if was not present
                } catch (IOException e) {
                    log.log(Level.SEVERE, "Cannot write lfs patterns " , e);
                    Context.getMain().showResult("Cannot wrute lfs patterns ",
                            ExceptionUtils.getStackTrace(e), Alert.AlertType.ERROR);
                }
            }
        }

    }

    private String getAttributesFileSafe() {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            return "";
        }
    }
}
