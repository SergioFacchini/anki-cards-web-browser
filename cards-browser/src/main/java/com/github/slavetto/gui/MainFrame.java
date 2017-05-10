package com.github.slavetto.gui;

import com.github.slavetto.gui.viewmodels.DeckWithCardNumber;
import com.github.slavetto.parser.APKGParser;
import com.github.slavetto.parser.exceptions.AnkiDatabaseNotFoundException;
import com.github.slavetto.parser.models.DeckInfo;
import com.github.slavetto.parser.models.DeckInfos;
import com.threerings.signals.Signal0;
import net.lingala.zip4j.exception.ZipException;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Vector;

public class MainFrame extends JFrame {
    private JPanel contentPane;
    private JButton buttonOK;
    private JTextField apkgFilePathText;
    private JButton browseForApkgFileBtn;
    private JList<DeckWithCardNumber> whatDecksList;
    private JTextField textField2;
    private JButton browseButton;
    private JList whatCategoriesList;
    private JButton startBtn;
    private JCheckBox randomizeCardsPositionsCheckBox;
    private JLabel apkgSelectedStatus;

    /**
     * The parser associated to the currently selected .apkg file. It's null until no file has been selected.
     */
    private APKGParser currentParser;
    private JFileChooser fileChooser;

    private final Signal0 onValidParserSelected = new Signal0();

    public MainFrame() {
        setTitle("Anki Cards Web Browsers generator");
        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonOK);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        //What decks to generate the browser for
        whatDecksList.setModel(new DefaultComboBoxModel<>());
        whatDecksList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        //Chooser
        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Anki archive (*.apkg)", "apkg"));

        //Listeners
        browseForApkgFileBtn.addActionListener(e -> {
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                onAnkiFileSelected(fileChooser.getSelectedFile());
            }
        });

        //Self listeners
        onValidParserSelected.connect(() -> { //Decks counter
            fetchCardCount();
            fetchDeckNames();
            fetchTags();
        });
    }

    private void fetchTags() {


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
            whatDecksList.setSelectionInterval(0, deckInfos.size() - 1);
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
