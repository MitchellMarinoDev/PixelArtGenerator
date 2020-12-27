package com.marinodev.pixelartgenerator;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageScaler {
    private BufferedImage img;
    private BufferedImage smallImg;
    private JLabel label;
    private JSpinner widthSpinner;
    private JSpinner heightSpinner;
    private int maxDisplayResX;
    private int maxDisplayResY;

    private int toX = 0;
    private int toY = 0;

    public void buildImagePreview() {
        calcToXY(); // calculate the toX and toY needed to fit image to maxDisplayRes
        smallImg = toBufferedImage(img.getScaledInstance((Integer) widthSpinner.getValue(), (Integer) heightSpinner.getValue(), Image.SCALE_AREA_AVERAGING));
        Image large = smallImg.getScaledInstance(toX, toY, Image.SCALE_SMOOTH);

        ImageIcon icon = new ImageIcon(large);
        label.setIcon(icon);
    }


    // PRIVATE
    private Image scaleImage(Image img, int toX, int toY, int resX, int resY) {
        Image small = img.getScaledInstance(resX, resY, Image.SCALE_AREA_AVERAGING);
        System.out.println();
        return small.getScaledInstance(toX, toY, Image.SCALE_SMOOTH);
    }

    private void calcToXY() {
        float imgRatio = (float) img.getWidth() / img.getHeight();

        boolean xIsLimitingAxis = xIsLimitingAxis();
        if (xIsLimitingAxis) {
            toX = img.getWidth();
            toY = (int) (toX * (1/imgRatio));

            // find a scale factor
            float sf = (float) maxDisplayResX / toX;

            // apply the scale factor
            toX *= sf;
            toY *= sf;
        } else {
            toY = img.getHeight();
            toX = (int) (toY * imgRatio);

            // find a scale factor
            float sf = (float) maxDisplayResY / toY;

            // apply the scale factor
            toX *= sf;
            toY *= sf;
        }
    }

    private boolean xIsLimitingAxis() {
        float displayRatio = (float) maxDisplayResX / maxDisplayResY;
        float imgRatio = (float) img.getWidth() / img.getHeight();
        return imgRatio > displayRatio;
    }

    public static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        BufferedImage bImage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bImage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        return bImage;
    }

    // GETTERS & SETTERS
    public Image getImg() {
        return img;
    }
    public void setImg(BufferedImage img) {
        this.img = img;
    }
    public void setLabel(JLabel label) {
        this.label = label;
    }

    public void setHeightSpinner(JSpinner heightSpinner) {
        this.heightSpinner = heightSpinner;
    }
    public void setWidthSpinner(JSpinner widthSpinner) {
        this.widthSpinner = widthSpinner;
    }

    public void setMaxDisplayResX(int maxDisplayResX) {
        this.maxDisplayResX = maxDisplayResX;
    }
    public void setMaxDisplayResY(int maxDisplayResY) {
        this.maxDisplayResY = maxDisplayResY;
    }

    public BufferedImage getSmallImg() {
        return smallImg;
    }
}