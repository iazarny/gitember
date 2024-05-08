package com.az.gitember.module.history.factory;

import com.az.gitember.controller.LookAndFeelSet;
import com.az.gitember.data.Const;
import com.az.gitember.service.Context;
import javafx.scene.control.TableRow;
import javafx.scene.control.TextField;
import org.eclipse.jgit.revplot.PlotCommit;
import org.eclipse.jgit.revplot.PlotLane;

import java.util.Map;
import java.util.Set;

public class HistoryCommitsTableRowFactory extends TableRow<PlotCommit<PlotLane>> {

    private final TextField searchTextField;


    public HistoryCommitsTableRowFactory(TextField searchTextField) {
        this.searchTextField = searchTextField;
    }

    @Override
    protected void updateItem(PlotCommit item, boolean empty) {
        super.updateItem(item, empty);
        setStyle(calculateStyle(item));
    }

    private String calculateStyle(final PlotCommit<PlotLane> plotCommit) {
        String searchString = searchTextField.getText();
        if (isSearchEligible(plotCommit, searchString)) {
            Map<String, Set<String>> map = Context.searchResult.getValue();
            if (map != null && map.containsKey(plotCommit.getName()) ) {
                return LookAndFeelSet.FOUND_ROW;
            }
        }
        return "";
    }

    private boolean isSearchEligible(PlotCommit<PlotLane> plotCommit, String searchString) {
        return plotCommit != null
                && plotCommit.getName() != null
                && searchString != null
                && searchString.length() > Const.SEARCH_LIMIT_CHAR;
    }
}
