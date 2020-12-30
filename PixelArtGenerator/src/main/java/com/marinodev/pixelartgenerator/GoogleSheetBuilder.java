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
import java.util.*;

public class GoogleSheetBuilder implements SpreadsheetBuilder{
    private static final String APPLICATION_NAME = "Pixel Art Generator";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static String RESOURCES_DIRECTORY_PATH;
    static {
        try {
            URL credentialsURL = GoogleSheetBuilder.class.getResource("/credentials.json");
            URL resourcesURL = credentialsURL.getPath().endsWith("/") ? credentialsURL.toURI().resolve("..").toURL() : credentialsURL.toURI().resolve(".").toURL();
            RESOURCES_DIRECTORY_PATH = resourcesURL.getPath();
        } catch (URISyntaxException | MalformedURLException e) {
            e.printStackTrace();
        }
    }
    public static final String TOKENS_DIRECTORY_PATH = RESOURCES_DIRECTORY_PATH + "tokens";


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
        InputStream in = GoogleSheetBuilder.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
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


    @Override
    public void buildSheet(JFrame frame, int widthOfPixelArtSection, int heightOfPixelArtSection, String[][] questionAnswers, List<List<Pixel>> pixelGroups, java.awt.Color bgColor) {
        // verify that all groups are filled.
        if (pixelGroups.size() != questionAnswers.length || pixelGroups.stream().noneMatch(pixelList -> pixelList.size() != 0)) {
            JOptionPane.showMessageDialog(frame, "Make sure that all groups have at least 1 pixel");
            return;
        }


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

        List<Request> requests = new ArrayList<>();


        // add columns if necessary
        requests.add(new Request().setAppendDimension(new AppendDimensionRequest()
                .setSheetId(0)
                .setDimension("COLUMNS")
                .setLength(Math.max(widthOfPixelArtSection - 26, 1))));

        if (!updateSheet(requests, service, spreadsheetId, frame)) return;

        requests.add(new Request().setUpdateDimensionProperties(
            new UpdateDimensionPropertiesRequest()
                .setRange(
                    new DimensionRange()
                        .setSheetId(0)
                        .setDimension("COLUMNS")
                        .setStartIndex(2)
                        .setEndIndex(widthOfPixelArtSection + 2)
                )
                .setProperties(
                        new DimensionProperties()
                                .setPixelSize(20))
                .setFields("pixelSize")
            )
        );
        if (!updateSheet(requests, service, spreadsheetId, frame)) return;

        // create conditional formatting rules
        requests = new ArrayList<>();

        for (int groupNumber = 0, pixelGroupsSize = pixelGroups.size(); groupNumber < pixelGroupsSize; groupNumber++) {
            List<Pixel> group = pixelGroups.get(groupNumber);
            // add CF for answer
            java.awt.Color color = group.get(0).color;
            requests.add(createBGCFRuleRequest(
                singleCordsToGridRange(1, groupNumber + 1),
                "=LOWER(TRIM($B$" + (groupNumber + 2) + "))=\"" + questionAnswers[groupNumber][1].toLowerCase(Locale.ENGLISH) + '\"',
                group.get(0).color,
                (color.getRed() + color.getGreen() + color.getBlue()) < 350 ? java.awt.Color.WHITE : java.awt.Color.BLACK
            ));


            for (Pixel pixel : group) {
                requests.add(createBGCFRuleRequest(
                    singleCordsToGridRange(pixel.x + 2, pixel.y),
                    "=LOWER(TRIM($B$" + (groupNumber + 2) + "))=\"" + questionAnswers[groupNumber][1].toLowerCase(Locale.ENGLISH) + '\"',
                    pixel.color,
                    java.awt.Color.BLACK
                ));
            }
        }

        if (!updateSheet(requests, service, spreadsheetId, frame)) return;

        // Build message
        JOptionPane.showMessageDialog(frame, "Sheet built!\nGo to your google drive to find it.");
    }

    // creates CF rule to change background color
    private Request createBGCFRuleRequest(GridRange range, String rule, java.awt.Color bgColor, java.awt.Color textColor) {
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
                            .setRed(  ((float) bgColor.getRed()  )/255)
                            .setGreen(((float) bgColor.getGreen())/255)
                            .setBlue( ((float) bgColor.getBlue() )/255)
                        )
                        .setTextFormat(new TextFormat().setForegroundColor(
                                new Color()
                                    .setRed(  ((float) textColor.getRed()  )/255)
                                    .setGreen(((float) textColor.getGreen())/255)
                                    .setBlue( ((float) textColor.getBlue() )/255)
                        ))
                    )
                )
            )
            .setIndex(0)
        );
    }

    private GridRange singleCordsToGridRange(int x, int y) {
        return new GridRange()
            .setSheetId(0)
            .setStartRowIndex(y)
            .setEndRowIndex(y+1)
            .setStartColumnIndex(x)
            .setEndColumnIndex(x+1);
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

    private static boolean updateSheet(List<Request> requests, Sheets service, String spreadsheetId, JFrame frame) {
        try {
            BatchUpdateSpreadsheetRequest body = new BatchUpdateSpreadsheetRequest()
                    .setRequests(requests);
            service.spreadsheets()
                    .batchUpdate(spreadsheetId, body)
                    .execute();
            return true;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error writing to Sheet");
            e.printStackTrace();
            return false;
        }
    }
}

/*
        List<GridRange> ranges = Collections.singletonList(new GridRange()
                .setSheetId(0)
                .setStartRowIndex(1)
                .setEndRowIndex(11)
                .setStartColumnIndex(0)
                .setEndColumnIndex(4)
        );
        List<Request> requests = Collections.singletonList(
                new Request().setAddConditionalFormatRule(new AddConditionalFormatRuleRequest()
                        .setRule(new ConditionalFormatRule()
                                .setRanges(ranges)
                                .setBooleanRule(new BooleanRule()
                                        .setCondition(new BooleanCondition()
                                                .setType("CUSTOM_FORMULA")
                                                .setValues(Collections.singletonList(
                                                        new ConditionValue().setUserEnteredValue(
                                                                "=LT($D2,median($D$2:$D$11))")
                                                ))
                                        )
                                        .setFormat(new CellFormat().setBackgroundColor(
                                                new Color().setRed(1f).setGreen(0.4f).setBlue(0.4f)
                                        ))
                                )
                        )
                        .setIndex(0)
                )
        );

                BatchUpdateSpreadsheetRequest body = new BatchUpdateSpreadsheetRequest()
                .setRequests(requests);
        BatchUpdateSpreadsheetResponse result = null;
        try {
            result = service.spreadsheets()
                .batchUpdate(spreadsheetId, body)
                .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.printf("%d cells updated.", result.getReplies().size());
 */