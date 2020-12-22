package com.marinodev;

import java.util.List;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.function.Consumer;

public abstract class PixelPainter extends MouseAdapter {
    private PixelPanel panel;
    private List<Consumer<Pixel>> button1Listeners = new ArrayList<>();
    private List<Consumer<Pixel>> button2Listeners = new ArrayList<>();
    private List<Consumer<Pixel>> button3Listeners = new ArrayList<>();

    public PixelPainter(PixelPanel panel) {
        super();
        panel.addMouseListener(this);
        this.panel = panel;
    }

    public void onRebuildPixels() {}

    @Override
    public void mousePressed(MouseEvent event) {
        Pixel pixel = panel.getPixelFromPos(event.getX(), event.getY());
        switch (event.getButton()) {
            case MouseEvent.BUTTON1 -> button1Listeners.forEach(pixelConsumer -> pixelConsumer.accept(pixel));
            case MouseEvent.BUTTON2 -> button2Listeners.forEach(pixelConsumer -> pixelConsumer.accept(pixel));
            case MouseEvent.BUTTON3 -> button3Listeners.forEach(pixelConsumer -> pixelConsumer.accept(pixel));
        }
    }

    public PixelPanel getPanel() {
        return panel;
    }

    public void addLeftClickListener  (Consumer<Pixel> pixelConsumer)  {
        button1Listeners.add(pixelConsumer);
    }
    public void addMiddleClickListener(Consumer<Pixel> pixelConsumer) {
        button2Listeners.add(pixelConsumer);
    }
    public void addRightClickListener (Consumer<Pixel> pixelConsumer) {
        button3Listeners.add(pixelConsumer);
    }
}
