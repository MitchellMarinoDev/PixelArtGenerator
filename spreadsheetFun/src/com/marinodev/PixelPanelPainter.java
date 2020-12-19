package com.marinodev;

import java.awt.*;
import java.util.List;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.function.Consumer;

public class PixelPanelPainter extends MouseAdapter {
    private int pixelSize;
    private PixelPanel panel;
    public Color currentColor = Color.BLUE;
    private List<Consumer<Pixel>> listeners = new ArrayList<>();

    public PixelPanelPainter(PixelPanel panel) {
        this.panel = panel;
        pixelSize = panel.getPixelSize();
    }

    public void addMiddleClickListener(Consumer<Pixel> pixelConsumer) {
        listeners.add(pixelConsumer);
    }

    @Override
    public void mousePressed(MouseEvent event) {
        Pixel pixel = panel.getPixelFromPos(event.getX(), event.getY());
        if (event.getButton() == MouseEvent.BUTTON1) {
            pixel.setColor(currentColor);
            pixel.repaint();
        } else if (event.getButton() == MouseEvent.BUTTON3) {
            pixel.setColor(Color.WHITE);
            pixel.repaint();
        } else if (event.getButton() == MouseEvent.BUTTON2) {
            // on a middle click
            listeners.forEach(eventConsumer -> eventConsumer.accept(pixel));
        }
    }
}
