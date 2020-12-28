package com.marinodev.pixelartgenerator;

import java.awt.*;

class RemoveAlphaColorTest {

    public static void main(String[] args) {
        Color input;
        Color output;
        Color expectedResult;

        // white alpha 0
        input = new Color(0, 0, 0, 0);
        output = PixelArtPanel.removeAlpha(input, Color.WHITE);
        expectedResult = Color.WHITE;

        System.out.println(expectedResult);
        assert output.equals(expectedResult);

        input = new Color(255, 255, 255, 0);
        output = PixelArtPanel.removeAlpha(input, Color.BLACK);
        expectedResult = Color.BLACK;

        System.out.println(expectedResult);
        assert output.equals(expectedResult);

        input = new Color(255, 255, 255, 127);
        output = PixelArtPanel.removeAlpha(input, Color.BLACK);
        expectedResult = new Color(127, 127, 127);

        System.out.println(expectedResult);
        assert output.equals(expectedResult);
    }
}