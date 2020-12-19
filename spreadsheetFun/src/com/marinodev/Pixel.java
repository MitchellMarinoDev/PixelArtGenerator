package com.marinodev;

import javax.swing.*;
import java.awt.*;

public class Pixel extends JPanel {
    private int size;
    private Color color;

    public Pixel(int size) {
        this.size = size;
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
}
