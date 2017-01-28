package com.az.gitember.ui;

import javafx.scene.control.TextField;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * This class is a TextField which implements an "autocomplete" functionality, based on a supplied list of entries.
 * @author Caleb Brinkman
 */
public class AutoCompleteTextField extends TextField  {
    /** The existing autocomplete entries. */
    private final SortedSet<String> entries = new TreeSet<>();
    private final ChangeListenerHistoryHint changeListenerHistoryHint;


    public AutoCompleteTextField() {
        this("");
    }

    public AutoCompleteTextField(String text) {
        super(text);

        changeListenerHistoryHint = new ChangeListenerHistoryHint(this, entries);

    }

    public SortedSet<String> getEntries() {
        return entries;
    }
}