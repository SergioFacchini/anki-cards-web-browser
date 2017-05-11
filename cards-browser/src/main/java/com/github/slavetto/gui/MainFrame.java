package com.github.slavetto.gui;

import com.github.slavetto.gui.viewmodels.DeckWithCardNumber;
import com.github.slavetto.gui.viewmodels.DecksWithTags;
import com.github.slavetto.parser.APKGParser;
import com.github.slavetto.parser.exceptions.AnkiDatabaseNotFoundException;
import com.github.slavetto.parser.models.DeckInfo;
import com.github.slavetto.parser.models.DeckInfos;
import com.threerings.signals.Signal0;
import net.lingala.zip4j.exception.ZipException;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class MainFrame extends JFrame {
    private JPanel contentPane;
    private JButton buttonOK;
    private JTextField apkgFilePathText;
    private JButton browseForApkgFileBtn;
    private JList<DeckWithCardNumber> whatDecksList;
    private JTextField exportFolderText;
    private JButton browseForDestinationFolder;
    private JList<DecksWithTags> whatCategoriesList;
    private JButton startBtn;
    private JCheckBox randomizeCardsPositionsCheckBox;
    private JLabel apkgSelectedStatus;
    private JLabel howManyCardsSelectedForExport;

    /**
     * It's true if the user has just selected the deck and the calculation of the cards numbers etc.. are taking place.
     */
    private boolean isFirstSetupInProgress = false;

    /**
     * The parser associated to the currently selected .apkg file. It's null until no file has been selected.
     */
    private APKGParser currentParser;

    private JFileChooser apkgChooser;
    private JFileChooser destinationFolderChooser;

    private final Signal0 onValidParserSelected = new Signal0();

    //Exporting options
    private File destinationFolder;

    private MainFrame() {
        setTitle("Anki Cards Web Browsers generator");
        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonOK);
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

            howManyCardsSelectedForExport.setText("Cards to export: "+numCardsToExport);
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

        //Self listeners
        onValidParserSelected.connect(() -> { //Decks counter
            isFirstSetupInProgress = true;

            fetchCardCount();
            fetchDeckNames();
            fetchTagsOfSelectedDecks();

            isFirstSetupInProgress = false;
        });
    }

    private void onDestinationFolderSelected(File destinationFolder) {
        this.destinationFolder = destinationFolder;

        exportFolderText.setText(destinationFolder.getAbsolutePath());
    }

    private void fetchTagsOfSelectedDecks() {
        ArrayList<DecksWithTags> deckTags = new ArrayList<>();
        try {
            for (DeckWithCardNumber deck : getDecksSelectedForExport()) {
                for (String tag: currentParser.fetchTagsOfDeck(deck.getDeckId())) {
                    long numCards = currentParser.getNumCardsHavingTagInDeck(deck.getDeckId(), tag);
                    deckTags.add(new DecksWithTags(deck.getDeckName(), tag, numCards));
                }
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }


        whatCategoriesList.setModel(new DefaultComboBoxModel<DecksWithTags>(new Vector<>(deckTags)));
        whatCategoriesList.setSelectionInterval(0, deckTags.size() - 1);
    }

    private List<DeckWithCardNumber> getDecksSelectedForExport() {
        return whatDecksList.getSelectedValuesList();
    }

    private void fetchCardCount() {
        try {
            long numCards = currentParser.getNumCardsInAllDecks();
            apkgSelectedStatus.setText(numCards+" cards found");
        } catch(SQLException exception) {
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

            whatDecksList.setModel(new DefaultComboBoxModel<DeckWithCardNumber>(new Vector<>(decksForList)));
            whatDecksList.setSelectionInterval(0, decksForList.size() - 1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void onAnkiFileSelected(File selectedFile)  {
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

            onValidParserSelected.dispatch();
        } catch (ZipException e) {
            e.printStackTrace();

            currentParser.clear();
            currentParser = null;
        } catch (SQLException e) {
            e.printStackTrace();

            currentParser.clear();
            currentParser = null;
        } catch (AnkiDatabaseNotFoundException e) {
            e.printStackTrace();

            currentParser.clear();
            currentParser = null;
        }
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
