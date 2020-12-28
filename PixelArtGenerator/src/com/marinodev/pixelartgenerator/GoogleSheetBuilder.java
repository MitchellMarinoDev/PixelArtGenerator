package com.marinodev.pixelartgenerator;

import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.SheetConditionalFormatting;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.List;

public class GoogleSheetBuilder implements SpreadsheetBuilder {
    public void buildSheet(JFrame frame, int widthOfPixelArtSection, int heightOfPixelArtSection, String[][] questionAnswers, List<List<Pixel>> pixelGroups) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Pixel Art Quiz");

        // make room for pixel art
        for (int rowI = 0; rowI < heightOfPixelArtSection; rowI++) {
            Row row = sheet.getRow(rowI);
            if (row == null)
                row = sheet.createRow(rowI);
            for (int col = 0; col < widthOfPixelArtSection; col++) {
                row.createCell(col);
                sheet.setColumnWidth(col + 2, 1000);
            }
        }

        { // Create Headers
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("QUESTIONS:");
            headerRow.createCell(1).setCellValue("ANSWERS:");
        }
        // Build data for ques ans pairs
        int rowCount = 0;
        for (String[] rowData : questionAnswers) {
            Row row = sheet.createRow(rowCount++);
            row.createCell(0).setCellValue(rowData[0]); // write the question into the cell
        }


        SheetConditionalFormatting sheetCF = sheet.getSheetConditionalFormatting();

        // for each group
        for (int i = 0, questionAnswersLength = questionAnswers.length; i < questionAnswersLength; i++) {
            String answer = questionAnswers[i][1];
            List<Pixel> pixelGroup = pixelGroups.get(i);

            buildRulesForGroup(pixelGroup, sheetCF, answer, i);
        }

        // choose save location
        JFileChooser fileSelector = new JFileChooser();
        fileSelector.addChoosableFileFilter(new FileNameExtensionFilter("xlsx", "xlsx"));
        fileSelector.setMultiSelectionEnabled(false);
        int returnVal = fileSelector.showSaveDialog(frame);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileSelector.getSelectedFile();

            writeSheet(workbook, file);
            workbook.close();
        }
    }

    private static void writeSheet(XSSFWorkbook workbook, String fileName) throws IOException {
        FileOutputStream outputStream = new FileOutputStream(fileName);
        workbook.write(outputStream);
    }
    private static void writeSheet(XSSFWorkbook workbook, File file) throws IOException {
        FileOutputStream outputStream = new FileOutputStream(file);
        workbook.write(outputStream);
    }

    private static void buildRulesForGroup(List<Pixel> pixelGroup, SheetConditionalFormatting sheetCF, String answer, int groupNumber) {
        for (Pixel pixel : pixelGroup) {
            ConditionalFormattingRule rule = sheetCF.createConditionalFormattingRule("LOWER(TRIM($B$" + (groupNumber + 1) + "))=\"" + answer.toLowerCase(Locale.ENGLISH) + "\"");
            rule.createPatternFormatting().setFillBackgroundColor(new XSSFColor(pixel.color));

            String addressString = sheetCordFromInt(pixel.x + 2) + (pixel.y + 1);
            CellRangeAddress[] regions = new CellRangeAddress[]{CellRangeAddress.valueOf(addressString)};
            ConditionalFormattingRule[] cfRules = new ConditionalFormattingRule[]{rule};

            sheetCF.addConditionalFormatting(regions, cfRules);
        }
    }


    // returns A-Z or AA-ZZ from a given int starting at 0
    private static String sheetCordFromInt(int num) {
        if (num < 0)
            throw new IllegalArgumentException("Number " + num + " is out of bounds.");
        if (num < 26) {
            return Character.toString((char)(65 + num));
        } else if (num < 26 * 26) {
            char first  = (char) (64 + (num / 26));
            char second = (char) (65 + (num % 26));
            System.out.println(num % 26);
            return String.valueOf(first) + second;
        }
        throw new IllegalArgumentException("Number " + num + " is out of bounds.");
    }
}