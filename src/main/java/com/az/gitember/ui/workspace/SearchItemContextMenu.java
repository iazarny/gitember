package com.az.gitember.ui.workspace;

import com.az.gitember.service.ExtensionMap;
import com.az.gitember.ui.FileViewerWindow;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SearchItemContextMenu  extends JPopupMenu {

    private static final Logger log = Logger.getLogger(SearchItemContextMenu.class.getName());

    private final JMenuItem openItem = new JMenuItem("Open");
    private final JMenuItem deleteItem = new JMenuItem("Delete ...");
    private final JMenuItem historyItem = new JMenuItem("Show history");

    private DefaultMutableTreeNode selectedNode;

    public SearchItemContextMenu() {
        this.add(openItem);
        this.addSeparator();
        this.add(historyItem);
        this.add(deleteItem);


        openItem.addActionListener(evt -> {
            if (selectedNode.getUserObject() instanceof SearchHit hit) {
                String fileName = hit.getProject().getProjectHomeFolder() + File.separator + hit.getPath();
                System.out.println(fileName);
                if (ExtensionMap.isTextExtension(fileName)) {
                    try {
                        String content = Files.readString(Paths.get(fileName));
                        FileViewerWindow viewer = new FileViewerWindow(hit.getLeafName(), content, hit.getPath());
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



        });
    }

    public void setSelectedNode(DefaultMutableTreeNode selectedNode) {
        this.selectedNode = selectedNode;
    }
}
