package com.marinodev.pixelartgenerator;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.*;

import java.util.List;
import java.util.function.Consumer;

public class GUI extends JFrame {
    private JPanel mainPanel;
    //region SwingElements
    // QUESTION ANSWER TABLE PANEL
    private JTable questionAnsTable;
    private DefaultTableModel questionAnsTableModel;
    private JButton addQuestion;
    private JButton removeQuestionButton;

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

        $$$setupUI$$$();
        setUpTable();
        // set up a listener for the add row button for the table

        // SETUP GUI COMPONENTS
        increaseScrollSpeeds();
        imageScalerSettings();

        setupPixelArt();
        setupGroup();
        setupPallet();
        setUpSpinners();
        bgColorButton.setBackground(bgColor);

        // SETUP BUTTONS
        pickImageButton.addActionListener(e -> pickImage());
        groupSpinner.addChangeListener(updateGroupSpinner);

        rebuildPreviewButton.addActionListener(e -> imageScaler.buildImagePreview());
        buildPixelArtButton.addActionListener(buildPixelArt);

        colorChooser.getSelectionModel().addChangeListener(onColorSelected);

        ((ColorPixelPainter) pixelArtPanel.getPainter()).addColorPickedListener(eyedropColor);
        removeQuestionButton.addActionListener(removeQuestion);


        super.setPreferredSize(new Dimension(800, 600));
        super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        super.setContentPane(mainPanel);
        super.pack();

        // ADD BUTTON LISTENERS
        palletButton0.addActionListener(e -> setActivePalletIndex(0));
        palletButton1.addActionListener(e -> setActivePalletIndex(1));
        palletButton2.addActionListener(e -> setActivePalletIndex(2));
        palletButton3.addActionListener(e -> setActivePalletIndex(3));

        // this call is necessary on Mac
        palletButton0.setOpaque(true);
        palletButton1.setOpaque(true);
        palletButton2.setOpaque(true);
        palletButton3.setOpaque(true);

        addQuestion.addActionListener(e -> addRowQuestions());
        bakeButton.addActionListener(bake);

        bgColorButton.addActionListener(switchBGColor);
        bgColorButton.setOpaque(true);

        buildButton.addActionListener(e -> buildSpreadsheet(new XLSXSpreadsheetBuilder()));
        buildGoogleSheetButton.addActionListener(e -> buildSpreadsheet(new GoogleSheetBuilder()));
    }

    // Custom Create Components
    private void createUIComponents() {
        questionAnsTable = new LockedTable();
    }

    //region button functions
    final ActionListener buildPixelArt = e -> {
        int width = (Integer) widthSpinner.getValue();
        int height = (Integer) heightSpinner.getValue();
        int sizeOfPixel = (Integer) sizeOfPixelSpinner.getValue();

        Color borderColor = bgColor.equals(Color.WHITE) ? Color.BLACK : Color.WHITE;
        pixelArtPanel.rebuildPixels(width, height, sizeOfPixel, bgColor, borderColor);
        try {
            BufferedImage smallImg = imageScaler.getSmallImg();
            Color[][] imageData = imageToPixelData(smallImg);
            pixelArtPanel.setPixelColors(imageData, bgColor);
        } catch (NullPointerException ex) {
            // no image selected, rebuild only
        }
    };

    final ChangeListener onColorSelected = e -> {
        pallet[activePalletIndex].setBackground(colorChooser.getColor());
        setActivePalletIndex(activePalletIndex); // refresh color
    };

    final ChangeListener updateGroupSpinner = e -> {
        clampGroupSpinner();
        ((GroupedBoarderPixelPainter) grouperPanel.getPainter()).setCurrentGroup((Integer) groupSpinner.getValue());
    };

    final ActionListener removeQuestion = e -> {
        if (questionAnsTableModel.getRowCount() > 1) // keep 1 row in table
            removeRowQuestions();
    };

    final ActionListener bake = e -> {
        Pixel[][] pixels = pixelArtPanel.getPixels();
        grouperPanel.rebuildPixels(pixelArtPanel.getNPixelsX(), pixelArtPanel.getNPixelsY(), pixelArtPanel.getPixelSize(), Color.WHITE, Color.BLACK);
        grouperPanel.copyPixels(pixels);
    };

    final ActionListener switchBGColor = e -> {
        if (bgColor.equals(Color.BLACK))
            bgColor = Color.WHITE;
        else if (bgColor.equals(Color.WHITE))
            bgColor = Color.BLACK;
        else
            throw new IllegalStateException("bgColor must be equal to Color.BLACK or Color.WHITE");

        ((ColorPixelPainter) pixelArtPanel.getPainter()).bgColor = bgColor;
        bgColorButton.setBackground(bgColor);
    };

    //Consumer<ChangeEvent> bake =

    final Consumer<Color> eyedropColor = color -> {
        // if a color in the pallet matches, select it. else set the current pallet color to it
        for (int i = 0, palletLength = pallet.length; i < palletLength; i++) {
            if (pallet[i].getBackground().equals(color)) {
                setActivePalletIndex(i);
                return;
            }
        }
        // if none of the above apply, set selected pallet to the color
        pallet[activePalletIndex].setBackground(color);
        setActivePalletIndex(activePalletIndex);
    };
    //endregion

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

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */

    private void $$$setupUI$$$() {
        createUIComponents();
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        final JTabbedPane tabbedPane1 = new JTabbedPane();
        mainPanel.add(tabbedPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(200, 200), null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane1.addTab("Questions", panel1);
        final JScrollPane scrollPane1 = new JScrollPane();
        panel1.add(scrollPane1, new GridConstraints(0, 0, 2, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        questionAnsTable.setAutoResizeMode(2);
        questionAnsTable.setFillsViewportHeight(false);
        scrollPane1.setViewportView(questionAnsTable);
        addQuestion = new JButton();
        addQuestion.setText("Add Question");
        panel1.add(addQuestion, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        removeQuestionButton = new JButton();
        removeQuestionButton.setText("Remove Question");
        panel1.add(removeQuestionButton, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(6, 3, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane1.addTab("Pick Image", panel2);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(9, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(panel3, new GridConstraints(0, 0, 4, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        pickImageButton = new JButton();
        pickImageButton.setText("Pick Image");
        panel3.add(pickImageButton, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        widthSpinner = new JSpinner();
        widthSpinner.setToolTipText("Width Of Pixel Art");
        panel3.add(widthSpinner, new GridConstraints(2, 0, 2, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        heightSpinner = new JSpinner();
        heightSpinner.setToolTipText("Height of Pixel Art");
        panel3.add(heightSpinner, new GridConstraints(2, 1, 2, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Width");
        panel3.add(label1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Height");
        panel3.add(label2, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buildPixelArtButton = new JButton();
        buildPixelArtButton.setText("Build Pixel Art");
        panel3.add(buildPixelArtButton, new GridConstraints(7, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        rebuildPreviewButton = new JButton();
        rebuildPreviewButton.setText("Rebuild Preview");
        panel3.add(rebuildPreviewButton, new GridConstraints(6, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sizeOfPixelSpinner = new JSpinner();
        panel3.add(sizeOfPixelSpinner, new GridConstraints(5, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Size of each Pixel");
        panel3.add(label3, new GridConstraints(4, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Background Color:");
        panel3.add(label4, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        bgColorButton = new JButton();
        bgColorButton.setText("");
        panel3.add(bgColorButton, new GridConstraints(8, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(40, 20), null, 1, false));
        final Spacer spacer1 = new Spacer();
        panel2.add(spacer1, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel2.add(spacer2, new GridConstraints(5, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        imagePreviewScrollPane = new JScrollPane();
        panel2.add(imagePreviewScrollPane, new GridConstraints(0, 1, 5, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        imagePreview = new JLabel();
        imagePreview.setText("");
        imagePreviewScrollPane.setViewportView(imagePreview);
        final JTabbedPane tabbedPane2 = new JTabbedPane();
        tabbedPane1.addTab("Pixel Art", tabbedPane2);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane2.addTab("Painter", panel4);
        pixelArtScroll = new JScrollPane();
        panel4.add(pixelArtScroll, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        pixelArtParent = new JPanel();
        pixelArtParent.setLayout(new BorderLayout(0, 0));
        pixelArtScroll.setViewportView(pixelArtParent);
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(2, 5, new Insets(0, 0, 0, 0), -1, -1));
        pixelArtParent.add(panel5, BorderLayout.CENTER);
        final JLabel label5 = new JLabel();
        label5.setText("Pallet:");
        panel5.add(label5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        palletButton0 = new JButton();
        palletButton0.setBackground(new Color(-16776961));
        palletButton0.setForeground(new Color(-4539461));
        palletButton0.setHideActionText(false);
        palletButton0.setText("");
        panel5.add(palletButton0, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(40, 20), null, 0, false));
        palletButton1 = new JButton();
        palletButton1.setBackground(new Color(-16776961));
        palletButton1.setForeground(new Color(-4539461));
        palletButton1.setHideActionText(false);
        palletButton1.setText("");
        panel5.add(palletButton1, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(40, 20), null, 0, false));
        palletButton2 = new JButton();
        palletButton2.setBackground(new Color(-16776961));
        palletButton2.setForeground(new Color(-4539461));
        palletButton2.setHideActionText(false);
        palletButton2.setText("");
        panel5.add(palletButton2, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(40, 20), null, 0, false));
        palletButton3 = new JButton();
        palletButton3.setBackground(new Color(-16776961));
        palletButton3.setForeground(new Color(-4539461));
        palletButton3.setHideActionText(false);
        palletButton3.setText("");
        panel5.add(palletButton3, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(40, 20), null, 0, false));
        bakeButton = new JButton();
        bakeButton.setText("Bake");
        panel5.add(bakeButton, new GridConstraints(1, 0, 1, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane2.addTab("Color Chooser", panel6);
        colorChooser = new JColorChooser();
        panel6.add(colorChooser, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane1.addTab("Grouper", panel7);
        final JScrollPane scrollPane2 = new JScrollPane();
        panel7.add(scrollPane2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        groupParent = new JPanel();
        groupParent.setLayout(new BorderLayout(0, 0));
        scrollPane2.setViewportView(groupParent);
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        groupParent.add(panel8, BorderLayout.NORTH);
        final JLabel label6 = new JLabel();
        label6.setText("Group:");
        panel8.add(label6, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        groupSpinner = new JSpinner();
        panel8.add(groupSpinner, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel9 = new JPanel();
        panel9.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel8.add(panel9, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buildButton = new JButton();
        buildButton.setText("Build XLSX");
        panel9.add(buildButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buildGoogleSheetButton = new JButton();
        buildGoogleSheetButton.setText("Build Google Sheet");
        panel9.add(buildGoogleSheetButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }
    //endregion
}
