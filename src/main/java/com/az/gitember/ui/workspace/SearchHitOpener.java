package com.az.gitember.ui.workspace;

import com.az.gitember.service.ExtensionMap;
import com.az.gitember.ui.FileViewerWindow;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Opens a file behind a search hit - text files in the internal viewer, everything else
 * with the system default application.
 */
public class SearchHitOpener {

    private static final Logger log = Logger.getLogger(SearchHitOpener.class.getName());

    private SearchHitOpener() {
    }

    public static void open(SearchHit hit) {
        open(hit);
    }

    public static void open(SearchHit hit, String searchTerm) {
        String fileName = hit.getProject().getProjectHomeFolder() + File.separator + hit.getPath();
        if (ExtensionMap.isTextExtension(fileName)) {
            try {
                String content = Files.readString(Paths.get(fileName));
                FileViewerWindow viewer = new FileViewerWindow(
                        hit.getLeafName(),
                        content,
                        hit.getPath(),
                        searchTerm);
                viewer.setVisible(true);
            } catch (Exception ex) {
                log.log(Level.WARNING, "Cannot open file", ex);
                JOptionPane.showMessageDialog(null, "Cannot open: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            try {
                Desktop.getDesktop().open(new File(fileName));
            } catch (Exception ex) {
                log.log(Level.WARNING, "Cannot open file with system", ex);
            }
        }
    }
}