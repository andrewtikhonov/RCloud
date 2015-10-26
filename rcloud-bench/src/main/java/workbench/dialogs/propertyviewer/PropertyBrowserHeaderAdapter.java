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
package workbench.dialogs.propertyviewer;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Feb 11, 2010
 * Time: 5:25:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class PropertyBrowserHeaderAdapter extends MouseAdapter {

    public static int NONE = 0;
    public int ASCENT = 1;
    public int DESCENT = 2;

    public int state = NONE;

    JTable table;

    PropertyBrowserHeaderAdapter(JTable table){
        this.table   = table;
    }

    public void mouseClicked(MouseEvent e) {
        int col = table.getTableHeader().columnAtPoint(e.getPoint());
        int sortCol = table.convertColumnIndexToModel(col);
        //table.getTableHeader().repaint();

        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }

        state++;

        if (state > DESCENT) {
            state = NONE;
        }

        ((PropertyBrowserModel)table.getModel()).
                sortByColumn(sortCol, state);
    }

}
