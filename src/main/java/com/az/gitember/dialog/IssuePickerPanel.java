package com.az.gitember.dialog;

import com.az.gitember.data.Issue;
import com.az.gitember.data.IssueTrackerConfig;
import com.az.gitember.service.Context;
import com.az.gitember.service.tracker.IssueKeyDetector;
import com.az.gitember.service.tracker.IssueTracker;
import com.az.gitember.service.tracker.IssueTrackerService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Combo of issues from the configured tracker, plus optional "include key in commit" checkbox.
 */
public class IssuePickerPanel extends JPanel {

    private static final Logger log = Logger.getLogger(IssuePickerPanel.class.getName());
    private static final Issue NONE = Issue.none();

    private final JComboBox<Issue> combo;
    private final DefaultComboBoxModel<Issue> model;
    private final JButton openBtn;
    private final JCheckBox includeKeyCheck;
    private final JTextField searchField;
    private final List<Consumer<Issue>> selectionListeners = new ArrayList<>();
    private Timer searchDebounce;

    public IssuePickerPanel(boolean showIncludeKey) {
        setLayout(new BorderLayout(6, 4));

        IssueTrackerConfig config = IssueTrackerService.config();
        boolean includeDefault = config == null || config.isIncludeIssueKeyInCommit();

        searchField = new JTextField();
        searchField.putClientProperty("JTextField.placeholderText", "Search issues…");
        searchField.setPreferredSize(new Dimension(120, 25));

        model = new DefaultComboBoxModel<>();
        model.addElement(NONE);
        combo = new JComboBox<>(model);
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(value != null ? value.toString() : NONE.toString());
                String tip = null;
                if (value instanceof Issue issue && issue.isPresent()) {
                    StringBuilder sb = new StringBuilder();
                    if (issue.getStatus() != null && !issue.getStatus().isBlank()) {
                        sb.append(issue.getStatus());
                    }
                    if (issue.getAssignee() != null && !issue.getAssignee().isBlank()) {
                        if (sb.length() > 0) {
                            sb.append(" · ");
                        }
                        sb.append(issue.getAssignee());
                    }
                    if (issue.getDescription() != null && !issue.getDescription().isBlank()) {
                        if (sb.length() > 0) {
                            sb.append('\n');
                        }
                        String desc = issue.getDescription();
                        if (desc.length() > 400) {
                            desc = desc.substring(0, 400) + "…";
                        }
                        sb.append(desc);
                    }
                    if (sb.length() > 0) {
                        tip = sb.toString();
                    }
                }
                setToolTipText(tip);
                return this;
            }
        });

        openBtn = new JButton("Open");
        openBtn.setEnabled(false);
        openBtn.addActionListener(e -> openSelected());

        combo.addActionListener(e -> {
            Issue selected = getSelectedIssue();
            openBtn.setEnabled(selected != null);
            Context.setCurrentIssue(selected);
            for (Consumer<Issue> listener : selectionListeners) {
                listener.accept(selected);
            }
        });

        includeKeyCheck = new JCheckBox("Include issue key", includeDefault);
        includeKeyCheck.setVisible(showIncludeKey);

        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.add(new JLabel("Issue:"), BorderLayout.WEST);
        JPanel fields = new JPanel(new BorderLayout(6, 0));
        fields.add(searchField, BorderLayout.WEST);
        fields.add(combo, BorderLayout.CENTER);
        fields.add(openBtn, BorderLayout.EAST);
        row.add(fields, BorderLayout.CENTER);

        add(row, BorderLayout.CENTER);
        if (showIncludeKey) {
            add(includeKeyCheck, BorderLayout.SOUTH);
        }

        searchDebounce = new Timer(400, e -> runSearch(searchField.getText()));
        searchDebounce.setRepeats(false);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { searchDebounce.restart(); }
            @Override public void removeUpdate(DocumentEvent e) { searchDebounce.restart(); }
            @Override public void changedUpdate(DocumentEvent e) { searchDebounce.restart(); }
        });

        loadInitial();
    }

    public Issue getSelectedIssue() {
        Issue selected = null;
        Object value = combo.getSelectedItem();
        if (value instanceof Issue issue && issue.isPresent()) {
            selected = issue;
        }
        return selected;
    }

    public boolean isIncludeKey() {
        return includeKeyCheck.isVisible() && includeKeyCheck.isSelected();
    }

    public void addIssueChangeListener(Consumer<Issue> listener) {
        if (listener != null) {
            selectionListeners.add(listener);
        }
    }

    public void setSelectedIssue(Issue issue) {
        if (issue != null && issue.isPresent()) {
            boolean found = false;
            for (int i = 0; i < model.getSize(); i++) {
                Issue existing = model.getElementAt(i);
                if (existing != null && issue.getKey().equals(existing.getKey())) {
                    combo.setSelectedIndex(i);
                    found = true;
                }
            }
            if (!found) {
                model.insertElementAt(issue, 1);
                combo.setSelectedItem(issue);
            }
        } else {
            combo.setSelectedItem(NONE);
        }
    }

    private void loadInitial() {
        Issue current = Context.getCurrentIssue();
        String branchKey = null;
        if (Context.getWorkingBranch() != null) {
            branchKey = IssueKeyDetector.findFirst(Context.getWorkingBranch().getShortName());
        }
        final String fromBranch = branchKey;
        runSearch("", current, fromBranch);
    }

    private void runSearch(String query) {
        runSearch(query, getSelectedIssue(), null);
    }

    private void runSearch(String query, Issue prefer, String branchKey) {
        IssueTracker tracker = IssueTrackerService.current();
        if (tracker == null) {
            return;
        }
        combo.setEnabled(false);
        new SwingWorker<List<Issue>, Void>() {
            @Override
            protected List<Issue> doInBackground() throws Exception {
                List<Issue> issues = tracker.search(query);
                if (prefer != null && prefer.isPresent()) {
                    boolean present = issues.stream().anyMatch(i -> prefer.getKey().equals(i.getKey()));
                    if (!present) {
                        tracker.getIssue(prefer.getKey()).ifPresent(issues::add);
                    }
                }
                if (branchKey != null) {
                    boolean present = issues.stream().anyMatch(i -> branchKey.equals(i.getKey()));
                    if (!present) {
                        tracker.getIssue(branchKey).ifPresent(issues::add);
                    }
                }
                return issues;
            }

            @Override
            protected void done() {
                combo.setEnabled(true);
                try {


                    List<Issue> issues = get();
                    Issue keep = prefer != null && prefer.isPresent() ? prefer : null;
                    model.removeAllElements();
                    model.addElement(NONE);
                    for (Issue issue : issues) {
                        model.addElement(issue);
                    }

                    if (!issues.isEmpty()) {
                        setSelectedIssue(issues.get(0));
                    }

                    /*if (keep != null) {
                        setSelectedIssue(keep);
                    } else */else if (branchKey != null) {
                        issues.stream()
                                .filter(i -> branchKey.equals(i.getKey()))
                                .findFirst()
                                .ifPresent(IssuePickerPanel.this::setSelectedIssue);
                    }
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Issue search failed", ex);
                }
            }
        }.execute();
    }

    private void openSelected() {
        Issue issue = getSelectedIssue();
        IssueTracker tracker = IssueTrackerService.current();
        if (issue != null && tracker != null) {
            String url = issue.getUrl();
            if (url == null || url.isBlank()) {
                url = tracker.getIssueUrl(issue.getKey());
            }
            if (url != null && !url.isBlank()) {
                try {
                    Desktop.getDesktop().browse(new URI(url));
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Cannot open issue URL " + url, ex);
                    JOptionPane.showMessageDialog(this,
                            "Cannot open " + url + "\n" + ex.getMessage(),
                            "Open issue", JOptionPane.WARNING_MESSAGE);
                }
            }
        }
    }
}
