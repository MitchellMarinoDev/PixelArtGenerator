package com.marinodev.pixelartgenerator;

import javax.swing.*;
import java.awt.*;

public class PixelArtPanel extends JPanel {
    int nPixelsX;
    int nPixelsY;
    int pixelWidth;
    Color[][] pixelColors;
    LineBorderSpecification[][] lineBorders;

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        PixelArtPanel panel = new PixelArtPanel(20, 20, 20);

        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        panel.pixelColors[0][0] = new Color(255, 255,255);
        panel.paintPixel(0, 0);
    }

    public PixelArtPanel(int nPixelsX, int nPixelsY, int pixelWidth) {
        super();
        this.nPixelsX = nPixelsX;
        this.nPixelsY = nPixelsY;
        this.pixelWidth = pixelWidth;
        pixelColors = new Color[nPixelsX][nPixelsY];
        lineBorders = new LineBorderSpecification[nPixelsX][nPixelsY];
        // set pixel colors and border specs
        for (int x = 0, pixelColorsLength = pixelColors.length; x < pixelColorsLength; x++) {
            for (int y = 0, pixelColorRowLength = pixelColors[x].length; y < pixelColorRowLength; y++) {
                pixelColors[x][y] = Color.WHITE;
                lineBorders[x][y] = new LineBorderSpecification(1, Color.BLACK);
            }
        }

        this.setPreferredSize(new Dimension(nPixelsX * pixelWidth, nPixelsY * pixelWidth));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // draw each pixel
        for (int x = 0, pixelColorsLength = pixelColors.length; x < pixelColorsLength; x++) {
            for (int y = 0, pixelColorRowLength = pixelColors[x].length; y < pixelColorRowLength; y++) {
                Color color = pixelColors[x][y];
                LineBorderSpecification spec = lineBorders[x][y];
                int xPos = pixelWidth * x;
                int yPos = pixelWidth * y;

                // draw pixel
                g.setColor(color);
                g.fillRect(xPos, yPos, pixelWidth, pixelWidth);
                // draw border
                ((Graphics2D) g).setStroke(new BasicStroke(spec.width));
                g.setColor(spec.color);
                g.drawRect(xPos, yPos, pixelWidth, pixelWidth);
            }
        }
    }

    public void setColor(int x, int y, Color color) {
        pixelColors[x][y] = color;
    }

    public void setLineBoarderSpec(int x, int y, LineBorderSpecification lineBorderSpecification) {
        lineBorders[x][y] = lineBorderSpecification;
    }

    public void paintPixel(int x, int y) {
        Graphics g = this.getGraphics();

        Color color = pixelColors[x][y];
        LineBorderSpecification spec = lineBorders[x][y];
        int xPos = pixelWidth * x;
        int yPos = pixelWidth * y;

        // draw pixel
        g.setColor(color);
        g.fillRect(xPos, yPos, pixelWidth, pixelWidth);
        // draw border
        ((Graphics2D) g).setStroke(new BasicStroke(spec.width));
        g.setColor(spec.color);
        g.drawRect(xPos, yPos, pixelWidth, pixelWidth);
    }
}
