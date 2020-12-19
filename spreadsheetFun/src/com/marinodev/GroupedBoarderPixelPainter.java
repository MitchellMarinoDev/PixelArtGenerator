package com.marinodev;

import javax.swing.border.AbstractBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GroupedBoarderPixelPainter extends PixelPainter {
    public AbstractBorder selectedBoarder;
    public AbstractBorder unselectedBoarder;
    public AbstractBorder defaultBoarder;

    private List<AbstractBorder> groupBoarders;

    private int currentGroup = 0;

    List<List<Pixel>> groupedPixels = new ArrayList<>();

    public GroupedBoarderPixelPainter(PixelPanel panel) {
        super(panel);

        groupBoarders = new ArrayList<>();
        groupedPixels.add(new ArrayList<>());

        selectedBoarder   = new LineBorder(Color.GREEN, 3);
        unselectedBoarder = new LineBorder(Color.RED,   3);
        defaultBoarder = new LineBorder(Color.BLACK,    1);

        // add logic to paint pixels
        addLeftClickListener(pixel -> {
            // TODO: ERROR: will add pixel to group even if it is in a group
            groupedPixels.get(currentGroup).add(pixel);
            pixel.setBorder(selectedBoarder);
            pixel.repaint();
        });
        addRightClickListener(pixel -> {
            // TODO: check if this works
            // remove pixel from list
            for (List<Pixel> pixelList : groupedPixels) {
                if (pixelList.contains(pixel)) {
                    pixelList.remove(pixel);
                    break;
                }
            }
            // remove boarder
            pixel.setBorder(defaultBoarder);
            pixel.repaint();
        });
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
}
