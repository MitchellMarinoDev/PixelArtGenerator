package com.marinodev.pixelartgenerator;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SheetsQuickstart implements SpreadsheetBuilder{
    private static final String APPLICATION_NAME = "Pixel Art Generator";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static String RESOURCES_DIRECTORY_PATH;
    static {
        try {
            URL credentialsURL = SheetsQuickstart.class.getResource("/credentials.json");
            URL resourcesURL = credentialsURL.getPath().endsWith("/") ? credentialsURL.toURI().resolve("..").toURL() : credentialsURL.toURI().resolve(".").toURL();
            RESOURCES_DIRECTORY_PATH = resourcesURL.getPath();
        } catch (URISyntaxException | MalformedURLException e) {
            e.printStackTrace();
        }
    }
    public static final String TOKENS_DIRECTORY_PATH = RESOURCES_DIRECTORY_PATH + "tokens";

//    public static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = SheetsQuickstart.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    /**
     * Prints the names and majors of students in a sample spreadsheet:
     * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
     */
//    public static void main(String... args) throws IOException, GeneralSecurityException {
//        // Build a new authorized API client service.
//        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
//        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
//                .setApplicationName(APPLICATION_NAME)
//                .build();
//
//        Spreadsheet spreadsheet = new Spreadsheet()
//                .setProperties(new SpreadsheetProperties()
//                .setTitle("Pixel Art"));
//        spreadsheet = service.spreadsheets().create(spreadsheet)
//                .setFields("spreadsheetId")
//                .execute();
//        String spreadsheetId = spreadsheet.getSpreadsheetId();
//        System.out.println("Spreadsheet ID: " + spreadsheetId);
//
//        // create conditional formatting rules
//        List<Request> requests = new ArrayList<>();
//
//
//
//        BatchUpdateSpreadsheetRequest body = new BatchUpdateSpreadsheetRequest()
//                .setRequests(requests);
//        BatchUpdateSpreadsheetResponse result = service.spreadsheets()
//                .batchUpdate(spreadsheetId, body)
//                .execute();
//        System.out.printf("%d cells updated.", result.getReplies().size());
//
//
//        final String spreadsheetId = "1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms";
//        final String range = "Class Data!A2:E";
//        ValueRange response = service.spreadsheets().values()
//                .get(spreadsheetId, range)
//                .execute();
//        List<List<Object>> values = response.getValues();
//        if (values == null || values.isEmpty()) {
//            System.out.println("No data found.");
//        } else {
//            System.out.println("Name, Major");
//            for (List row : values) {
//                // Print columns A and E, which correspond to indices 0 and 4.
//                System.out.printf("%s, %s\n", row.get(0), row.get(4));
//            }
//        }
//    }

    @Override
    public void buildSheet(JFrame frame, int widthOfPixelArtSection, int heightOfPixelArtSection, String[][] questionAnswers, List<List<Pixel>> pixelGroups, java.awt.Color bgColor) {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT;
        Sheets service;
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        } catch (GeneralSecurityException e) {
            JOptionPane.showMessageDialog(frame, "Could not authorize user.");
            return;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Could not read credentials needed to create Google Sheet.");
            return;
        }
        // User authorized

        // create spreadsheet
        Spreadsheet spreadsheet = new Spreadsheet()
                .setProperties(new SpreadsheetProperties()
                .setTitle("Pixel Art"));
        try {
            spreadsheet = service.spreadsheets().create(spreadsheet)
                    .setFields("spreadsheetUrl,spreadsheetId")
                    .execute();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Could not create Sheet.");
            e.printStackTrace();
            return;
        }
        String spreadsheetId = spreadsheet.getSpreadsheetId();
        System.out.println("Spreadsheet ID: " + spreadsheetId);


        // TODO:

        // Create Questions and Headers
        List<List<Object>> values = new ArrayList<>();
        for (String[] quesAns : questionAnswers) {
            values.add(Collections.singletonList(quesAns[0]));
        }
        
        List<ValueRange> data = Arrays.asList(
            // Headers:
            new ValueRange()
                .setRange("A1:B1")
                .setValues(Collections.singletonList(Arrays.asList("QUESTIONS:", "ANSWERS:"))),
            // Questions:
            new ValueRange()
                .setRange("A" + 2 + ":A" + (questionAnswers.length+1))
                .setValues(values)
        );

        // update
        try {
            BatchUpdateValuesRequest body = new BatchUpdateValuesRequest()
                .setValueInputOption("RAW")
                .setData(data);
            service.spreadsheets().values().batchUpdate(spreadsheetId, body).execute();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error writing to Sheet");
            e.printStackTrace();
            return;
        }

        // create conditional formatting rules

//        List<Request> requests = new ArrayList<>();
//
//        // TODO: add requests to list here
//
//
//
//
//        // update
//        try {
//            BatchUpdateSpreadsheetRequest body = new BatchUpdateSpreadsheetRequest()
//                    .setRequests(requests);
//            BatchUpdateSpreadsheetResponse result = service.spreadsheets()
//                    .batchUpdate(spreadsheetId, body)
//                    .execute();
//            System.out.printf("%d cells updated.", result.getReplies().size());
//        } catch (IOException e) {
//            JOptionPane.showMessageDialog(frame, "Error writing to Sheet");
//            return;
//        }

        // Build message
        JOptionPane.showMessageDialog(frame, "Sheet built!\nGo to your google drive to find it.");
    }

    private Request createCFRuleRequest (GridRange range, String rule, java.awt.Color color) {
        return new Request().setAddConditionalFormatRule(new AddConditionalFormatRuleRequest()
                .setRule(new ConditionalFormatRule()
                        .setRanges(Collections.singletonList(range))
                        .setBooleanRule(new BooleanRule()
                                .setCondition(new BooleanCondition()
                                        .setType("CUSTOM_FORMULA")
                                        .setValues(Collections.singletonList(
                                                new ConditionValue().setUserEnteredValue(
                                                        rule)
                                        ))
                                )
                                .setFormat(new CellFormat().setBackgroundColor(
                                        new Color()
                                                .setRed(  ((float) color.getRed()  )/255)
                                                .setGreen(((float) color.getGreen())/255)
                                                .setBlue( ((float) color.getBlue() )/255)
                                ))
                        )
                )
                .setIndex(0)
        );
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