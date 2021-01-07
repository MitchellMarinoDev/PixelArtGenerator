package com.marinodev.pixelartgenerator;

import javax.swing.*;
import java.util.List;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.function.Consumer;

public abstract class PixelPainter extends MouseAdapter {
    private final PixelArtPanel panel;
    private final List<Consumer<Pixel>> button1Listeners = new ArrayList<>();
    private final List<Consumer<Pixel>> button2Listeners = new ArrayList<>();
    private final List<Consumer<Pixel>> button3Listeners = new ArrayList<>();

    private int lastMouseButton;

    public PixelPainter(PixelArtPanel panel) {
        super();
        panel.addMouseListener(this);
        panel.addMouseMotionListener(this);
        this.panel = panel;
    }

    public void onRebuildPixels() {}

    @Override
    public void mouseDragged(MouseEvent event) {
        Pixel pixel = panel.getPixel(event.getX() / panel.getPixelSize(), event.getY() / panel.getPixelSize());
        System.out.println(event.getButton());
        if (SwingUtilities.isLeftMouseButton(event))
            button1Listeners.forEach(pixelConsumer -> pixelConsumer.accept(pixel));
        else if (SwingUtilities.isMiddleMouseButton(event))
            button2Listeners.forEach(pixelConsumer -> pixelConsumer.accept(pixel));
        else if (SwingUtilities.isRightMouseButton(event))
            button3Listeners.forEach(pixelConsumer -> pixelConsumer.accept(pixel));
    }

    @Override
    public void mousePressed(MouseEvent event) {
        Pixel pixel = panel.getPixel(event.getX() / panel.getPixelSize(), event.getY() / panel.getPixelSize());
        switch (event.getButton()) {
            case MouseEvent.BUTTON1:
                button1Listeners.forEach(pixelConsumer -> pixelConsumer.accept(pixel));
                break;
            case MouseEvent.BUTTON2 :
                button2Listeners.forEach(pixelConsumer -> pixelConsumer.accept(pixel));
                break;
            case MouseEvent.BUTTON3:
                button3Listeners.forEach(pixelConsumer -> pixelConsumer.accept(pixel));
                break;
        }
    }

    public PixelArtPanel getPanel() {
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
