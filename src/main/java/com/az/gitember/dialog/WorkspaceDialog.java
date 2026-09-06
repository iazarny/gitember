package com.az.gitember.dialog;

import com.az.gitember.data.Const;
import com.az.gitember.data.Project;
import com.az.gitember.data.Settings;
import com.az.gitember.data.Workspace;
import com.az.gitember.service.Context;
import com.az.gitember.ui.misc.Util;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

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
 * <p>Create mode starts a new workspace and persists it when the user presses <em>Open</em>.
 * Edit mode loads an existing workspace; changes are kept on a copy until <em>Save</em>,
 * then written back to that same instance. <em>Cancel</em> discards the copy.
 */
public class WorkspaceDialog extends JDialog {

    /** Live settings instance being edited, or {@code null} when creating a workspace. */
    private final Workspace existingWorkspace;
    private final Workspace workspace;
    public final JTextField nameField;
    private final DefaultListModel<Project> projectModel = new DefaultListModel<>();
    private final JList<Project> projectList = new JList<>(projectModel);

    /** Invoked with the persisted workspace after a successful save (may be {@code null}). */
    private final Consumer<Workspace> onWorkspaceOpened;

    private boolean confirmed = false;


    public WorkspaceDialog(Frame owner, Consumer<Workspace> onWorkspaceOpened) {
        this(owner, null, onWorkspaceOpened);
    }

    public WorkspaceDialog(Frame owner, Workspace existing, Consumer<Workspace> onWorkspaceOpened) {
        super(owner, existing != null ? "Edit workspace" : "Workspaces",
                Dialog.ModalityType.DOCUMENT_MODAL);


        setSize(640, 480);
        setLocationRelativeTo(owner);
        Util.bindEscapeToDispose(this);

        this.existingWorkspace = existing;
        this.onWorkspaceOpened = onWorkspaceOpened;
        this.workspace = loadWorkingCopy(existing);

        nameField = new JTextField(24);
        nameField.setName("workspaceNameField");
        projectList.setName("workspaceProjectList");
        projectList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        projectList.setCellRenderer(new ProjectCellRenderer());

        setLayout(new BorderLayout());
        add(buildTopPanel(), BorderLayout.NORTH);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildButtonPanel(), BorderLayout.SOUTH);

        loadIntoForm();

        // Keep the workspace name in sync as it is typed.
        nameField.getDocument().addDocumentListener(new SimpleDocumentListener(this::applyNameEdit));

    }

    // ── UI construction ────────────────────────────────────────────────────────

    private JPanel buildTopPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        form.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        form.add(nameField, gbc);

        return form;
    }

    private JComponent buildCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 15, 5, 15));

        JLabel title = new JLabel("Repositories");
        title.setFont(title.getFont().deriveFont(Font.BOLD, title.getFont().getSize() - 1f));
        title.setForeground(UIManager.getColor("Label.disabledForeground"));

        JScrollPane scroll = new JScrollPane(projectList);

        Dimension size = new Dimension(180, 36);

        JButton addExistingBtn = Util.createButton("Add existing…", "Add Existing Project…", FontAwesomeSolid.PLUS,0,size);
                //new JButton("Add Existing Project…");
        addExistingBtn.setName("addExistingProjectButton");
        addExistingBtn.addActionListener(e -> addExistingProject());

        JButton addFromDiskBtn = Util.createButton("Add new…", "Add Repository from Disk…", FontAwesomeSolid.PLUS,0,size);
        //new JButton("Add Repository from Disk…");
        addFromDiskBtn.setName("addRepositoryFromDiskButton");
        addFromDiskBtn.addActionListener(e -> addRepositoryFromDisk());


        JButton removeBtn = Util.createButton("Remove", null, FontAwesomeSolid.MINUS,0,size);
        //new JButton("Remove");
        removeBtn.setName("removeProjectButton");
        removeBtn.addActionListener(e -> removeSelectedProject());
        removeBtn.setEnabled(false);
        projectList.addListSelectionListener(e ->
                removeBtn.setEnabled(projectList.getSelectedValue() != null));

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
        JButton confirmBtn;
        if (existingWorkspace != null) {
            confirmBtn = new JButton("Save");
            confirmBtn.setName("saveWorkspaceButton");
        } else {
            confirmBtn = new JButton("Open");
            confirmBtn.setName("openWorkspaceButton");
        }
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setName("cancelWorkspaceButton");
        confirmBtn.addActionListener(e -> persistWorkspace());
        cancelBtn.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(confirmBtn);

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.add(confirmBtn);
        panel.add(cancelBtn);
        return panel;
    }

    // ── Model handling ───────────────────────────────────────────────────────────

    private Workspace loadWorkingCopy(Workspace existing) {
        Workspace copy;
        if (existing != null) {
            copy = new Workspace(existing.getName(), new TreeSet<>(existing.getProjects()));
        } else {
            Settings settings = Context.getSettings();
            String name = settings != null ? settings.createNewWorkspaceName() : "New workspace";
            copy = new Workspace(name, new TreeSet<>());
        }
        return copy;
    }

    private void loadIntoForm() {
        nameField.setText(workspace.getName() != null ? workspace.getName() : "");
        projectModel.clear();
        workspace.getProjects().forEach(projectModel::addElement);
    }

    private void applyNameEdit() {
        workspace.setName(nameField.getText().trim());
    }

    // ── Project actions ────────────────────────────────────────────────────────

    private void addExistingProject() {
        // Candidates: the known recent projects, excluding those already in this workspace.
        Settings settings = Context.getSettings();
        Set<Project> candidates = new TreeSet<>();
        if (settings != null) {
            candidates.addAll(settings.getProjects());
        }
        candidates.removeAll(workspace.getProjects());
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
            workspace.getProjects().addAll(chooser.getSelectedValuesList());
            loadIntoForm();
        }
    }

    private void addRepositoryFromDisk() {
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
        Project project = Context.getSettings().getOrCreateProject(folder.getAbsolutePath());
        if (!workspace.getProjects().add(project)) {
            JOptionPane.showMessageDialog(this, "This repository is already in the workspace.",
                    "Already Added", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        loadIntoForm();
        projectList.setSelectedValue(project, true);
    }

    private void removeSelectedProject() {
        Project selected = projectList.getSelectedValue();
        if (selected == null) {
            return;
        }
        workspace.getProjects().remove(selected);
        loadIntoForm();
    }

    // ── Persist ──────────────────────────────────────────────────────────────────

    public boolean isConfirmed() {
        return confirmed;
    }

    private void persistWorkspace() {
        applyNameEdit();
        if (workspace.getName() == null || workspace.getName().isBlank()) {
            JOptionPane.showMessageDialog(this, "Workspace name cannot be empty.",
                    "Invalid Name", JOptionPane.WARNING_MESSAGE);
            nameField.requestFocusInWindow();
            return;
        }
        Settings settings = Context.getSettings();
        if (settings != null) {
            if (isNameTaken(settings)) {
                JOptionPane.showMessageDialog(this,
                        "A workspace named \"" + workspace.getName() + "\" already exists.",
                        "Invalid Name", JOptionPane.WARNING_MESSAGE);
                nameField.requestFocusInWindow();
                return;
            }
            Workspace persisted = applyToSettings(settings);
            Context.saveSettings();
            if (onWorkspaceOpened != null) {
                onWorkspaceOpened.accept(persisted);
            }
        }
        confirmed = true;
        dispose();
    }

    private boolean isNameTaken(Settings settings) {
        boolean taken = false;
        String name = workspace.getName();
        for (Workspace ws : settings.getWorkspaces()) {
            if (ws != existingWorkspace && name.equals(ws.getName())) {
                taken = true;
            }
        }
        return taken;
    }

    private Workspace applyToSettings(Settings settings) {
        Workspace persisted;
        if (existingWorkspace != null) {
            existingWorkspace.setName(workspace.getName());
            existingWorkspace.setProjects(new TreeSet<>(workspace.getProjects()));
            persisted = existingWorkspace;
        } else {
            settings.getWorkspaces().add(workspace);
            persisted = workspace;
        }
        return persisted;
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
