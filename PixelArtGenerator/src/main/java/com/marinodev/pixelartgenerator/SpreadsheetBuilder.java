package com.marinodev.pixelartgenerator;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public interface SpreadsheetBuilder {
    void buildSheet(JFrame frame, int widthOfPixelArtSection, int heightOfPixelArtSection, String[][] questionAnswers, List<List<Pixel>> pixelGroups, Color bgColor);
}
