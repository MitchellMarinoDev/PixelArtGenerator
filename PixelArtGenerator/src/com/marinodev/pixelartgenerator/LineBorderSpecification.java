package com.marinodev.pixelartgenerator;

import java.awt.*;

public class LineBorderSpecification {
    public int thickness;
    public Color color;

    public LineBorderSpecification() {
        thickness = 1;
        color = Color.BLACK;
    }

    public LineBorderSpecification(Color color, int thickness) {
        this.thickness = thickness;
        this.color = color;
    }

    public void copy(LineBorderSpecification other) {
        this.thickness = other.thickness;
        this.color = other.color;
    }
}
