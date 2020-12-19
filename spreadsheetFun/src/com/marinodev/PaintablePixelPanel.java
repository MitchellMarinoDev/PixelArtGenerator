package com.marinodev;

public class PaintablePixelPanel extends PixelPanel{
    private PixelPainter painter;

    public PaintablePixelPanel(int x, int y, int pixelSize) {
        super(x, y, pixelSize);
    }

    public PixelPainter getPainter() {
        return painter;
    }
    public void setPainter(PixelPainter painter) {
        this.painter = painter;
    }
}