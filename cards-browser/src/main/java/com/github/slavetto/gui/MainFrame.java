package com.github.slavetto.gui;

import javax.swing.*;

public class MainFrame extends JFrame {
    private JPanel contentPane;
    private JButton buttonOK;
    private JTextField textField1;
    private JButton browseForApkgFileBtn;
    private JList whatDecksList;
    private JTextField textField2;
    private JButton browseButton;
    private JList whatCategoriesList;
    private JButton startBtn;
    private JCheckBox randomizeCardsPositionsCheckBox;

    public MainFrame() {
        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonOK);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            //For some reason we could not load the OS's look and feel. We'll just continue with the default one.
            e.printStackTrace();
        }

        MainFrame dialog = new MainFrame();
        dialog.pack();
        dialog.setResizable(false);
        dialog.setVisible(true);
    }

}
