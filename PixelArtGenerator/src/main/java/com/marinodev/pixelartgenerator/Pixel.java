package com.marinodev.pixelartgenerator;

import java.awt.*;

public class Pixel {
    public int x;
    public int y;
    public int size;
    public Color color;
    public LineBorderSpecification spec;

    public Pixel() {}

    public Pixel(int x, int y, int size, Color color, LineBorderSpecification spec) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.color = color;
        this.spec = spec;
    }

    public void copy(Pixel pixel) {
        this.x      = pixel.x;
        this.y      = pixel.y;
        this.size   = pixel.size;
        this.color  = pixel.color;
        this.spec   = pixel.spec;
    }
}
