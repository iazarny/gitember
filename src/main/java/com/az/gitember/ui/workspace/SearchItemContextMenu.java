package com.az.gitember.ui.workspace;

import com.az.gitember.service.I18n;
import com.az.gitember.service.Context;
import com.az.gitember.service.SearchService;
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

    private final JMenuItem openItem = new JMenuItem(I18n.t("open"));
    private final JMenuItem deleteItem = new JMenuItem(I18n.t("searchItemContextMenu.delete"));
    private final JMenuItem historyItem = new JMenuItem(I18n.t("showHistory"));

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
                            Context.initRepoOnly(hit.getProject());
                            JFrame frame = new JFrame(I18n.t("commitDetailPanel.history", hit.getLeafName()));
                            frame.setSize(1000, 600);
                            frame.setLocationRelativeTo(parent);
                            HistoryPanel hp = new HistoryPanel(MainFrame.getInstance().getStatusBar());
                            frame.getContentPane().add(hp);
                            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                            frame.addWindowListener(new java.awt.event.WindowAdapter() {
                                @Override
                                public void windowClosed(java.awt.event.WindowEvent e) {
                                    hp.dispose();
                                }
                            });
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
                                I18n.t("physicallyDeleteCannotUndone", hit.getLeafName()),
                                I18n.t("workingCopyContextMenu.delete"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
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
                SearchHitOpener.open(hit);
            }
        });
    }

    public void setSelectedNode(DefaultMutableTreeNode selectedNode) {
        this.selectedNode = selectedNode;
    }
}
