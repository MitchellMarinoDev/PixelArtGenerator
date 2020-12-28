package com.marinodev.pixelartgenerator;

import java.awt.*;

public class ColorPixelPainter extends PixelPainter {
    public Color currentColor = Color.BLUE;
    public Color bgColor = Color.WHITE;

    public ColorPixelPainter(PixelArtPanel panel) {
        super(panel);
        // add logic to paint pixels
        addLeftClickListener(pixel -> {
            pixel.color = currentColor;
            panel.paintPixel(pixel);
        });
        addRightClickListener(pixel -> {
            pixel.color = bgColor;
            panel.paintPixel(pixel);
        });
    }
}
