package com.marinodev.pixelartgenerator;


import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GroupedBoarderPixelPainter extends PixelPainter {
    public final LineBorderSpecification selectedBoarder;
    public final LineBorderSpecification unselectedBoarder;
    public final LineBorderSpecification defaultBoarder;

    private int currentGroup = 0;

    List<List<Pixel>> groupedPixels = new ArrayList<>();
    List<LineBorderSpecification> groupSpecs = new ArrayList<>();

    public GroupedBoarderPixelPainter(PixelArtPanel panel) {
        super(panel);

        selectedBoarder   = new LineBorderSpecification(Color.GREEN, 3);
        unselectedBoarder = new LineBorderSpecification(Color.RED,   3);
        defaultBoarder    = new LineBorderSpecification(Color.BLACK, 1);

        groupedPixels.add(new ArrayList<>());
        LineBorderSpecification spec = new LineBorderSpecification();
        spec.copy(selectedBoarder);
        groupSpecs.add(spec);

        // add logic to paint pixels
        addLeftClickListener(pixel -> {
            // if pixel is already in a list, return
            for (List<Pixel> pixelList : groupedPixels) {
                if (pixelList.contains(pixel))
                    return;
            }
            // add it to the current group and set it's border to the group's border
            groupedPixels.get(currentGroup).add(pixel);
            pixel.spec = groupSpecs.get(currentGroup);
            panel.paintPixel(pixel);
        });
        addRightClickListener(pixel -> {
            // remove the pixel from the list if it is in the current group
            if (groupedPixels.get(currentGroup).remove(pixel)) {
                // remove boarder
                pixel.spec = defaultBoarder;
                panel.paintPixel(pixel);
            }
        });
    }


//    @Override
//    public void onRebuildPixels() {
//        // TODO: Currently not working. boarder is not changed to selected/unselected
//        // new pixels are create therefore we need to make a new list of grouped pixels at the same cords
//        List<List<Pixel>> newGroupedPixels = new ArrayList<>();
//        for (int i = 0, groupedPixelsSize = groupedPixels.size(); i < groupedPixelsSize; i++) {
//            List<Pixel> pixelList = groupedPixels.get(i);
//            newGroupedPixels.add(new ArrayList<>());
//            for (Pixel pixel : pixelList) {
//                // get new pixel that is at the same point as the old one and add it to the list
//                Pixel newPixel = super.getPanel().getPixel(pixel.x, pixel.y);
//                newGroupedPixels.get(i).add(newPixel);
//                // apply the boarder
////                newPixel.spec = pixel.spec;
//            }
//        }
//        groupedPixels = newGroupedPixels;
//        // add correct boarder
//        for (int i = 0, groupedPixelsSize = groupedPixels.size(); i < groupedPixelsSize; i++) {
//            List<Pixel> pixelList = groupedPixels.get(i);
//            for (Pixel pixel : pixelList) {
//                pixel.spec = groupSpecs.get(i);
//            }
//        }
//        getPanel().repaint();
//    }

    public void setCurrentGroup(int currentGroup) {
        // make more groups if necessary
        if (currentGroup + 1 > groupedPixels.size()) {
            //System.out.println("Groups to make " + ((currentGroup + 1)- groupedPixels.size()));
            for (int i = 0, groupsToMake = (currentGroup + 1) - groupedPixels.size(); i < groupsToMake; i ++) {
                // make a new group
                groupedPixels.add(new ArrayList<>());
                groupSpecs.add(new LineBorderSpecification());
            }
        }

        // set the old boarders to unselected and the new boarders to selected
        if (this.currentGroup != currentGroup) {
            groupSpecs.get(this.currentGroup).copy(unselectedBoarder);
            this.currentGroup = currentGroup;
            groupSpecs.get(this.currentGroup).copy(selectedBoarder);
        }
        getPanel().repaint();
    }

    public void setMaxGroups(int maxGroups) {
        if (maxGroups < groupedPixels.size()) {
            while (maxGroups < groupedPixels.size()) {
                groupedPixels.remove(groupedPixels.size()-1);
                groupSpecs.remove(groupedPixels.size()-1);
            }
        }
    }

    public List<List<Pixel>> getPixelGroups() {
        return groupedPixels;
    }

}
