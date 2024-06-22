package com.az.gitember.dialog;

import com.az.gitember.App;
import com.az.gitember.data.StatWPParameters;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.Collection;

public class StatWPDialog extends Dialog<StatWPParameters> {

    private final int MIN_WIDTH = 200;


    public StatWPDialog(String title, String header, Collection<String> branches, boolean workProgress, boolean showWorkingHours) {

        super();
        this.setTitle(title);
        this.setHeaderText(header);
        //dialog.setGraphic(new ImageView(this.getClass().getResource("stat.png").toString())); todo
        this.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(25, 15, 15, 25));


        ComboBox<String> branchName = new ComboBox<String>();
        branchName.setMinWidth(MIN_WIDTH);
        if (branches != null) {
            branchName.getItems().addAll(branches);
            branchName.setValue( branches.stream().filter( s -> !s.equalsIgnoreCase("HEAD")).findFirst().get() );
        }

        Spinner<Integer> maxPeople = new Spinner<>(1, 10, 5);
        maxPeople.setMinWidth(MIN_WIDTH);

        Spinner<Integer> monthQty = new Spinner<>(1, 36, 12);
        monthQty.setMinWidth(MIN_WIDTH);

        CheckBox perMonth = new CheckBox();
        perMonth.setSelected(true);

        CheckBox delta = new CheckBox();
        CheckBox woringHours = new CheckBox();

        grid.add(new Label("Branch : "), 0, 0);
        grid.add(branchName, 1, 0);



        if (workProgress) {
            grid.add(new Label("Depth in month : "), 0, 1);
            grid.add(monthQty, 1, 1);
            grid.add(new Label("People : "), 0, 2);
            grid.add(maxPeople, 1, 2);
            grid.add(new Label("Per month : "), 0, 3);
            grid.add(perMonth, 1, 3);
            grid.add(new Label("Delta lines : "), 0, 4);
            grid.add(delta, 1, 4);
        } else if (showWorkingHours) {
            grid.add(new Label("Working hours : "), 0, 1);
            grid.add(woringHours, 1, 1);
        }

        this.getDialogPane().setContent(grid);

        this.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return
                        new StatWPParameters(
                                branchName.getValue(),
                                maxPeople.getValue(),
                                monthQty.getValue(),
                                perMonth.isSelected(),
                                delta.isSelected(),
                                woringHours.isSelected()
                );
            }
            return null;
        });

        Platform.runLater(() -> branchName.requestFocus());
        this.initOwner(App.getScene().getWindow());

    }


}
