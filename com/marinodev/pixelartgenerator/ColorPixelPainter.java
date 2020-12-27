package com.marinodev.pixelartgenerator;

import java.awt.*;

public class ColorPixelPainter extends PixelPainter{
    public Color currentColor = Color.BLUE;
    public Color bgColor = Color.WHITE;

    public ColorPixelPainter(PixelPanel panel) {
        super(panel);
        // add logic to paint pixels
        addLeftClickListener(pixel -> {
            pixel.setColor(currentColor);
            pixel.repaint();
        });
        addRightClickListener(pixel -> {
            pixel.setColor(bgColor);
            pixel.repaint();
        });
    }
}
