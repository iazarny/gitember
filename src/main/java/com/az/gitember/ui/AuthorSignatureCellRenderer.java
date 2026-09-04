package com.az.gitember.ui;

import com.az.gitember.data.SignatureStatus;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * History-table author column: a key icon for signed commits, author name otherwise.
 */
public class AuthorSignatureCellRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        setIcon(null);
        if (table.getModel() instanceof HistoryPanel.CommitTableModel model) {
            if (model.isSignedAt(row)) {
                setIcon(CommitSignatureDialog.statusIcon(SignatureStatus.SIGNED, 12));
            }
        }
        return this;
    }
}
