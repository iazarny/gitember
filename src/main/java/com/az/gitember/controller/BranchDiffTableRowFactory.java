package com.az.gitember.controller;

import com.az.gitember.data.Const;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.diff.DiffEntry;

import java.util.function.Predicate;

public class BranchDiffTableRowFactory implements Callback<TableView<DiffEntry>, TableRow<DiffEntry>> {

    private final TextField searchText;

    public BranchDiffTableRowFactory(TextField searchText) {
        this.searchText = searchText;
    }

    @Override
    public TableRow<DiffEntry> call(TableView<DiffEntry> param) {

        return new TableRow<>() {
            @Override
            protected void updateItem(DiffEntry diffEntry, boolean empty) {
                super.updateItem(diffEntry, empty);
                String style = calculateStyle(diffEntry);
                if (StringUtils.isNotBlank(style)) {
                    setStyle(style);
                }
            }

            private String calculateStyle(final DiffEntry diffEntry) {
                if (diffEntry != null) {

                    if (searchText.getText() != null
                            && searchText.getText().length() > Const.SEARCH_LIMIT_CHAR) {
                        if ( diffEntry.getOldPath().toLowerCase().contains(searchText.getText().toLowerCase())
                                || diffEntry.getNewPath().toLowerCase().contains(searchText.getText().toLowerCase())  ) {
                            return LookAndFeelSet.FOUND_ROW;
                        }

                    }

                }
                return  "";
            }

        };
    }

}
