package com.az.gitember.controller.common;

import com.az.gitember.data.ScmRevisionInformation;
import com.az.gitember.service.GitemberUtil;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class ScmRevisionInformationCellFactory implements  Callback<ListView<ScmRevisionInformation>, ListCell<ScmRevisionInformation>> {
    @Override
    public ListCell<ScmRevisionInformation> call(ListView<ScmRevisionInformation> param) {
        return new ListCell<ScmRevisionInformation>() {
            @Override
            protected void updateItem(ScmRevisionInformation item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    setText(GitemberUtil.formatRev(item));
                }
            }
        };
    }
}
