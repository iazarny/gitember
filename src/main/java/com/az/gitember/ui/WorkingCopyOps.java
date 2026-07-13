package com.az.gitember.ui;

import com.az.gitember.data.ScmItem;
import com.az.gitember.service.Context;
import com.az.gitember.service.ExtensionMap;
import com.az.gitember.ui.misc.Util;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class WorkingCopyOps  extends JPanel {


    protected final JButton stageAllBtn;
    protected final JButton unstageAllBtn;
    protected final JButton refreshBtn;
    protected final JTextField searchField;
    protected final StatusBar statusBar;



    public WorkingCopyOps(StatusBar statusBar) {

        this.statusBar = statusBar;

        // Toolbar
        stageAllBtn = Util.createButton("Stage All", null, FontAwesomeSolid.PLUS);
        stageAllBtn.setEnabled(false);

        unstageAllBtn = Util.createButton("Unstage All", null, FontAwesomeSolid.MINUS);
        unstageAllBtn.setEnabled(false);

        refreshBtn = Util.createButton("Refresh", null, FontAwesomeSolid.SYNC);

        searchField = new JTextField(15);
        searchField.setPreferredSize(new Dimension(150, 25));
        searchField.setMinimumSize(new Dimension(100, 25));
        searchField.setMaximumSize(new Dimension(150, 25));
        searchField.setEnabled(false);


        searchField.addActionListener(e -> applyFilter());
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
        });

        stageAllBtn.addActionListener(e -> stageAll());
        unstageAllBtn.addActionListener(e -> unstageAll());
        refreshBtn.addActionListener(e -> refresh());

    }

    public JButton getStageAllBtn() {
        return stageAllBtn;
    }
    public JButton getUnstageAllBtn() {
        return unstageAllBtn;
    }
    public JButton getRefreshBtn() {
        return refreshBtn;
    }
    public JTextField getSearchField() {
        return searchField;
    }

    abstract protected void applyFilter();
    abstract protected void stageAll();
    abstract protected void unstageAll();
    abstract protected void refresh();
    abstract protected void updateButtonStates();


}
