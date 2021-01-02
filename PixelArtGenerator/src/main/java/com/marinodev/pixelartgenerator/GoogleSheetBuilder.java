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
import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.*;

public class GoogleSheetBuilder implements SpreadsheetBuilder{
    private static final String APPLICATION_NAME = "Pixel Art Generator";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String APP_DATA_DIR;
    static {
        AppDirs appDirs = AppDirsFactory.getInstance();
        APP_DATA_DIR = appDirs.getUserDataDir(APPLICATION_NAME.replace(" ", ""), null, "MarinoDev");
    }
    public static final String TOKENS_DIRECTORY_PATH = APP_DATA_DIR + "/tokens";

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
            String rule = "=LOWER(TRIM($B$" + (groupNumber + 2) + "))=\"" + questionAnswers[groupNumber][1].toLowerCase(Locale.ENGLISH) + '\"';
            List<Pixel> group = pixelGroups.get(groupNumber);
            // add CF for answer
            java.awt.Color color = group.get(0).color;
            requests.add(createBGCFRuleRequest(
                singleCordsToGridRange(1, groupNumber + 1),
                rule,
                group.get(0).color,
                // if it is too dark make the text white
                (color.getRed() + color.getGreen() + color.getBlue()) < 350 ? java.awt.Color.WHITE : java.awt.Color.BLACK
            ));


            for (Pixel pixel : group) {
                requests.add(createBGCFRuleRequest(
                    singleCordsToGridRange(pixel.x + 2, pixel.y),
                    rule,
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

    private static GridRange singleCordsToGridRange(int x, int y) {
        return new GridRange()
                .setSheetId(0)
                .setStartRowIndex(y)
                .setEndRowIndex(y+1)
                .setStartColumnIndex(x)
                .setEndColumnIndex(x+1);
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