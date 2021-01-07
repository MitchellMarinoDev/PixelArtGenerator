package com.marinodev.pixelartgenerator;

import java.awt.*;

public class PaintablePixelPanel extends PixelArtPanel {
    private PixelPainter painter;

    public PaintablePixelPanel(int x, int y, int pixelSize) {
        super(x, y, pixelSize);
    }

    @Override
    public void rebuildPixels(int x, int y, int pixelSize, Color bgColor, Color borderColor) {
        super.rebuildPixels(x, y, pixelSize, bgColor, borderColor);
        if (painter != null)
            painter.onRebuildPixels();
    }

    public PixelPainter getPainter() {
        return painter;
    }
    public void setPainter(PixelPainter painter) {
        this.painter = painter;
    }
}