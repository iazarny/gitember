package com.az.gitember.controller.branchdiff;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.diff.DiffEntry;

public class BranchDiffCellFactory implements Callback<TableColumn<DiffEntry, DiffEntry>, TableCell<DiffEntry, DiffEntry>> {

    private final boolean left;

    public BranchDiffCellFactory(boolean left) {
        this.left = left;
    }

    @Override
    public TableCell<DiffEntry, DiffEntry> call(TableColumn<DiffEntry, DiffEntry> c) {


        return new TableCell<DiffEntry, DiffEntry>() {
            @Override
            protected void updateItem(DiffEntry de, boolean empty) {
                super.updateItem(de, empty);
                getStyleClass().removeIf(style -> style.toLowerCase().startsWith("diff-"));
                if (!empty) {
                    String itemName = getItemName(de);
                    getStyleClass().add(calculateStyle(de, itemName));
                    setText(itemName);
                }
            }

            private String calculateStyle(final DiffEntry diffEntry, final String itemName) {
                String rez = "";
                if (diffEntry != null && StringUtils.isNotBlank(itemName)) {
                    if (DiffEntry.ChangeType.ADD == diffEntry.getChangeType()) {
                        rez = "diff-new";
                    } else if (DiffEntry.ChangeType.MODIFY == diffEntry.getChangeType()) {
                        rez = "diff-modified";
                    } else if (DiffEntry.ChangeType.DELETE == diffEntry.getChangeType()) {
                        rez = "diff-deleted";
                    } else {
                        rez = "diff-copy";
                    }
                }

                return rez;
            }

            private String getItemName(DiffEntry de) {
                String rez = de.getNewPath();
                if (left) {
                    rez = de.getOldPath();
                }
                return "/dev/null".equals(rez) ? "" : rez;
            }

        };
    }


}
