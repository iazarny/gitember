package com.az.gitember.controller;

import com.az.gitember.data.Const;
import com.az.gitember.data.ScmItem;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import org.eclipse.jgit.diff.DiffEntry;

public class BranchDiffTableRowFactory implements Callback<TableView<DiffEntry>, TableRow<DiffEntry>> {

    @Override
    public TableRow<DiffEntry> call(TableView<DiffEntry> param) {
        return new TableRow<>() {
            @Override
            protected void updateItem(DiffEntry diffEntry, boolean empty) {
                super.updateItem(diffEntry, empty);
                if (!empty) {
                    getStyleClass().add(calculateStyle(diffEntry));
                }
            }

            private String calculateStyle(final DiffEntry diffEntry) {
                if (diffEntry != null) {
                    if (DiffEntry.ChangeType.ADD == diffEntry.getChangeType()) {
                        return "diff-row-new";
                    } else if (DiffEntry.ChangeType.MODIFY == diffEntry.getChangeType()) {
                        return "diff-row-modified";
                    } else if (DiffEntry.ChangeType.DELETE == diffEntry.getChangeType()) {
                        return "diff-row-deleted";
                    }
                    return "diff-row-copy";
                }
                return  "";
            }

        };
    }

}
