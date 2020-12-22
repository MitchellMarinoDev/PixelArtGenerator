package com.marinodev;

import javax.swing.*;
import java.awt.*;

public class PixelPanel extends JPanel {
    private int pixelSize;
    private Pixel[][] pixels;
    private int pixelWidth;
    private int pixelHeight;

    public PixelPanel(int x, int y, int pixelSize) {
        super();
        pixelWidth = x;
        pixelHeight = y;
        this.pixelSize = pixelSize;
        this.pixels = new Pixel[x][y];

        rebuildPixels(x, y, pixelSize);
    }
    public void rebuildPixels(int x, int y, int pixelSize) {
        pixelWidth = x;
        pixelHeight = y;
        this.setLayout(new GridLayout(y, x, 0, 0));
        this.pixelSize = pixelSize;
        this.pixels = new Pixel[x][y];
        this.removeAll();
        for (int row = 0; row < y; row++) {
            for (int column = 0; column < x; column++) {
                Pixel pixel = new Pixel(this.pixelSize, column, row);
                pixels[column][row] = pixel;
                this.add(pixel);
            }
        }
    }

    public void paintPixels(int[][] data, boolean hasAlpha) {
        for (int x = 0, dataWidth = data.length; x < dataWidth; x++) {
            for (int y = 0, dataHeight = data[0].length; y < dataHeight; y++) {
                pixels[x][y].setColor(new Color(data[x][y], hasAlpha));
            }
        }
    }

    public void paintPixels(Color[][] data) {
        for (int x = 0, dataWidth = data.length; x < dataWidth; x++) {
            for (int y = 0, dataHeight = data[0].length; y < dataHeight; y++) {
                pixels[x][y].setColor(data[x][y]);
            }
        }
    }

    public void paintPixels(Pixel[][] data) {
        for (int x = 0, dataWidth = data.length; x < dataWidth; x++) {
            for (int y = 0, dataHeight = data[0].length; y < dataHeight; y++) {
                pixels[x][y].setColor(data[x][y].getColor());
            }
        }
    }

    public int getPixelWidth() {
        return pixelWidth;
    }
    public int getPixelHeight() {
        return pixelHeight;
    }

    public int getPixelSize() {
        return pixelSize;
    }
    Pixel getPixelFromIndex(int x, int y) {
        return pixels[x][y];
    }
    Pixel getPixelFromPos(int x, int y) {
        int xIndex = x / pixelSize;
        int yIndex = y / pixelSize;
        return pixels[xIndex][yIndex];
    }
    public Pixel[][] getPixels() {
        return pixels;
    }
}