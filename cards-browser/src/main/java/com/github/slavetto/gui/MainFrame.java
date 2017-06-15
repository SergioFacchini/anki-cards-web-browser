package com.github.slavetto.gui;

import com.github.slavetto.exporter.AnkiExpectedExportingException;
import com.github.slavetto.exporter.ExportOptions;
import com.github.slavetto.exporter.Exporter;
import com.github.slavetto.gui.viewmodels.DeckWithCardNumber;
import com.github.slavetto.gui.viewmodels.DecksWithTags;
import com.github.slavetto.parser.APKGParser;
import com.github.slavetto.parser.exceptions.AnkiDatabaseNotFoundException;
import com.github.slavetto.parser.models.DeckInfo;
import com.github.slavetto.parser.models.DeckInfos;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import net.lingala.zip4j.exception.ZipException;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class MainFrame extends JFrame {
    private JPanel contentPane;
    private JTextField apkgFilePathText;
    private JButton browseForApkgFileBtn;
    private JList<DeckWithCardNumber> whatDecksList;
    private JTextField exportFolderText;
    private JButton browseForDestinationFolder;
    private JList<DecksWithTags> whatCategoriesList;
    private JButton exportBtn;
    private JCheckBox randomizeCardsPositionsCheckBox;
    private JLabel apkgSelectedStatus;
    private JLabel howManyCardsSelectedForExport;
    private JTextField cardsAuthorsNames;
    private JTextField sidebarNotes;

    /**
     * It's true if the user has just selected the deck and the calculation of the cards numbers etc.. are taking place.
     */
    private boolean isFirstSetupInProgress = false;

    /**
     * The parser associated to the currently selected .apkg file. It's null until no file has been selected.
     */
    private APKGParser currentParser;

    private final JFileChooser apkgChooser;
    private final JFileChooser destinationFolderChooser;

    //Exporting options
    private File destinationFolder;

    private MainFrame() {
        setTitle("Anki Cards Web Browsers generator");
        setContentPane(contentPane);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        //What decks to generate the browser for
        whatDecksList.setModel(new DefaultComboBoxModel<>());
        whatDecksList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        whatDecksList.addListSelectionListener(e -> {
            //When changing decks to export, we refresh the list of tags to export
            if (!isFirstSetupInProgress) {
                fetchTagsOfSelectedDecks();
            }
        });

        //What tags to consider for exporting
        whatCategoriesList.setModel(new DefaultComboBoxModel<>());
        whatCategoriesList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        whatCategoriesList.addListSelectionListener(x -> {
            long numCardsToExport = whatCategoriesList.getSelectedValuesList()
                    .stream()
                    .mapToLong(DecksWithTags::getNumCards)
                    .sum();

            howManyCardsSelectedForExport.setText("Cards to export: " + numCardsToExport);
        });

        //Choosers
        apkgChooser = new JFileChooser();
        apkgChooser.setFileFilter(new FileNameExtensionFilter("Anki archive (*.apkg)", "apkg"));

        destinationFolderChooser = new JFileChooser();
        destinationFolderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        //Listeners
        browseForApkgFileBtn.addActionListener(e -> {
            if (apkgChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                onAnkiFileSelected(apkgChooser.getSelectedFile());
            }
        });

        browseForDestinationFolder.addActionListener(e -> {
            if (destinationFolderChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                onDestinationFolderSelected(destinationFolderChooser.getSelectedFile());
            }
        });

        exportBtn.addActionListener(e -> performExport());

        //When closing the window, perform a cleanup
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (currentParser != null) {
                    currentParser.clear();
                }
            }
        });
    }

    private void performExport() {
        if (destinationFolder == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please, choose a destination folder",
                    "Choose a destination folder",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        //Building exporter options
        ExportOptions exportOptions = new ExportOptions();
        exportOptions.cardsAuthors = cardsAuthorsNames.getText();
        exportOptions.sidebarNotes = sidebarNotes.getText();
        exportOptions.shuffleCards = randomizeCardsPositionsCheckBox.isSelected();

        //Building exporter
        Exporter exporter = new Exporter(
                currentParser, destinationFolder,
                getTagsSelectedForExport(), exportOptions
        );

        try {
            exporter.tryExporting();

            JOptionPane.showMessageDialog(
                    this,
                    "The browser was generated correctly!",
                    "Browser generated",
                    JOptionPane.INFORMATION_MESSAGE
            );

            //Opening the export folder
            Desktop.getDesktop().open(destinationFolder);

        } catch (SQLException e1) {
            e1.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "A database error occurred when trying to generate the browser.\n" +
                            "Please check that the apgk file is not damaged. If necessary try to re-export it, " +
                            "then retry.",
                    "A database error occurred",
                    JOptionPane.ERROR_MESSAGE
            );
        } catch (IOException | AnkiExpectedExportingException e1) {
            e1.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "An error occurred when writing the browser.\n" +
                            "Please check that the folder you supplied is valid and writable. If necessary, " +
                            "change the folder and retry.",
                    "A file error occurred",
                    JOptionPane.ERROR_MESSAGE
            );
        } catch (Exception e1) {
            e1.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "An unknown error occurred when writing the browser.\nHere are the technical details:\n" +
                            e1.getMessage(),
                    "A very bad error occurred.",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void onDestinationFolderSelected(File destinationFolder) {
        this.destinationFolder = destinationFolder;

        exportFolderText.setText(destinationFolder.getAbsolutePath());
    }

    private void fetchTagsOfSelectedDecks() {
        ArrayList<DecksWithTags> deckTags = new ArrayList<>();
        try {
            for (DeckWithCardNumber deck : getDecksSelectedForExport()) {
                for (String tag : currentParser.fetchTagsOfDeck(deck.getDeckId())) {
                    long numCards = currentParser.getNumCardsHavingTagInDeck(deck.getDeckId(), tag);
                    deckTags.add(new DecksWithTags(deck.getDeckId(), deck.getDeckName(), tag, numCards));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


        whatCategoriesList.setModel(new DefaultComboBoxModel<>(new Vector<>(deckTags)));
        whatCategoriesList.setSelectionInterval(0, deckTags.size() - 1);
    }

    private List<DeckWithCardNumber> getDecksSelectedForExport() {
        return whatDecksList.getSelectedValuesList();
    }


    private List<DecksWithTags> getTagsSelectedForExport() {
        return whatCategoriesList.getSelectedValuesList();
    }

    private void fetchCardCount() {
        try {
            long numCards = currentParser.getNumCardsInAllDecks();
            apkgSelectedStatus.setText(numCards + " cards found");
        } catch (SQLException exception) {
            apkgSelectedStatus.setText("Error while retrieving cards");
        }
    }

    private void fetchDeckNames() {
        try {
            ArrayList<DeckWithCardNumber> decksForList = new ArrayList<>();
            DeckInfos deckInfos = currentParser.getDeckInfos();
            for (DeckInfo deckInfo : deckInfos) {
                long numCardsInDeck = currentParser.getNumCardsInDeck(deckInfo.getId());
                if (numCardsInDeck != 0) {
                    decksForList.add(new DeckWithCardNumber(deckInfo, numCardsInDeck));
                }
            }

            whatDecksList.setModel(new DefaultComboBoxModel<>(new Vector<>(decksForList)));
            whatDecksList.setSelectionInterval(0, decksForList.size() - 1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void onAnkiFileSelected(File selectedFile) {
        updateSelectedFileText(selectedFile);
        tryCreateParser(selectedFile);
    }

    private void updateSelectedFileText(File selectedFile) {
        apkgFilePathText.setText(selectedFile.getAbsolutePath());
    }

    private void tryCreateParser(File selectedFile) {
        currentParser = new APKGParser(selectedFile);
        try {
            currentParser.tryOpenFile();

            onValidParserSelected();
        } catch (ZipException | SQLException | IOException | AnkiDatabaseNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "An error occurred when trying to open Anki archive\n" +
                            "Please check that the selected archive is valid. If necessary re-export it again and retry.\n" +
                            "Technical error: " + e.getMessage(),
                    "An error occurred when trying to open Anki archive",
                    JOptionPane.ERROR_MESSAGE
            );

            currentParser.clear();
            currentParser = null;
        }
    }

    private void onValidParserSelected() {
        isFirstSetupInProgress = true;

        fetchCardCount();
        fetchDeckNames();
        fetchTagsOfSelectedDecks();

        isFirstSetupInProgress = false;
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

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(6, 1, new Insets(10, 10, 10, 10), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "1 - Select an *.apkg file"));
        apkgFilePathText = new JTextField();
        apkgFilePathText.setEditable(false);
        apkgFilePathText.setHorizontalAlignment(2);
        apkgFilePathText.setText("(nothing selected)");
        panel2.add(apkgFilePathText, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        browseForApkgFileBtn = new JButton();
        browseForApkgFileBtn.setText("Browse...");
        browseForApkgFileBtn.setMnemonic('B');
        browseForApkgFileBtn.setDisplayedMnemonicIndex(0);
        panel2.add(browseForApkgFileBtn, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        apkgSelectedStatus = new JLabel();
        apkgSelectedStatus.setText("Nothing selected");
        panel2.add(apkgSelectedStatus, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(450, -1), new Dimension(450, -1), null, 0, false));
        panel3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "2 - What decks do you want to generate the browser for?"));
        final JLabel label1 = new JLabel();
        label1.setText("Notning selected");
        panel3.add(label1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel3.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        whatDecksList = new JList();
        scrollPane1.setViewportView(whatDecksList);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel4, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel4.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "4 - In what folder the browser will be generated?"));
        exportFolderText = new JTextField();
        exportFolderText.setHorizontalAlignment(2);
        exportFolderText.setText("(nothing selected)");
        panel4.add(exportFolderText, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        browseForDestinationFolder = new JButton();
        browseForDestinationFolder.setText("Browse...");
        browseForDestinationFolder.setMnemonic('O');
        browseForDestinationFolder.setDisplayedMnemonicIndex(2);
        panel4.add(browseForDestinationFolder, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("WARNING: The existing files will be overwritten!");
        panel4.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel5, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel5.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "3 - What categories will have to be created? (based on tags)"));
        final JScrollPane scrollPane2 = new JScrollPane();
        panel5.add(scrollPane2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        whatCategoriesList = new JList();
        scrollPane2.setViewportView(whatCategoriesList);
        howManyCardsSelectedForExport = new JLabel();
        howManyCardsSelectedForExport.setText("Nothing selected");
        panel5.add(howManyCardsSelectedForExport, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        exportBtn = new JButton();
        exportBtn.setText("Create");
        exportBtn.setMnemonic('C');
        exportBtn.setDisplayedMnemonicIndex(0);
        contentPane.add(exportBtn, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel6, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel6.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "5 - Generation options"));
        randomizeCardsPositionsCheckBox = new JCheckBox();
        randomizeCardsPositionsCheckBox.setText("Randomize cards positions");
        randomizeCardsPositionsCheckBox.setMnemonic('R');
        randomizeCardsPositionsCheckBox.setDisplayedMnemonicIndex(0);
        panel6.add(randomizeCardsPositionsCheckBox, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Cards authors");
        panel6.add(label3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cardsAuthorsNames = new JTextField();
        panel6.add(cardsAuthorsNames, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Sidebar notes");
        panel6.add(label4, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sidebarNotes = new JTextField();
        panel6.add(sidebarNotes, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
