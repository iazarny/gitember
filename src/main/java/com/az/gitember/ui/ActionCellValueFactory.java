package com.az.gitember.ui;

import com.az.gitember.misc.ScmItem;
import com.az.gitember.misc.ScmItemAction;
import com.az.gitember.misc.ScmRevisionInformation;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created by Igor_Azarny on 04 - Dec - 2016
 */
public class ActionCellValueFactory implements ObservableValue<FontIcon> {

    static Map<String, Callable<FontIcon>> icons = new HashMap<>();

    static {
        icons.put(ScmItemAction.ADD, () -> new FontIcon(FontAwesome.PLUS_SQUARE_O));
        icons.put(ScmItemAction.DELETE, () -> new FontIcon(FontAwesome.MINUS_SQUARE_O));
        icons.put(ScmItemAction.MODIFY, () -> new FontIcon(FontAwesome.EDIT));
        icons.put(ScmItemAction.COPY, () -> new FontIcon(FontAwesome.COPY));
        icons.put(ScmItemAction.RENAME, () -> new FontIcon(FontAwesome.COPY));
    }

    private static Callable<FontIcon> fontIconUnknown = () -> new FontIcon(FontAwesome.QUESTION);

    private final TableColumn.CellDataFeatures<ScmItem, FontIcon> cellDataFeatures;
    private final TableColumn.CellDataFeatures<ScmRevisionInformation, FontIcon> cellDataFeaturesScmRev;


    public ActionCellValueFactory(TableColumn.CellDataFeatures<ScmItem, FontIcon> cellDataFeatures,
                                  TableColumn.CellDataFeatures<ScmRevisionInformation, FontIcon> cellDataFeaturesScmRev) {
        this.cellDataFeatures = cellDataFeatures;
        this.cellDataFeaturesScmRev = cellDataFeaturesScmRev;
    }

    @Override
    public void addListener(ChangeListener<? super FontIcon> listener) {

    }

    @Override
    public void removeListener(ChangeListener<? super FontIcon> listener) {

    }

    @Override
    public FontIcon getValue() {
        try {
            if (cellDataFeaturesScmRev == null) {
                return icons.getOrDefault(cellDataFeatures.getValue().getFirst(), fontIconUnknown).call();
            } else {
                return icons.getOrDefault(cellDataFeaturesScmRev.getValue().getAffectedItems().get(0).getFirst(),
                        fontIconUnknown).call();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void addListener(InvalidationListener listener) {

    }

    @Override
    public void removeListener(InvalidationListener listener) {

    }
}
