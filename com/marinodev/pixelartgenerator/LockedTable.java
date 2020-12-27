package com.marinodev.pixelartgenerator;

import javax.swing.*;

// table with the first column locked
public class LockedTable extends JTable {

    @Override
    public boolean isCellEditable(int row, int column) {
        if (column == 0)
            return false;
        return super.isCellEditable(row, column);
    }
}
