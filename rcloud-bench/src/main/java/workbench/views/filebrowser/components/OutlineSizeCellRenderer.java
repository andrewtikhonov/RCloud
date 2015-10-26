/*
 * R Cloud - R-based Cloud Platform for Computational Research
 * at EMBL-EBI (European Bioinformatics Institute)
 *
 * Copyright (C) 2007-2015 European Bioinformatics Institute
 * Copyright (C) 2009-2015 Andrew Tikhonov - andrew.tikhonov@gmail.com
 * Copyright (C) 2007-2009 Karim Chine - karim.chine@m4x.org
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package workbench.views.filebrowser.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Jul 28, 2009
 * Time: 6:11:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class OutlineSizeCellRenderer extends JLabel implements TableCellRenderer {

    static long GB = 1073741824;
    static long MB = 1048576;
    static long KB = 1024;

    static String suffixGB = " GB";
    static String suffixMB = " MB";
    static String suffixKB = " KB";

    public OutlineSizeCellRenderer() {
        super();

        setHorizontalAlignment(JLabel.RIGHT);
        setBorder(new EmptyBorder(0,0,0,10));
        setOpaque(true);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
        if (value != null) {
            long l = (Long) value;

            if (false) { // (n.getFile().isDirectory()) {
                setText("--");
            } else {
                if (l > GB) {
                    setText(Math.round(l / GB) + suffixGB);

                } else if (l > MB) {
                    setText(Math.round(l / MB) + suffixMB);

                } else {
                    setText(Math.max(Math.round(l / KB), 1) + suffixKB);
                }
            }
        } else {
            setText("");
        }

        setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
        setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());

        return this;
    }

}
