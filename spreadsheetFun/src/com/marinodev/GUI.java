package com.marinodev;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.*;
import java.util.Arrays;

public class GUI extends JFrame{
    private JPanel mainPanel;
    private JTable questionAnsTable;
    private DefaultTableModel questionAnsTableModel;
    private JButton addQuestion;
    private JButton pickImageButton;
    private JSpinner widthSpinner;
    private JSpinner heightSpinner;
    private JLabel imagePreview;
    private JScrollPane imagePreviewScrollPane;
    private JButton rebuildPreviewButton;
    private JTabbedPane tabedPane;
    private JButton buildPixelArtButton;
    private JPanel pixelArtParent;
    private JScrollPane pixelArtScroll;
    private JSpinner sizeOfPixelSpinner;
    private JButton palletButton0;
    private JButton palletButton1;
    private JButton palletButton2;
    private JButton palletButton3;
    private JColorChooser colorChooser;
    private JButton bakeButton;
    private PixelPanel pixelArtPanel;

    private final AbstractBorder inactiveBoarder = new LineBorder(Color.RED, 3);
    private final AbstractBorder activeBoarder = new LineBorder(Color.GREEN, 3);

    private final JButton[] pallet;
    private int activePalletIndex = 0;


    private final int PALLET_SIZE = 4;


    private ImageScaler imageScaler = new ImageScaler();
    private BufferedImage image;

    public GUI(String title) throws HeadlessException {
        super(title);

        setUpTable();
        // set up a listener for the add row button for the table
        addQuestion.addActionListener(e -> questionAnsTableModel.addRow(new Object[]{"", ""}));

        imagePreviewScrollPane.getVerticalScrollBar().setUnitIncrement(16);   // increase scrolling speed of image preview
        imagePreviewScrollPane.getHorizontalScrollBar().setUnitIncrement(16); // increase scrolling speed of image preview
        pixelArtScroll.getVerticalScrollBar().setUnitIncrement(16);   // increase scrolling speed of pixelArtScroll
        pixelArtScroll.getHorizontalScrollBar().setUnitIncrement(16); // increase scrolling speed of pixelArtScroll
        imageScalerSettings(); // set up imageScaler

        setupPixelArt();

        pallet = new JButton[PALLET_SIZE];
        pallet[0] = palletButton0;
        pallet[1] = palletButton1;
        pallet[2] = palletButton2;
        pallet[3] = palletButton3;

        palletButton0.setBorder(new LineBorder(Color.GREEN, 3));
        palletButton1.setBorder(new LineBorder(Color.RED,   3));
        palletButton2.setBorder(new LineBorder(Color.RED,   3));
        palletButton3.setBorder(new LineBorder(Color.RED,   3));



        setUpSpinners();

        this.setPreferredSize(new Dimension(800, 600));

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(mainPanel);
        this.pack();

        pickImageButton.addActionListener(e -> pickImage());
        rebuildPreviewButton.addActionListener(e -> imageScaler.buildImagePreview());

        buildPixelArtButton.addActionListener(e -> {
            pixelArtPanel.rebuildPixels((Integer) widthSpinner.getValue(), (Integer) heightSpinner.getValue(), (Integer) sizeOfPixelSpinner.getValue());
            BufferedImage smallImg = imageScaler.getSmallImg();
            System.out.println(imageScaler.getSmallImg() == null);
            Color[][] imageData = imageToPixelData(smallImg);
            boolean hasAlphaChannel = smallImg.getAlphaRaster() != null;
            pixelArtPanel.paintPixels(imageData, hasAlphaChannel);
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
            Color pixelColor = pixel.getColor();
            // if a color in the pallet matches, select it. else set the current pallet color to it
            for (int i = 0, palletLength = pallet.length; i < palletLength; i++) {
                if (pallet[i].getBackground().equals(pixelColor)) {
                    setActivePalletIndex(i);
                    return;
                }
            }
            pallet[activePalletIndex].setBackground(pixelColor);
            setActivePalletIndex(activePalletIndex);
        });

    }

    public static void main(String[] args) {
        JFrame frame = new GUI("Pixel Art Quiz Generator");
        frame.setVisible(true);
        // create table data
    }

    private void imageScalerSettings() {
        imageScaler.setLabel(imagePreview);
        imageScaler.setHeightSpinner(heightSpinner);
        imageScaler.setWidthSpinner(widthSpinner);
        imageScaler.setMaxDisplayResX(800);
        imageScaler.setMaxDisplayResY(800);
    }

    private void setUpTable() {
        // Set up Questions Column
        TableColumn questionCol = new TableColumn();
        questionCol.setHeaderValue("Questions");
        questionCol.setMinWidth(100);
        questionCol.setWidth(questionAnsTable.getWidth()-100);

        // Set up Answers Column
        TableColumn answersCol = new TableColumn();
        answersCol.setHeaderValue("Answers");
        answersCol.setMinWidth(100);

        questionAnsTableModel = new DefaultTableModel();
        questionAnsTableModel.addColumn("Questions");
        questionAnsTableModel.addColumn("Answers");

        // Add Columns
        questionAnsTable.setModel(questionAnsTableModel);
        questionAnsTable.getColumnModel().getColumn(0).setMinWidth(100);
        questionAnsTable.getColumnModel().getColumn(0).setPreferredWidth(500);
        questionAnsTable.getColumnModel().getColumn(1).setMinWidth(100);
    }

    private void pickImage() {
        var fileSelector = new JFileChooser();
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

            System.out.println("Opening: " + file.getName());
        } else {
            System.out.println("Open command cancelled by user");
        }
    }

    private void setActivePalletIndex(int index) {
        pallet[activePalletIndex].setBorder(inactiveBoarder);
        activePalletIndex = index;
        pallet[activePalletIndex].setBorder(activeBoarder);
        Color currentColor = pallet[activePalletIndex].getBackground();
        pixelArtPanel.getPainter().currentColor = currentColor;
    }


    private void setUpSpinners() {
        SpinnerModel smH = new SpinnerNumberModel(20, 1, 999, 1);
        SpinnerModel smW = new SpinnerNumberModel(20, 1, 999, 1);
        // Size of Pixel Spinner
        SpinnerModel smSOP = new SpinnerNumberModel(20, 4, 80, 1);

        widthSpinner .setModel(smW);
        heightSpinner.setModel(smH);
        sizeOfPixelSpinner.setModel(smSOP);
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



    private void setupPixelArt() {
        pixelArtPanel = new PixelPanel(20, 20, 20);
        pixelArtParent.add(pixelArtPanel, BorderLayout.SOUTH, 1);
    }

    private void createUIComponents() {

    }
}