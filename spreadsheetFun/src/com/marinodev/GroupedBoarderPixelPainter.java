package com.marinodev;

import javax.swing.border.AbstractBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GroupedBoarderPixelPainter extends PixelPainter {
    public LineBorder selectedBoarder;
    public LineBorder unselectedBoarder;
    public LineBorder defaultBoarder;

    private int currentGroup = 0;

    List<List<Pixel>> groupedPixels = new ArrayList<>();

    public GroupedBoarderPixelPainter(PixelPanel panel) {
        super(panel);

        groupedPixels.add(new ArrayList<>());

        selectedBoarder   = new LineBorder(Color.GREEN, 3);
        unselectedBoarder = new LineBorder(Color.RED,   3);
        defaultBoarder    = new LineBorder(Color.BLACK, 1);


        // add logic to paint pixels
        addLeftClickListener(pixel -> {
            for (List<Pixel> pixelList : groupedPixels) {
                if (pixelList.contains(pixel))
                    return;
            }
            groupedPixels.get(currentGroup).add(pixel);
            pixel.setBorder(selectedBoarder);
            pixel.repaint();
        });
        addRightClickListener(pixel -> {
            if (groupedPixels.get(currentGroup).remove(pixel)) {
                // remove boarder
                pixel.setBorder(defaultBoarder);
                pixel.repaint();
            }
        });
    }

    @Override
    public void onRebuildPixels() {
        // new pixels are create therefore we need to make a new list of grouped pixels at the same cords
        List<List<Pixel>> newGroupedPixels = new ArrayList<>();
        for (int i = 0, groupedPixelsSize = groupedPixels.size(); i < groupedPixelsSize; i++) {
            List<Pixel> pixelList = groupedPixels.get(i);
            newGroupedPixels.add(new ArrayList<>());
            for (Pixel pixel : pixelList) {
                // get new pixel that is at the same point as the old one and add it to the list
                Pixel newPixel = super.getPanel().getPixelFromIndex(pixel.getXPos(), pixel.getYPos());
                newGroupedPixels.get(newGroupedPixels.size() - 1).add(newPixel);
                // apply the boarder
                if (currentGroup == i) {
                    newPixel.setBorder(selectedBoarder);
                } else {
                    newPixel.setBorder(unselectedBoarder);
                }
                newPixel.repaint();
            }
        }
        groupedPixels = newGroupedPixels;
    }

    public void setCurrentGroup(int currentGroup) {
        if (currentGroup + 1 > groupedPixels.size()) {
            System.out.println("Groups to make " + ((currentGroup + 1)- groupedPixels.size()));
            for (int i = 0, groupsToMake = (currentGroup + 1) - groupedPixels.size(); i < groupsToMake; i ++) {
                groupedPixels.add(new ArrayList<>());
            }
        }
        if (groupedPixels.size() > this.currentGroup) {
            for (Pixel pixel : groupedPixels.get(this.currentGroup)) {
                pixel.setBorder(unselectedBoarder);
            }
        }
        this.currentGroup = currentGroup;
        for (Pixel pixel : groupedPixels.get(this.currentGroup)) {
            pixel.setBorder(selectedBoarder);
        }
    }

    public void setMaxGroups(int maxGroups) {
        if (maxGroups < groupedPixels.size()) {
            while (maxGroups < groupedPixels.size()) {
                groupedPixels.remove(groupedPixels.size()-1);
            }
        }
    }

    public List<List<Pixel>> getPixelGroups() {
        return groupedPixels;
    }

}
