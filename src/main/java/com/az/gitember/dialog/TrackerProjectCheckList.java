package com.az.gitember.dialog;

import com.az.gitember.data.TrackerProject;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * Checkbox list of tracker projects or teams, used by Jira and Linear settings forms.
 */
public class TrackerProjectCheckList extends JPanel {

    private final List<JCheckBox> checks = new ArrayList<>();
    private final String emptyHint;

    public TrackerProjectCheckList(String emptyHint) {
        this.emptyHint = emptyHint != null ? emptyHint : "Test connection to load items.";
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    public void populate(List<String> selectedKeys, List<TrackerProject> remote) {
        removeAll();
        checks.clear();
        LinkedHashSet<String> keys = new LinkedHashSet<>();
        if (selectedKeys != null) {
            keys.addAll(selectedKeys);
        }
        Map<String, String> names = new HashMap<>();
        if (remote != null) {
            for (TrackerProject p : remote) {
                keys.add(p.getKey());
                names.put(p.getKey(), p.getName());
            }
        }
        if (keys.isEmpty()) {
            JLabel empty = new JLabel(emptyHint);
            empty.setForeground(UIManager.getColor("Label.disabledForeground"));
            add(empty);
        } else {
            for (String key : keys) {
                String label = names.containsKey(key) && names.get(key) != null && !names.get(key).isBlank()
                        ? key + " — " + names.get(key) : key;
                JCheckBox box = new JCheckBox(label, selectedKeys != null && selectedKeys.contains(key));
                box.putClientProperty("projectKey", key);
                checks.add(box);
                add(box);
            }
        }
        revalidate();
        repaint();
    }

    public List<String> selectedKeys() {
        List<String> keys = new ArrayList<>();
        for (JCheckBox box : checks) {
            if (box.isSelected() && box.getClientProperty("projectKey") instanceof String key) {
                keys.add(key);
            }
        }
        return keys;
    }
}
