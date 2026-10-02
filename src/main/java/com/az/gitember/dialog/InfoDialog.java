package com.az.gitember.dialog;

import com.az.gitember.service.Context;
import com.az.gitember.ui.misc.Util;

import javax.swing.*;
import java.awt.*;
import java.util.stream.Collectors;

public class InfoDialog extends JDialog {

    public InfoDialog(JFrame parentFrame) {
        super(SwingUtilities.getWindowAncestor(parentFrame), "Info",
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



        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        btnPanel.add(closeBtn);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        //mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane,   BorderLayout.CENTER);
        mainPanel.add(btnPanel,    BorderLayout.SOUTH);
        setContentPane(mainPanel);


        Util.bindEscapeToDispose(this);

    }

}
