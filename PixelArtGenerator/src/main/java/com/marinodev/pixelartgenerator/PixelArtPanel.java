package com.marinodev.pixelartgenerator;

import javax.swing.*;
import java.awt.*;

public class PixelArtPanel extends JPanel {
    private int nPixelsX;
    private int nPixelsY;
    private int pixelSize;
    Pixel[][] pixels;

    public PixelArtPanel(int nPixelsX, int nPixelsY, int pixelSize) {
        super();
        rebuildPixels(nPixelsX, nPixelsY, pixelSize, Color.WHITE, Color.BLACK);
    }

    public void rebuildPixels(int nPixelsX, int nPixelsY, int pixelSize, Color bgColor, Color borderColor) {
        this.nPixelsX = nPixelsX;
        this.nPixelsY = nPixelsY;
        this.pixelSize = pixelSize;
        pixels = new Pixel[nPixelsX][nPixelsY];
        // set pixel colors and border specs
        for (int x = 0, pixelsLength = pixels.length; x < pixelsLength; x++) {
            for (int y = 0, pixelRowLength = pixels[x].length; y < pixelRowLength; y++) {
                pixels[x][y] = new Pixel(x, y, pixelSize, bgColor, new LineBorderSpecification(borderColor, 1));
            }
        }
        this.setPreferredSize(new Dimension(nPixelsX * pixelSize, nPixelsY * pixelSize));
        this.setSize(new Dimension(nPixelsX * pixelSize, nPixelsY * pixelSize));
        this.repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // draw each pixel
        for (Pixel[] pixelRow : pixels) {
            for (Pixel pixel : pixelRow) {
                paintPixel(pixel, g);
            }
        }
    }

    public void paintPixel(int x, int y) {
        Pixel pixel = pixels[x][y];
        paintPixel(pixel);
    }

    public void paintPixel(Pixel pixel) {
        Graphics g = this.getGraphics();
        paintPixel(pixel, g);
    }

    private void paintPixel(Pixel pixel, Graphics g) {
        Color color = pixel.color;
        LineBorderSpecification spec = pixel.spec;
        int xPos = pixelSize * pixel.x;
        int yPos = pixelSize * pixel.y;

        // draw border as full square
        g.setColor(spec.color);
        g.fillRect(xPos, yPos, pixelSize, pixelSize);

        // draw pixel inside boarder
        g.setColor(color);
        g.fillRect(xPos + spec.thickness, yPos + spec.thickness, pixelSize - 2 * spec.thickness, pixelSize - 2 * spec.thickness);
    }

    // GETTERS AND SETTERS
    public void setPixel(int x, int y, Pixel pixel) {
        pixels[x][y] = pixel;
    }

    public void setPixels(Pixel[][] pixels) {
        for (int i = 0, pixelsLength = pixels.length; i < pixelsLength; i++) {
            for (int j = 0, pixelsSetLength = pixels[i].length; j < pixelsSetLength; j++) {
                this.pixels[i][j] = pixels[i][j];
            }
        }
        repaint();
    }

    public void copyPixels(Pixel[][] pixels) {
        for (int i = 0, pixelsLength = pixels.length; i < pixelsLength; i++) {
            for (int j = 0, pixelsSetLength = pixels[i].length; j < pixelsSetLength; j++) {
                this.pixels[i][j].copy(pixels[i][j]);
            }
        }
        repaint();
    }

    public void setPixelColors(Color[][] colors, Color bgColor) {
        for (int i = 0, colorsLength = colors.length; i < colorsLength; i++) {
            for (int j = 0, colorSetLength = colors[i].length; j < colorSetLength; j++) {
                // convert color that may have alpha to one without
                pixels[i][j].color = removeAlpha(colors[i][j], bgColor);
            }
        }
    }

    public Pixel getPixel(int x, int y) {
        return pixels[x][y];
    }
    public Pixel[][] getPixels() {
        return pixels;
    }
    public int getNPixelsX() {
        return nPixelsX;
    }
    public int getNPixelsY() {
        return nPixelsY;
    }
    public int getPixelSize() {
        return pixelSize;
    }
    public static Color removeAlpha(Color color, Color backgroundColor) {
        if (backgroundColor.equals(Color.BLACK)) {
            return new Color(color.getRGB());
        } else if (backgroundColor.equals(Color.WHITE)) {
            if (color == null)
                return backgroundColor;
            int r = color.getRed(), g = color.getGreen(), b = color.getBlue(), alpha = color.getAlpha();
            r *= ((float) alpha / 255);
            r += (255 - alpha);
            g *= ((float) alpha / 255);
            g += (255 - alpha);
            b *= ((float) alpha / 255);
            b += (255 - alpha);
            return new Color(r, g, b);
        } else
            throw new IllegalArgumentException("backgroundColor must be black or white");
    }
}
