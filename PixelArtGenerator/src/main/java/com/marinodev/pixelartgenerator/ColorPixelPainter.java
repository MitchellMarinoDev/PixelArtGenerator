package com.marinodev.pixelartgenerator;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

public class ColorPixelPainter extends PixelPainter {
    public Color currentColor = Color.BLUE;
    public Color bgColor = Color.WHITE;

    private List<Consumer<Color>> onColorPickedListener = new ArrayList<>();

    private boolean pDown = false;

    public ColorPixelPainter(PixelArtPanel panel) {
        super(panel);
        // add logic to paint pixels
        addLeftClickListener(pixel -> {
            // if p is pressed, pick color
            if (pDown) {
                onColorPickedListener.forEach(colorConsumer -> colorConsumer.accept(pixel.color));
                return;
            }

            // otherwise, paint pixel
            pixel.color = currentColor;
            panel.paintPixel(pixel);
        });

        addRightClickListener(pixel -> {
            pixel.color = bgColor;
            panel.paintPixel(pixel);
        });
        addMiddleClickListener(pixel -> {
            onColorPickedListener.forEach(colorConsumer -> colorConsumer.accept(pixel.color));
        });

        // set up a listener for p
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(key -> {
            if (key.getKeyCode() == KeyEvent.VK_P)
                pDown = (key.getID() == KeyEvent.KEY_PRESSED);
            return false;
        });
    }

    public void addColorPickedListener(Consumer<Color> colorConsumer) {
        onColorPickedListener.add(colorConsumer);
    }
}
