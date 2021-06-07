package com.az.gitember.controller;

import com.az.gitember.App;
import com.az.gitember.controller.handlers.*;
import com.az.gitember.data.ScmBranch;
import com.az.gitember.data.ScmRevisionInformation;
import com.az.gitember.service.Context;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MainTreeContextMenuFactory {

    private MenuItem pullMI;
    private MenuItem pushMI;

    ContextMenu createContextMenu(final ScmBranch branchItem) {

        boolean disablePool = branchItem.getRemoteMergeName() == null &&  ScmBranch.BranchType.LOCAL.equals(branchItem.getBranchType());
        final String name = branchItem.getShortName();
        String fullName = name;
        if (branchItem.getRemoteMergeName() != null) {
            fullName += " (" + branchItem.getRemoteMergeName() + ")";
        }

        ContextMenu cm = new ContextMenu();

        MenuItem checkoutMI = new MenuItem("Checkout");
        checkoutMI.setOnAction(new CheckoutBranchEventHandler(branchItem));

        MenuItem branchMI = new MenuItem("Create branch ...");
        branchMI.setOnAction( new CreateBranchEventHandler(branchItem) );

        SeparatorMenuItem separator = new SeparatorMenuItem();




        MenuItem deleteMI = new MenuItem("Delete "+ branchItem.getShortName() + "...");
        deleteMI.setOnAction(new DeleteBranchEventHandler(branchItem));

        pullMI = new MenuItem("Pull " + (disablePool ? "" : fullName));
        pushMI = new MenuItem("Push " + (name == fullName ? (name + " ..."):(fullName) ) );

        cm.getItems().add(checkoutMI);
        cm.getItems().add(branchMI);

        if (!branchItem.getShortName().equals(Context.workingBranch.getValue().getShortName()) && ScmBranch.BranchType.TAG != branchItem.getBranchType()) {

            MenuItem mergeMI = new MenuItem("Merge "+ branchItem.getShortName() + " -> " + Context.workingBranch.getValue().getShortName() +  "...");
            mergeMI.setOnAction(new MergeBranchEventHandler(branchItem.getFullName()));

            MenuItem rebaseMI = new MenuItem("Rebase "+ branchItem.getShortName() + " -> " + Context.workingBranch.getValue().getShortName() +  "...");
            rebaseMI.setOnAction(new RebaseBranchEventHandler(branchItem.getFullName()));

            cm.getItems().add(mergeMI);
            cm.getItems().add(rebaseMI);



        }

        if (branchItem.getBranchType().equals(ScmBranch.BranchType.LOCAL)) {
            cm.getItems().add(new SeparatorMenuItem());
            cm.getItems().add(pullMI);
            cm.getItems().add(pushMI);
        }

        cm.getItems().add(new SeparatorMenuItem());
        cm.getItems().add(deleteMI);

        pullMI.setDisable(disablePool);
        if (!disablePool) {
            pullMI.setOnAction(new PullHandler(branchItem));
        }
        pushMI.setOnAction( new PushHandler(branchItem) );

        if (ScmBranch.BranchType.TAG == branchItem.getBranchType()) {
            cm.getItems().removeAll( pullMI, pushMI);
        }

        return cm;
    }



    ContextMenu createContextMenu(ScmRevisionInformation scmItem) {

        MenuItem applyStashMI = new MenuItem("Apply stash ...");
        applyStashMI.setOnAction(new ApplyStashEventHandler(scmItem));

        MenuItem deleteStashMI = new MenuItem("Delete stash ...");
        deleteStashMI.setOnAction(new DeleteStashEventHandler(scmItem));

        return new ContextMenu(
                applyStashMI,
                new SeparatorMenuItem(),
                deleteStashMI
        );
    }


    ContextMenu createSearchContextMenu() {
        MenuItem searchMenuItem = new MenuItem("Search ...");
        searchMenuItem.setOnAction(new MainTreeBranchSearchHandler(searchMenuItem));
        return new ContextMenu(searchMenuItem);
    }

    ContextMenu createTagContextMenu() {

        MenuItem createTagMenuItem = new MenuItem("Create tag ...");
        createTagMenuItem.setOnAction(new CreateTagEventHandler());

        MenuItem searchMenuItem = new MenuItem("Search ...");
        searchMenuItem.setOnAction(new MainTreeBranchSearchHandler(searchMenuItem));


        return new ContextMenu(searchMenuItem, new SeparatorMenuItem(), createTagMenuItem);
    }
}
