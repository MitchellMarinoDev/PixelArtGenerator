package com.marinodev;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PixelPainter implements Runnable {

    private JFrame frame;

    public static void main(String[] args) {
        EventQueue.invokeLater(new PixelPainter());
    }

    @Override
    public void run() {
        initGUI();
    }

    public void initGUI() {
        frame = new JFrame("Pixel Art");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.add(new PixelPanel(20, 20, 20));

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.add(new PixelPanel(20, 20, 20));
    }

//    private JPanel createPixels() {
//        int width = 30;
//        int height = 20;
//
//        JPanel panel = new JPanel();
//        panel.setLayout(new GridLayout(height, width, 0, 0));
//
//        for (int row = 0; row < height; row++) {
//            for (int column = 0; column < width; column++) {
//                PixelPanel pixelPanel = new PixelPanel();
//                pixelPanel.addMouseListener(new ColorListener(pixelPanel));
//                panel.add(pixelPanel);
//            }
//        }
//
//        return panel;
//    }



}