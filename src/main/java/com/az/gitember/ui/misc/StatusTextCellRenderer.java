package com.az.gitember.ui.misc;

import com.az.gitember.service.I18n;

import javax.swing.table.DefaultTableCellRenderer;

/**
 * Table cell renderer that shows a translated {@code ScmItem.Status} value. The model keeps the
 * raw status string, which the code compares against, so only the displayed text changes.
 */
public class StatusTextCellRenderer extends DefaultTableCellRenderer {

    @Override
    protected void setValue(Object value) {
        super.setValue(value instanceof String status ? I18n.status(status) : value);
    }
}
