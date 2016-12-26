package com.az.gitember.ui;

import com.az.gitember.misc.ScmItem;
import com.az.gitember.misc.ScmItemAction;
import com.az.gitember.misc.ScmItemStatus;
import com.az.gitember.misc.ScmRevisionInformation;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created by Igor_Azarny on 04 - Dec - 2016
 */
public class StatusCellValueFactory implements ObservableValue<FontIcon> {

    static Map<String, Callable<FontIcon>> icons = new HashMap<>();

    static {
        icons.put(ScmItemStatus.REMOVED, () -> new FontIcon(FontAwesome.MINUS_SQUARE_O));
        icons.put(ScmItemStatus.UNTRACKED, () -> new FontIcon(FontAwesome.QUESTION));
        icons.put(ScmItemStatus.MISSED, () -> new FontIcon(FontAwesome.MINUS_SQUARE_O));
        icons.put(ScmItemStatus.MODIFIED, () -> new FontIcon(FontAwesome.EDIT));
        icons.put(ScmItemStatus.ADDED, () -> new FontIcon(FontAwesome.PLUS_SQUARE_O));
    }

    private static Callable<FontIcon> fontIconUnknown = () -> new FontIcon(FontAwesome.QUESTION);
    private final List<String> statuses;


    public StatusCellValueFactory(List<String> statuses) {
        this.statuses = statuses;
    }

    @Override
    public void addListener(ChangeListener<? super FontIcon> listener) {

    }

    @Override
    public void removeListener(ChangeListener<? super FontIcon> listener) {

    }

    @Override
    public FontIcon getValue() {
        String st = statuses.stream().filter(s -> s.equals(ScmItemStatus.MODIFIED)
                || s.equals(ScmItemStatus.REMOVED)
                || s.equals(ScmItemStatus.MISSED)
                || s.equals(ScmItemStatus.ADDED)
                || s.equals(ScmItemStatus.UNTRACKED)
        ).findFirst().orElse(null);

        try {
            return icons.getOrDefault(st,
                    fontIconUnknown).call();
        } catch (Exception e) {
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
