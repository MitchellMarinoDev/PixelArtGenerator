# PixelArtGenerator

A pixel art questionnaire generator for Google Sheets or Microsoft Excel.
This tool can be used to generate a spreadsheet with question and answer columns, and a pixel art image to the right.

Instructions
-----------
  1. Write out all question answer pairs in the table of the "Question" tab.
  2. Once done, ***be sure to press enter to finish editing the cell.***
  3. Go to the "Pick Image" tab if you choose to start from an image, otherwise, skip to step 5.
  4. Press "Pick Image" and select an image from your device.
  5. Set the width and height with the spinners. You can also set the size of the pixels in the editor.
  6. After updating the width or height, you can press "Rebuild Preview" to refresh the preview.
  7. Once you are satisfied with the image, width, and height parameters, go the the "Pixel Art" tab.
  8. Use the pallet on the top to select an active color.
  9. To select a new color, you can go to the "Color Chooser" sub-tab and select a new color.
  10. You can left-click to paint the selected color, middle click (or hold "P" and left click) to eyedrop a pixel, and right-click to erase a pixel.
  11. Once you are satisfied with your pixel art, press the "Bake" button right below the pallet and go the the "Grouper" tab.
  12. You must now group the pixels into groups. these groups are tied to the questions for the "Questions" tab.
            When a user types in the correct answer on one of the answer cells, the corresponding group is shown.
  13. To add a pixel to the selected group left-click it. to remove one, right-click it.
  14. Use the spinner near the top to change the active group.
  15. Pixels in the active group will have a green border, while pixels that are in a different group will have a red border.
  16. Once everything is grouped to perfection, you can generate a Google Sheet by selecting the "Build Google Sheet" button near the top.
            The first time you build a sheet with Google, it will ask for permission to edit your spreadsheets.
  17. You can also generate an XLSX file by pressing the "Build XLSX" button on the right. You must select a file location and ***you must add the .xlsx extension to the file*** 

Features:
--------
- Question answer table editor
- Image importer
- Pixel editor
- Pixel grouper
- Builds to:
  - Google sheets
  - XLSX file

TODO:
----
- When baking the pixel art image to the grouper, make the groups not reset.
- delete other unused columns when building to a spreadsheet.
- make pixel art cells more square When building to and XLSX
- make button automatic actions that happen when changing tabs
