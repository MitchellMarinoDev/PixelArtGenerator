package com.marinodev.pixelartgenerator;

import java.awt.*;

public class LineBorderSpecification {
    public int width;
    public Color color;

    public LineBorderSpecification() {
        width = 1;
        color = new Color(0, 0, 0);
    }

    public LineBorderSpecification(int width, Color color) {
        this.width = width;
        this.color = color;
    }
}
