package com.az.gitember.ui.workspace;

import com.az.gitember.service.Context;
import com.az.gitember.service.ExtensionMap;
import com.az.gitember.service.SearchService;
import com.az.gitember.ui.FileViewerWindow;
import com.az.gitember.ui.HistoryPanel;
import com.az.gitember.ui.MainFrame;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SearchItemContextMenu  extends JPopupMenu {

    private static final Logger log = Logger.getLogger(SearchItemContextMenu.class.getName());

    private final  JTree searchTree;
    private final  Component parent;

    private final JMenuItem openItem = new JMenuItem("Open");
    private final JMenuItem deleteItem = new JMenuItem("Delete ...");
    private final JMenuItem historyItem = new JMenuItem("Show history");

    private DefaultMutableTreeNode selectedNode;

    public SearchItemContextMenu(Component parent, JTree searchTree) {
        this.searchTree = searchTree;
        this.parent = parent;
        this.add(openItem);
        this.addSeparator();
        this.add(historyItem);
        this.add(deleteItem);

        historyItem.addActionListener(
                evt -> {
                    if (selectedNode.getUserObject() instanceof SearchHit hit) {
                        try {
                            Context.initRepoOnly(hit.getProject().getProjectHomeFolder());
                            JFrame frame = new JFrame("History: " + hit.getLeafName());
                            frame.setSize(1000, 600);
                            frame.setLocationRelativeTo(parent);
                            HistoryPanel hp = new HistoryPanel(MainFrame.getInstance().getStatusBar());
                            frame.getContentPane().add(hp);
                            frame.setVisible(true);
                            hp.loadFileHistory(hit.getPath());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }

                    }

                }
        );

        deleteItem.addActionListener(
                evt -> {
                    if (selectedNode.getUserObject() instanceof SearchHit hit) {
                        String fileName = hit.getProject().getProjectHomeFolder() + File.separator + hit.getPath();

                        int c = JOptionPane.showConfirmDialog(parent,
                                "Physically delete '" + hit.getLeafName() + "'?\nThis cannot be undone.",
                                "Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                        if (c == JOptionPane.YES_OPTION)  {
                                try (SearchService searchService = SearchService.forProject(hit.getProject())) {
                                    searchService.deleteFileDoc(hit.getPath());
                                    Files.deleteIfExists(Paths.get(fileName));
                                    DefaultTreeModel model = (DefaultTreeModel) searchTree.getModel();
                                    model.removeNodeFromParent(selectedNode);
                                } catch (Exception ex) {
                                    log.log(Level.WARNING, "Cannot delete file: " + fileName, ex);
                                }
                        }
                    }
                }
        );

        openItem.addActionListener(evt -> {
            if (selectedNode.getUserObject() instanceof SearchHit hit) {
                String fileName = hit.getProject().getProjectHomeFolder() + File.separator + hit.getPath();
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
