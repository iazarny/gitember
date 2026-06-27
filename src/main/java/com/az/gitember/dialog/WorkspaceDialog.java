package com.az.gitember.dialog;

import com.az.gitember.data.Const;
import com.az.gitember.data.Project;
import com.az.gitember.data.Settings;
import com.az.gitember.data.Workspace;
import com.az.gitember.service.Context;
import com.az.gitember.ui.misc.Util;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;

/**
 * Create / edit and open a {@link Workspace}. A workspace has a name and a list of
 * projects (git repositories). Projects can be added either by picking one of the
 * already-known projects from {@link Settings#getProjects()}, or by selecting a git
 * repository folder on disk.
 *
 * <p>Edits are made against an in-memory copy of the settings' workspaces and are only
 * persisted when the user presses <em>Open</em> — pressing <em>Cancel</em> discards them.
 */
public class WorkspaceDialog extends JDialog {

    private final List<Workspace> workingWorkspaces = new ArrayList<>();
    private final JComboBox<Workspace> workspaceCombo;
    public final JTextField nameField;
    private final DefaultListModel<Project> projectModel = new DefaultListModel<>();
    private final JList<Project> projectList = new JList<>(projectModel);

    /** Invoked with the opened workspace after a successful save (may be {@code null}). */
    private final Consumer<Workspace> onWorkspaceOpened;

    /** Guards combo-selection handling while we mutate the combo programmatically. */
    private boolean updating = false;

    private boolean confirmed = false;


    public WorkspaceDialog(Frame owner, Consumer<Workspace> onWorkspaceOpened) {
        super(owner, "Workspaces", Dialog.ModalityType.DOCUMENT_MODAL);
        this.onWorkspaceOpened = onWorkspaceOpened;

        loadWorkingCopy();

        workspaceCombo = new JComboBox<>(workingWorkspaces.toArray(new Workspace[0]));
        nameField = new JTextField(24);
        projectList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        projectList.setCellRenderer(new ProjectCellRenderer());

        setLayout(new BorderLayout());
        add(buildTopPanel(), BorderLayout.NORTH);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildButtonPanel(), BorderLayout.SOUTH);

        if (!workingWorkspaces.isEmpty()) {
            workspaceCombo.setSelectedIndex(0);
        }
        loadSelectedIntoForm();

        workspaceCombo.addActionListener(e -> {
            if (!updating) {
                loadSelectedIntoForm();
            }
        });

        setSize(560, 520);
        setLocationRelativeTo(owner);
        Util.bindEscapeToDispose(this);
    }

    // ── UI construction ────────────────────────────────────────────────────────

    private JPanel buildTopPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JButton newBtn = new JButton("New");
        newBtn.addActionListener(e -> createNewWorkspace());
        JButton deleteBtn = new JButton("Delete");
        deleteBtn.addActionListener(e -> deleteSelectedWorkspace());

        gbc.gridx = 0; gbc.gridy = 0; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        form.add(new JLabel("Workspace:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        form.add(workspaceCombo, gbc);
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        form.add(newBtn, gbc);
        gbc.gridx = 3;
        form.add(deleteBtn, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        form.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        form.add(nameField, gbc);
        gbc.gridwidth = 1;

        // Keep the combo label in sync as the name is typed.
        nameField.getDocument().addDocumentListener(new SimpleDocumentListener(this::applyNameEdit));

        return form;
    }

    private JComponent buildCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 15, 5, 15));

        JLabel title = new JLabel("Repositories");
        title.setFont(title.getFont().deriveFont(Font.BOLD, title.getFont().getSize() - 1f));
        title.setForeground(UIManager.getColor("Label.disabledForeground"));

        JScrollPane scroll = new JScrollPane(projectList);

        JButton addExistingBtn = new JButton("Add Existing Project…");
        addExistingBtn.addActionListener(e -> addExistingProject());
        JButton addFromDiskBtn = new JButton("Add Repository from Disk…");
        addFromDiskBtn.addActionListener(e -> addRepositoryFromDisk());
        JButton removeBtn = new JButton("Remove");
        removeBtn.addActionListener(e -> removeSelectedProject());

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btnBar.add(addExistingBtn);
        btnBar.add(addFromDiskBtn);
        btnBar.add(removeBtn);

        panel.add(title, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(btnBar, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildButtonPanel() {
        JButton openBtn = new JButton("Open");
        JButton cancelBtn = new JButton("Cancel");
        openBtn.addActionListener(e -> openWorkspace());
        cancelBtn.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(openBtn);

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.add(openBtn);
        panel.add(cancelBtn);
        return panel;
    }

    // ── Model handling ───────────────────────────────────────────────────────────

    private void loadWorkingCopy() {
        Settings settings = Context.getSettings();
        if (settings == null) {
            return;
        }
        // Copy each workspace so edits are isolated until the user presses Open.
        for (Workspace ws : settings.getWorkspaces()) {
            workingWorkspaces.add(new Workspace(ws.getName(), new TreeSet<>(ws.getProjects())));
        }
    }

    private Workspace selectedWorkspace() {
        return (Workspace) workspaceCombo.getSelectedItem();
    }

    private void loadSelectedIntoForm() {
        Workspace ws = selectedWorkspace();
        updating = true;
        try {
            nameField.setText(ws != null ? ws.getName() : "");
        } finally {
            updating = false;
        }
        projectModel.clear();
        if (ws != null) {
            ws.getProjects().forEach(projectModel::addElement);
        }
    }

    private void applyNameEdit() {
        if (updating) {
            return;
        }
        Workspace ws = selectedWorkspace();
        if (ws != null) {
            ws.setName(nameField.getText().trim());
            // Refresh the combo's rendered label without firing a reselection.
            updating = true;
            try {
                workspaceCombo.repaint();
            } finally {
                updating = false;
            }
        }
    }

    private void createNewWorkspace() {
        String name = Context.getSettings().createNewWorkspaceName();
        Workspace ws = new Workspace(name, new TreeSet<>());
        workingWorkspaces.add(ws);
        updating = true;
        try {
            workspaceCombo.addItem(ws);
            workspaceCombo.setSelectedItem(ws);
        } finally {
            updating = false;
        }
        loadSelectedIntoForm();
        nameField.requestFocusInWindow();
        nameField.selectAll();
    }

    private void deleteSelectedWorkspace() {
        Workspace ws = selectedWorkspace();
        if (ws == null) {
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this,
                "Remove workspace \"" + ws.getName() + "\"?\nThe repositories themselves are not deleted from disk.",
                "Delete Workspace", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) {
            return;
        }
        workingWorkspaces.remove(ws);
        updating = true;
        try {
            workspaceCombo.removeItem(ws);
        } finally {
            updating = false;
        }
        loadSelectedIntoForm();
    }



    // ── Project actions ────────────────────────────────────────────────────────

    private void addExistingProject() {
        Workspace ws = selectedWorkspace();
        if (ws == null) {
            JOptionPane.showMessageDialog(this, "Create or select a workspace first.",
                    "No Workspace", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        // Candidates: the known recent projects, excluding those already in this workspace.
        Settings settings = Context.getSettings();
        Set<Project> candidates = new TreeSet<>();
        if (settings != null) {
            candidates.addAll(settings.getProjects());
        }
        candidates.removeAll(ws.getProjects());
        if (candidates.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No other known projects to add.",
                    "Add Existing Project", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Project[] options = candidates.toArray(new Project[0]);
        JList<Project> chooser = new JList<>(options);
        chooser.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        chooser.setCellRenderer(new ProjectCellRenderer());
        int result = JOptionPane.showConfirmDialog(this, new JScrollPane(chooser),
                "Select project(s) to add", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            ws.getProjects().addAll(chooser.getSelectedValuesList());
            loadSelectedIntoForm();
        }
    }

    private void addRepositoryFromDisk() {
        Workspace ws = selectedWorkspace();
        if (ws == null) {
            JOptionPane.showMessageDialog(this, "Create or select a workspace first.",
                    "No Workspace", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Select Git Repository");
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File folder = chooser.getSelectedFile();
        if (!new File(folder, Const.GIT_FOLDER).exists()) {
            JOptionPane.showMessageDialog(this,
                    "The selected folder is not a git repository (no " + Const.GIT_FOLDER + " folder found).",
                    "Not a Repository", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Project project = new Project(folder.getAbsolutePath(), new Date());
        if (!ws.getProjects().add(project)) {
            JOptionPane.showMessageDialog(this, "This repository is already in the workspace.",
                    "Already Added", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        loadSelectedIntoForm();
        projectList.setSelectedValue(project, true);
    }

    private void removeSelectedProject() {
        Workspace ws = selectedWorkspace();
        Project selected = projectList.getSelectedValue();
        if (ws == null || selected == null) {
            return;
        }
        ws.getProjects().remove(selected);
        loadSelectedIntoForm();
    }

    // ── Persist ──────────────────────────────────────────────────────────────────

    public boolean isConfirmed() {
        return confirmed;
    }

    private void openWorkspace() {
        Workspace ws = selectedWorkspace();
        if (ws == null) {
            dispose();
            return;
        }
        applyNameEdit();
        if (ws.getName() == null || ws.getName().isBlank()) {
            JOptionPane.showMessageDialog(this, "Workspace name cannot be empty.",
                    "Invalid Name", JOptionPane.WARNING_MESSAGE);
            nameField.requestFocusInWindow();
            return;
        }
        Settings settings = Context.getSettings();
        if (settings != null) {
            settings.setWorkspaces(new ArrayList<>(workingWorkspaces));
            Context.saveSettings();
            if (onWorkspaceOpened != null) {
                onWorkspaceOpened.accept(ws);
            }
        }
        confirmed = true;
        dispose();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────

    /** Renders a project as "folder-name — full/path". */
    private static class ProjectCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Project p) {
                String folder = p.getProjectHomeFolder();
                String name = new File(folder).getName();
                setText("<html><b>" + name + "</b>  <span style='color:gray'>" + folder + "</span></html>");
            }
            return this;
        }
    }

    /** Minimal {@link javax.swing.event.DocumentListener} that runs one action on any change. */
    private static class SimpleDocumentListener implements javax.swing.event.DocumentListener {
        private final Runnable action;

        SimpleDocumentListener(Runnable action) {
            this.action = action;
        }

        @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { action.run(); }
        @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { action.run(); }
        @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { action.run(); }
    }
}
