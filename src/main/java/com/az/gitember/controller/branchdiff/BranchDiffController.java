package com.az.gitember.controller.branchdiff;

import com.az.gitember.controller.handlers.DiffEventHandler;
import com.az.gitember.controller.workingcopy.WorkingcopyTableGraphicsValueFactory;
import com.az.gitember.data.*;
import com.az.gitember.service.Context;
import com.az.gitember.service.ExtensionMap;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Ref;
import org.kordamp.ikonli.javafx.StackedFontIcon;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controlled for branch difference.
 */
public class BranchDiffController implements Initializable {

    private final static Logger log = Logger.getLogger(BranchDiffController.class.getName());

    private final ObservableList<DiffEntry> observableDiffEntry = FXCollections.observableList(new ArrayList<>());
    private final BranchDiffCellFactory branchDiffCellFactoryLeft = new BranchDiffCellFactory(true);
    private final BranchDiffCellFactory branchDiffCellFactoryRight = new BranchDiffCellFactory(false);

    public TableView<DiffEntry> diffTableView;
    public TableColumn<DiffEntry, StackedFontIcon> actionTableColumn;
    public TableColumn<DiffEntry, DiffEntry> leftTableColumn;
    public TableColumn<DiffEntry, DiffEntry> rightTableColumn;


    public MenuItem menuItemShowDiff;
    public TextField searchText;
    private Ref leftHead = null;
    private Ref rightHead = null;
    private String leftBranchName;
    private String rightBranchName;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        actionTableColumn.setCellValueFactory(c ->
                new WorkingcopyTableGraphicsValueFactory(c.getValue().getChangeType().name(), null));

        leftTableColumn.setCellValueFactory(c -> new SimpleObjectProperty(c.getValue()));
        leftTableColumn.setCellFactory( c-> branchDiffCellFactoryLeft.call(c) );

        rightTableColumn.setCellValueFactory(c -> new SimpleObjectProperty(c.getValue()));
        rightTableColumn.setCellFactory( c-> branchDiffCellFactoryRight.call(c) );

        diffTableView.setItems(observableDiffEntry);
        diffTableView.setRowFactory(new BranchDiffTableRowFactory(searchText));

        diffTableView.setOnContextMenuRequested(
                e -> {
                    DiffEntry entry = diffTableView.getSelectionModel().getSelectedItem();
                    if (entry == null) {
                        menuItemShowDiff.setDisable(true);
                    } else {
                        String fileName = StringUtils.defaultIfBlank(
                                adjustName(diffTableView.getSelectionModel().getSelectedItem().getOldPath()),
                                adjustName(diffTableView.getSelectionModel().getSelectedItem().getNewPath())
                        );
                        menuItemShowDiff.setDisable(!ExtensionMap.isTextExtension(fileName));
                    }
                }
        );

        searchText.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    diffTableView.refresh();
                }
        );

    }

    public void setData(String leftBranchName, String rightBranchName, List<DiffEntry> diffEntries) {
        this.leftBranchName = leftBranchName;
        this.rightBranchName = rightBranchName;
        leftTableColumn.setGraphic(new Text(leftBranchName));
        rightTableColumn.setGraphic(new Text(rightBranchName));
        observableDiffEntry.addAll(diffEntries);
    }



    public void showItemDiffHandler(ActionEvent actionEvent) {
        String fileName = StringUtils.defaultIfBlank(
                adjustName(diffTableView.getSelectionModel().getSelectedItem().getOldPath()),
                adjustName(diffTableView.getSelectionModel().getSelectedItem().getNewPath())
        );
        try {

            DiffEventHandler diffEventHandler = new DiffEventHandler(
                    new ScmItem(fileName, new ScmItemAttribute()),
                    getLeftHead().getName(),
                    getRightHead().getName()
            );

            diffEventHandler.handle(null);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Cannot show  difference for  " + fileName, e);
            Context.getMain().showResult("Cannot show  difference for  " + fileName,
                    ExceptionUtils.getRootCause(e).getMessage(), Alert.AlertType.ERROR);
        }
    }

    public Ref getLeftHead() throws IOException {
        if (leftHead == null) {
            leftHead = Context.getGitRepoService().getRepository().exactRef(leftBranchName);
        }
        return leftHead;
    }

    public Ref getRightHead() throws IOException {
        if (rightHead == null) {
            rightHead = Context.getGitRepoService().getRepository().exactRef(rightBranchName);
        }
        return rightHead;
    }

    private SimpleObjectProperty getName(String fileName) {
        final String name = adjustName(fileName);
        return new SimpleObjectProperty(name);
    }

    private String adjustName(String fileName) {
        final String name;
        if ("/dev/null".equals(fileName)) {
            name = "";
        } else {
            name = fileName;
        }
        return name;
    }
}
