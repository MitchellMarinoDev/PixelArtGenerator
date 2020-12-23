package com.marinodev.pixelartgenerator;

import javax.swing.*;
import java.awt.*;

public class Pixel extends JPanel {
    private int size;
    private Color color;
    private int xPos;
    private int yPos;

    public Pixel(int size) {
        this.size = size;
        this.color = Color.WHITE;
        this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        this.setPreferredSize(new Dimension(size, size));
    }

    public Pixel(int size, int xPos, int yPos) {
        this.size = size;
        this.xPos = xPos;
        this.yPos = yPos;
        this.color = Color.WHITE;
        this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        this.setPreferredSize(new Dimension(size, size));
    }

    public Color getColor() {
        return color;
    }
    public void setColor(Color backgroundColor) {
        this.color = backgroundColor;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(getColor());
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    public int getXPos() {
        return xPos;
    }
    public int getYPos() {
        return yPos;
    }
}