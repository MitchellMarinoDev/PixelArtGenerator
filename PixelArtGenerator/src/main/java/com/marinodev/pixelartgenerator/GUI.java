package com.marinodev.pixelartgenerator;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.*;

import java.util.List;

public class GUI extends JFrame {
    private JPanel mainPanel;
    //region SwingElements
    // QUESTION ANSWER TABLE PANEL
    private JTable questionAnsTable;
    private DefaultTableModel questionAnsTableModel;
    private JButton addQuestion;
    private JButton removeQuestion;

    // PICK IMAGE PANEL
    private JButton pickImageButton;
    private JSpinner widthSpinner;
    private JSpinner heightSpinner;
    private JSpinner sizeOfPixelSpinner;

    private JButton rebuildPreviewButton;
    private JButton buildPixelArtButton;
    private JScrollPane imagePreviewScrollPane;
    private JLabel imagePreview;
    private JButton bgColorButton;

    // PIXEL ART PANEL
    private JColorChooser colorChooser;
    private PaintablePixelPanel pixelArtPanel;
    private JButton bakeButton;

    private JButton palletButton0;
    private JButton palletButton1;
    private JButton palletButton2;
    private JButton palletButton3;
    private JPanel pixelArtParent;
    private JScrollPane pixelArtScroll;

    // GROUPER PANEL
    private JPanel groupParent;
    private JSpinner groupSpinner;
    private JButton buildButton;
    private JButton buildGoogleSheetButton;
    private PaintablePixelPanel grouperPanel;
    //endregion

    private Color bgColor = Color.WHITE;

    private final AbstractBorder inactiveBoarder = new LineBorder(Color.RED, 3);
    private final AbstractBorder activeBoarder = new LineBorder(Color.GREEN, 3);

    private JButton[] pallet;
    private int activePalletIndex = 0;

    private final ImageScaler imageScaler = new ImageScaler();
    private BufferedImage image;

    public GUI(String title) throws HeadlessException {
        super(title);
        System.out.println(SheetsQuickstart.TOKENS_DIRECTORY_PATH);

        setUpTable();
        // set up a listener for the add row button for the table
        addQuestion.addActionListener(e -> addRowQuestions());

        increaseScrollSpeeds();
        imageScalerSettings();

        setupPixelArt();
        setupGroup();
        setupPallet();
        setUpSpinners();

        pickImageButton.addActionListener(e -> pickImage());
        rebuildPreviewButton.addActionListener(e -> imageScaler.buildImagePreview());

        buildPixelArtButton.addActionListener(e -> {
            pixelArtPanel.rebuildPixels((Integer) widthSpinner.getValue(), (Integer) heightSpinner.getValue(), (Integer) sizeOfPixelSpinner.getValue());
            BufferedImage smallImg = imageScaler.getSmallImg();
            Color[][] imageData = imageToPixelData(smallImg);
            pixelArtPanel.setPixelColors(imageData, bgColor);
            pack();
        });

        palletButton0.addActionListener(e -> setActivePalletIndex(0));
        palletButton1.addActionListener(e -> setActivePalletIndex(1));
        palletButton2.addActionListener(e -> setActivePalletIndex(2));
        palletButton3.addActionListener(e -> setActivePalletIndex(3));

        colorChooser.getSelectionModel().addChangeListener(e -> {
            pallet[activePalletIndex].setBackground(colorChooser.getColor());
            setActivePalletIndex(activePalletIndex); // refresh color
        });

        pixelArtPanel.getPainter().addMiddleClickListener(pixel -> {
            Color pixelColor = pixel.color;
            // if a color in the pallet matches, select it. else set the current pallet color to it
            for (int i = 0, palletLength = pallet.length; i < palletLength; i++) {
                if (pallet[i].getBackground().equals(pixelColor)) {
                    setActivePalletIndex(i);
                    return;
                }
            }
            // if none of the above apply, set selected pallet to the color
            pallet[activePalletIndex].setBackground(pixelColor);
            setActivePalletIndex(activePalletIndex);
        });

        groupSpinner.addChangeListener(e -> {
            clampGroupSpinner();
            ((GroupedBoarderPixelPainter) grouperPanel.getPainter()).setCurrentGroup((Integer) groupSpinner.getValue());
        });
        removeQuestion.addActionListener(e -> {
            if (questionAnsTableModel.getRowCount() > 1) // keep 1 row in table
                removeRowQuestions();
        });

        this.setPreferredSize(new Dimension(800, 600));

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(mainPanel);
        this.pack();
        bakeButton.addActionListener(e -> {
            Pixel[][] pixels = pixelArtPanel.getPixels();
            grouperPanel.rebuildPixels(pixelArtPanel.getNPixelsX(), pixelArtPanel.getNPixelsY(), pixelArtPanel.getPixelSize());
            grouperPanel.copyPixels(pixels);
        });
        buildButton.addActionListener(e -> buildSpreadsheet(new XLSXSpreadsheetBuilder()));
        bgColorButton.setBackground(bgColor);
        bgColorButton.addActionListener(e -> {
            if (bgColor.equals(Color.BLACK))
                bgColor = Color.WHITE;
            else if (bgColor.equals(Color.WHITE))
                bgColor = Color.BLACK;
            else
                throw new IllegalStateException("bgColor must be equal to Color.BLACK or Color.WHITE");
            bgColorButton.setBackground(bgColor);
        });
        buildGoogleSheetButton.addActionListener(e -> buildSpreadsheet(new SheetsQuickstart()));
    }

    // Custom Create Components
    private void createUIComponents() {
        questionAnsTable = new LockedTable();
    }


    //region HELPER FUNCTIONS
    private void buildSpreadsheet(SpreadsheetBuilder builder) {
        // Generate data array from table
        // [ROW][COL]
        String[][] data = new String[questionAnsTableModel.getRowCount()][questionAnsTableModel.getColumnCount() - 1];
        for (int row = 0; row < questionAnsTableModel.getRowCount(); row++) {
            for (int col = 1; col < questionAnsTableModel.getColumnCount(); col++)
                data[row][col - 1] = (String) questionAnsTableModel.getValueAt(row, col);
        }
        List<List<Pixel>> groupedPixels = ((GroupedBoarderPixelPainter) grouperPanel.getPainter()).getPixelGroups();

        builder.buildSheet(this, pixelArtPanel.getNPixelsX(), pixelArtPanel.getNPixelsY(), data, groupedPixels, bgColor);
    }
    private void pickImage() {
        JFileChooser fileSelector = new JFileChooser();
        int returnVal = fileSelector.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileSelector.getSelectedFile();

            try {
                image = ImageIO.read(file);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            assert image != null;

            imageScaler.setImg(image);
            imageScaler.buildImagePreview();
        }
    }
    private void setActivePalletIndex(int index) {
        pallet[activePalletIndex].setBorder(inactiveBoarder);
        activePalletIndex = index;
        pallet[activePalletIndex].setBorder(activeBoarder);
        ((ColorPixelPainter) pixelArtPanel.getPainter()).currentColor = pallet[activePalletIndex].getBackground();
    }
    private static Color[][] imageToPixelData(BufferedImage image) {
        final int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        final int width = image.getWidth();
        final int height = image.getHeight();
        final boolean hasAlphaChannel = image.getAlphaRaster() != null;

        Color[][] result = new Color[width][height];
        for (int pixel = 0, row = 0, col = 0; pixel + 1 < pixels.length; pixel++) {
            Color color = new Color(pixels[pixel], hasAlphaChannel);
            result[col][row] = color;

            if (++col == width) {
                col = 0;
                row++;
            }
        }
        return result;
    }
    private void clampGroupSpinner() {
        if ((Integer) groupSpinner.getValue() > questionAnsTableModel.getRowCount() - 1)
            groupSpinner.setValue(questionAnsTableModel.getRowCount() - 1);
    }
    private void addRowQuestions() {
        questionAnsTableModel.addRow(new Object[]{questionAnsTableModel.getRowCount(), "", ""});
    }
    private void removeRowQuestions() {
        questionAnsTableModel.removeRow(questionAnsTableModel.getRowCount() - 1);
        ((GroupedBoarderPixelPainter) grouperPanel.getPainter()).setMaxGroups(questionAnsTableModel.getRowCount());
        clampGroupSpinner();
    }
    //endregion

    //region SETUP FUNCTIONS
    private void imageScalerSettings() {
        imageScaler.setLabel(imagePreview);
        imageScaler.setHeightSpinner(heightSpinner);
        imageScaler.setWidthSpinner(widthSpinner);
        imageScaler.setMaxDisplayResX(800);
        imageScaler.setMaxDisplayResY(800);
    }
    private void setUpTable() {
        // create table
        questionAnsTableModel = new DefaultTableModel();
        questionAnsTableModel.addColumn("ID");
        questionAnsTableModel.addColumn("Questions");
        questionAnsTableModel.addColumn("Answers");
        questionAnsTable.setModel(questionAnsTableModel);

        // get columns
        TableColumn IDCol = questionAnsTable.getColumnModel().getColumn(0);
        TableColumn questionCol = questionAnsTable.getColumnModel().getColumn(1);
        TableColumn answersCol = questionAnsTable.getColumnModel().getColumn(2);

        // Set up index Column
        IDCol.setMinWidth(20);
        IDCol.setPreferredWidth(20);
        IDCol.setWidth(20);
        IDCol.setMaxWidth(20);

        // Set up Questions Column
        questionCol.setMinWidth(100);
        questionCol.setPreferredWidth(questionAnsTable.getWidth());

        // Set up Answers Column
        answersCol.setMinWidth(100);

        addRowQuestions();
    }
    private void setupPixelArt() {
        pixelArtPanel = new PaintablePixelPanel(20, 20, 20);
        pixelArtPanel.setPainter(new ColorPixelPainter(pixelArtPanel));
        pixelArtParent.add(pixelArtPanel, BorderLayout.SOUTH, 1);
    }
    private void setupPallet() {
        int PALLET_SIZE = 4;
        pallet = new JButton[PALLET_SIZE];
        pallet[0] = palletButton0;
        pallet[1] = palletButton1;
        pallet[2] = palletButton2;
        pallet[3] = palletButton3;

        palletButton0.setBorder(new LineBorder(Color.GREEN, 3));
        palletButton1.setBorder(new LineBorder(Color.RED, 3));
        palletButton2.setBorder(new LineBorder(Color.RED, 3));
        palletButton3.setBorder(new LineBorder(Color.RED, 3));
    }
    private void setupGroup() {
        //grouperPanel = new PaintablePixelPanel(20, 20, 20);
        grouperPanel = new PaintablePixelPanel(20, 20, 20);
        grouperPanel.setPainter(new GroupedBoarderPixelPainter(grouperPanel));
        groupParent.add(grouperPanel, 1);
    }
    private void increaseScrollSpeeds() {
        // increase scrolling speed of image preview
        imagePreviewScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        imagePreviewScrollPane.getHorizontalScrollBar().setUnitIncrement(16);

        // increase scrolling speed of pixelArtScroll
        pixelArtScroll.getVerticalScrollBar().setUnitIncrement(16);
        pixelArtScroll.getHorizontalScrollBar().setUnitIncrement(16);
    }
    private void setUpSpinners() {
        SpinnerModel smH = new SpinnerNumberModel(20, 1, 999, 1);
        SpinnerModel smW = new SpinnerNumberModel(20, 1, 999, 1);
        // Size of Pixel Spinner
        SpinnerModel smSOP = new SpinnerNumberModel(20, 4, 80, 1);
        // Group Spinner
        SpinnerModel groupSpinnerModel = new SpinnerNumberModel(0, 0, 999, 1);


        widthSpinner.setModel(smW);
        heightSpinner.setModel(smH);
        sizeOfPixelSpinner.setModel(smSOP);
        groupSpinner.setModel(groupSpinnerModel);
    }
    //endregion
}
