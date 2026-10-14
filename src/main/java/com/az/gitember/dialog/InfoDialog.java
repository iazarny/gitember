package com.az.gitember.dialog;

import com.az.gitember.service.I18n;
import com.az.gitember.service.Context;
import com.az.gitember.ui.misc.Util;

import javax.swing.*;
import java.awt.*;
import java.util.stream.Collectors;

public class InfoDialog extends JDialog {

    public InfoDialog(JFrame parentFrame) {
        super(SwingUtilities.getWindowAncestor(parentFrame), I18n.t("info"),
                ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(640, 320);
        setLocationRelativeTo(parentFrame);

        String text = Context.getGitRepoService().getCommandLine().stream()
                .collect(Collectors.joining("\n"));

        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setText(text);

        JScrollPane scrollPane = new JScrollPane(textArea);



        JButton clear = new JButton(I18n.t("clear"));
        clear.addActionListener(e -> {
            textArea.setText("");
            Context.getGitRepoService().getCommandLine().clear();
            dispose();
        });

        JButton closeBtn = new JButton(I18n.t("close"));
        closeBtn.addActionListener(e -> dispose());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        btnPanel.add(clear);
        btnPanel.add(closeBtn);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        //mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane,   BorderLayout.CENTER);
        mainPanel.add(btnPanel,    BorderLayout.SOUTH);
        setContentPane(mainPanel);


        Util.bindEscapeToDispose(this);

    }

}
