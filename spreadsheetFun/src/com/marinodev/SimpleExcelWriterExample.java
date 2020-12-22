package com.marinodev;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * A very simple program that writes some data to an Excel file
 * using the Apache POI library.
 * @author www.codejava.net
 *
 */
public class SimpleExcelWriterExample {

    public static void main(String[] args) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Java Books");
        buildData(sheet);
        SheetConditionalFormatting sheetCF = sheet.getSheetConditionalFormatting();


        ConditionalFormattingRule rule = sheetCF.createConditionalFormattingRule("$C$3=42");
        rule.createPatternFormatting().setFillBackgroundColor(new XSSFColor(new java.awt.Color(80, 80, 100)));

        ConditionalFormattingRule[] cfRules = new ConditionalFormattingRule[]{rule};
        CellRangeAddress[] regions = new CellRangeAddress[]{CellRangeAddress.valueOf("D1:D3")};

        sheetCF.addConditionalFormatting(regions, cfRules);
    }




    private static void buildData(XSSFSheet sheet) {
        Object[][] bookData = {
                {"Head First Java", "Kathy Serria", 79},
                {"Effective Java", "Joshua Bloch", 36},
                {"Clean Code", "Robert martin", 42},
                {"Thinking in Java", "Bruce Eckel", 35},
        };

        int rowCount = 0;

        for (Object[] aBook : bookData) {
            Row row = sheet.createRow(rowCount++);

            int columnCount = 0;

            for (Object field : aBook) {
                Cell cell = row.createCell(columnCount++);
                if (field instanceof String) {
                    cell.setCellValue((String) field);
                } else if (field instanceof Integer) {
                    cell.setCellValue((Integer) field);
                }
            }
        }
    }

}