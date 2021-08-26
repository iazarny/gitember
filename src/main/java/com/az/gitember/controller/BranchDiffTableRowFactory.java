package com.az.gitember.controller;

import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import org.eclipse.jgit.diff.DiffEntry;

import java.util.function.Predicate;

public class BranchDiffTableRowFactory implements Callback<TableView<DiffEntry>, TableRow<DiffEntry>> {

    @Override
    public TableRow<DiffEntry> call(TableView<DiffEntry> param) {
        return new TableRow<>() {
            @Override
            protected void updateItem(DiffEntry diffEntry, boolean empty) {
                super.updateItem(diffEntry, empty);
                getStyleClass().removeIf(s -> s.toLowerCase().startsWith("diff-"));
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
